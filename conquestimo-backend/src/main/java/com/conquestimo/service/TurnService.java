package com.conquestimo.service;

import com.conquestimo.dto.UpkeepPreviewDto;
import com.conquestimo.entity.*;
import com.conquestimo.repository.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TurnService {

    private final GameRepository gameRepository;
    private final GamePlayerRepository playerRepository;
    private final TurnSubmissionRepository submissionRepository;
    private final GameRegionRepository regionRepository;
    private final TurnResolutionService resolutionService;
    private final UpkeepService upkeepService;

    public TurnService(GameRepository gameRepository,
                       GamePlayerRepository playerRepository,
                       TurnSubmissionRepository submissionRepository,
                       GameRegionRepository regionRepository,
                       TurnResolutionService resolutionService,
                       UpkeepService upkeepService) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.submissionRepository = submissionRepository;
        this.regionRepository = regionRepository;
        this.resolutionService = resolutionService;
        this.upkeepService = upkeepService;
    }

    @Transactional
    public void endTurn(Game game, GamePlayer player) {
        if (game.getState() != GameState.IN_PROGRESS) {
            throw new IllegalStateException("Game is not in progress");
        }
        if (submissionRepository.existsByGameIdAndPlayerIdAndTurnNumber(
                game.getId(), player.getId(), game.getTurnNumber())) {
            throw new IllegalStateException("Turn already ended");
        }

        TurnSubmission submission = new TurnSubmission();
        submission.setGame(game);
        submission.setPlayer(player);
        submission.setTurnNumber(game.getTurnNumber());
        submissionRepository.save(submission);

        checkAndTriggerResolution(game);
    }

    private void checkAndTriggerResolution(Game game) {
        List<GamePlayer> activePlayers = playerRepository.findByGameId(game.getId()).stream()
                .filter(p -> !p.isEliminated() && !p.isAi())
                .collect(Collectors.toList());

        long humanCount = activePlayers.size();
        long humanSubmissions = activePlayers.stream()
                .filter(p -> submissionRepository.existsByGameIdAndPlayerIdAndTurnNumber(
                        game.getId(), p.getId(), game.getTurnNumber()))
                .count();

        if (humanSubmissions >= humanCount) {
            resolutionService.resolveTurn(game);
        }
    }

    @Scheduled(fixedDelay = 10_000)
    @Transactional
    public void checkTimers() {
        List<Game> activeGames = gameRepository.findByState(GameState.IN_PROGRESS);
        for (Game game : activeGames) {
            if (game.getTurnStartedAt() == null) {
                game.setTurnStartedAt(LocalDateTime.now());
                gameRepository.save(game);
                continue;
            }
            LocalDateTime deadline = game.getTurnStartedAt()
                    .plusSeconds(game.getTurnTimerSeconds());
            if (LocalDateTime.now().isAfter(deadline)) {
                // Force-end turn for all players who haven't submitted
                List<GamePlayer> activePlayers = playerRepository.findByGameId(game.getId()).stream()
                        .filter(p -> !p.isEliminated() && !p.isAi())
                        .collect(Collectors.toList());

                for (GamePlayer player : activePlayers) {
                    if (!submissionRepository.existsByGameIdAndPlayerIdAndTurnNumber(
                            game.getId(), player.getId(), game.getTurnNumber())) {
                        TurnSubmission submission = new TurnSubmission();
                        submission.setGame(game);
                        submission.setPlayer(player);
                        submission.setTurnNumber(game.getTurnNumber());
                        submissionRepository.save(submission);
                    }
                }

                resolutionService.resolveTurn(game);
            }
        }
    }

    public UpkeepPreviewDto getUpkeepPreview(Game game, GamePlayer player) {
        List<GameRegion> allRegions = regionRepository.findByGameId(game.getId());
        return upkeepService.getPreview(player, allRegions);
    }

    public boolean hasSubmitted(Game game, GamePlayer player) {
        return submissionRepository.existsByGameIdAndPlayerIdAndTurnNumber(
                game.getId(), player.getId(), game.getTurnNumber());
    }
}
