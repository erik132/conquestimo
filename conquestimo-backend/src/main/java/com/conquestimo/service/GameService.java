package com.conquestimo.service;

import com.conquestimo.dto.*;
import com.conquestimo.entity.*;
import com.conquestimo.repository.AppUserRepository;
import com.conquestimo.repository.GamePlayerRepository;
import com.conquestimo.repository.GameRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameService {

    private static final List<String> PLAYER_COLORS = List.of(
            "#e74c3c", "#3498db", "#2ecc71", "#f39c12", "#9b59b6"
    );

    private static final List<String> AI_NAMES = List.of(
            "AI Magnus", "AI Attila", "AI Caesar", "AI Genghis", "AI Napoleon"
    );

    private final GameRepository gameRepository;
    private final GamePlayerRepository gamePlayerRepository;
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SimpMessagingTemplate messagingTemplate;
    private final GameInitializationService gameInitializationService;

    public GameService(GameRepository gameRepository,
                       GamePlayerRepository gamePlayerRepository,
                       AppUserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       SimpMessagingTemplate messagingTemplate,
                       @Lazy GameInitializationService gameInitializationService) {
        this.gameRepository = gameRepository;
        this.gamePlayerRepository = gamePlayerRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.messagingTemplate = messagingTemplate;
        this.gameInitializationService = gameInitializationService;
    }

    public List<GameSummaryDto> listGames() {
        return gameRepository.findByStateNot(GameState.ENDED).stream()
                .map(game -> new GameSummaryDto(
                        game.getId(),
                        game.getName(),
                        game.getState(),
                        gamePlayerRepository.countByGameId(game.getId()),
                        game.getMaxPlayers(),
                        game.getPasswordHash() != null,
                        gamePlayerRepository.countByGameIdAndAiTrueAndEliminatedFalse(game.getId())
                ))
                .collect(Collectors.toList());
    }

    public GameDetailDto getGameDetail(Long gameId) {
        Game game = findGame(gameId);
        return toDetailDto(game);
    }

    @Transactional
    public GameDetailDto createGame(String username, CreateGameRequest request) {
        AppUser user = findUser(username);

        Game game = new Game();
        game.setName(request.getName());
        game.setTurnTimerSeconds(request.getTimerSeconds());
        game.setMovementCap(request.getMovementCap());
        game.setMaxPlayers(request.getMaxPlayers());
        game.setCreatedByUserId(user.getId());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            game.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        game = gameRepository.save(game);

        GamePlayer player = new GamePlayer();
        player.setGame(game);
        player.setUser(user);
        player.setColor(PLAYER_COLORS.get(0));
        gamePlayerRepository.save(player);

        GameDetailDto detail = toDetailDto(game);
        broadcastLobbyUpdate();
        return detail;
    }

    @Transactional
    public GameDetailDto joinGame(String username, Long gameId, JoinGameRequest request) {
        AppUser user = findUser(username);
        Game game = findGame(gameId);

        if (game.getState() == GameState.ENDED) {
            throw new IllegalStateException("Game has ended");
        }

        if (game.getState() == GameState.LOBBY) {
            if (gamePlayerRepository.existsByGameIdAndUserId(gameId, user.getId())) {
                throw new IllegalStateException("Already in this game");
            }
            int currentCount = gamePlayerRepository.countByGameId(gameId);
            if (currentCount >= game.getMaxPlayers()) {
                throw new IllegalStateException("Game is full");
            }
            if (game.getPasswordHash() != null) {
                String provided = request.getPassword();
                if (provided == null || !passwordEncoder.matches(provided, game.getPasswordHash())) {
                    throw new IllegalArgumentException("Incorrect password");
                }
            }

            String color = assignColor(gameId);
            GamePlayer player = new GamePlayer();
            player.setGame(game);
            player.setUser(user);
            player.setColor(color);
            gamePlayerRepository.save(player);

            GameDetailDto detail = toDetailDto(game);
            broadcastLobbyUpdate();
            broadcastGameUpdate(gameId, detail);
            return detail;
        }

        // IN_PROGRESS path
        java.util.Optional<GamePlayer> existing = gamePlayerRepository.findByGameIdAndUserUsername(gameId, username);

        if (existing.isPresent()) {
            // RECONNECT CASE
            GamePlayer player = existing.get();
            if (!player.isAi()) {
                // Already a human player — just return current state
                return toDetailDto(game);
            }
            // Slot was converted to AI; restore original owner
            player.setAi(false);
            player.setAiName(null);
            player.setAiTargetPlayerId(null);
            player.setAiAttackTurnCount(0);
            player.setAiRegionsCapturedThisCycle(0);
            player.setAiDevRollTurn(0);
            gamePlayerRepository.save(player);

            GameDetailDto detail = toDetailDto(game);
            broadcastLobbyUpdate();
            broadcastGameUpdate(gameId, detail);
            return detail;
        }

        // NEW PLAYER TAKEOVER
        if (game.getPasswordHash() != null) {
            String provided = request.getPassword();
            if (provided == null || !passwordEncoder.matches(provided, game.getPasswordHash())) {
                throw new IllegalArgumentException("Incorrect password");
            }
        }

        java.util.List<GamePlayer> aiPlayers = gamePlayerRepository.findByGameIdAndAiTrueAndEliminatedFalse(gameId);
        if (aiPlayers.isEmpty()) {
            throw new IllegalStateException("No AI players available to take over");
        }

        GamePlayer target = aiPlayers.get(0);
        target.setAi(false);
        target.setUser(user);
        target.setAiName(null);
        target.setAiTargetPlayerId(null);
        target.setAiAttackTurnCount(0);
        target.setAiRegionsCapturedThisCycle(0);
        target.setAiDevRollTurn(0);
        gamePlayerRepository.save(target);

        GameDetailDto detail = toDetailDto(game);
        broadcastLobbyUpdate();
        broadcastGameUpdate(gameId, detail);
        return detail;
    }

    @Transactional
    public void leaveGame(String username, Long gameId) {
        AppUser user = findUser(username);
        Game game = findGame(gameId);

        if (game.getState() != GameState.LOBBY) {
            throw new IllegalStateException("Cannot leave a game that has already started");
        }

        GamePlayer player = gamePlayerRepository.findByGameIdAndUserId(gameId, user.getId())
                .orElseThrow(() -> new IllegalStateException("Not in this game"));
        gamePlayerRepository.delete(player);

        int remaining = gamePlayerRepository.countByGameId(gameId);
        if (remaining == 0) {
            gameRepository.delete(game);
            broadcastLobbyUpdate();
        } else {
            GameDetailDto detail = toDetailDto(game);
            broadcastLobbyUpdate();
            broadcastGameUpdate(gameId, detail);
        }
    }

    @Transactional
    public GameDetailDto startGame(String username, Long gameId) {
        AppUser user = findUser(username);
        Game game = findGame(gameId);

        if (game.getState() != GameState.LOBBY) {
            throw new IllegalStateException("Game is not in lobby state");
        }
        if (!user.getId().equals(game.getCreatedByUserId())) {
            throw new IllegalStateException("Only the game creator can start the game");
        }

        fillWithAiPlayers(game);

        game.setState(GameState.IN_PROGRESS);
        gameRepository.save(game);

        gameInitializationService.initializeGame(game);

        GameDetailDto detail = toDetailDto(game);
        broadcastLobbyUpdate();
        broadcastGameUpdate(gameId, detail);
        return detail;
    }

    private void fillWithAiPlayers(Game game) {
        int current = gamePlayerRepository.countByGameId(game.getId());
        int aiIndex = 0;

        for (int i = current; i < game.getMaxPlayers(); i++) {
            String color = assignColor(game.getId());
            String aiName = aiIndex < AI_NAMES.size() ? AI_NAMES.get(aiIndex) : "AI Player " + (aiIndex + 1);
            aiIndex++;

            GamePlayer aiPlayer = new GamePlayer();
            aiPlayer.setGame(game);
            aiPlayer.setAi(true);
            aiPlayer.setColor(color);
            // Store AI name in a transient way — we use the color+index to identify them.
            // Username is resolved as "AI" in the DTO. We store the name via a dedicated field added below.
            aiPlayer.setAiName(aiName);
            gamePlayerRepository.save(aiPlayer);
        }
    }

    private String assignColor(Long gameId) {
        List<GamePlayer> existing = gamePlayerRepository.findByGameId(gameId);
        List<String> taken = existing.stream().map(GamePlayer::getColor).collect(Collectors.toList());
        return PLAYER_COLORS.stream()
                .filter(c -> !taken.contains(c))
                .findFirst()
                .orElse("#cccccc");
    }

    private GameDetailDto toDetailDto(Game game) {
        String createdByUsername = game.getCreatedByUserId() == null ? null :
                userRepository.findById(game.getCreatedByUserId())
                        .map(AppUser::getUsername)
                        .orElse(null);

        List<PlayerDto> players = gamePlayerRepository.findByGameId(game.getId()).stream()
                .map(gp -> new PlayerDto(
                        gp.getId(),
                        gp.isAi() ? gp.getAiName() : (gp.getUser() != null ? gp.getUser().getUsername() : "Unknown"),
                        gp.isAi(),
                        gp.getColor(),
                        gp.getGold(),
                        gp.isEliminated()
                ))
                .collect(Collectors.toList());

        return new GameDetailDto(
                game.getId(),
                game.getName(),
                game.getState(),
                game.getSeason(),
                game.getTurnNumber(),
                game.getTurnTimerSeconds(),
                game.getMovementCap(),
                game.getMaxPlayers(),
                game.getPasswordHash() != null,
                createdByUsername,
                players,
                game.getTurnStartedAt()
        );
    }

    public void broadcastGameUpdate(Long gameId, GameDetailDto detail) {
        messagingTemplate.convertAndSend("/topic/game/" + gameId, detail);
    }

    private void broadcastLobbyUpdate() {
        messagingTemplate.convertAndSend("/topic/lobby", listGames());
    }

    private Game findGame(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
    }

    private AppUser findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
