package com.conquestimo.service;

import com.conquestimo.entity.*;
import com.conquestimo.repository.GamePlayerRepository;
import com.conquestimo.repository.GameRegionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GameEndService {

    private final GamePlayerRepository playerRepository;
    private final GameRegionRepository regionRepository;

    public GameEndService(GamePlayerRepository playerRepository, GameRegionRepository regionRepository) {
        this.playerRepository = playerRepository;
        this.regionRepository = regionRepository;
    }

    /** Returns true if the game has ended. Updates game state accordingly. */
    public boolean checkAndHandleGameEnd(Game game, List<GameRegion> regions) {
        List<GamePlayer> players = playerRepository.findByGameId(game.getId());

        // Update elimination status
        Set<Long> playersWithRegions = regions.stream()
                .filter(r -> r.getOwner() != null)
                .map(r -> r.getOwner().getId())
                .collect(Collectors.toSet());

        for (GamePlayer player : players) {
            if (!player.isEliminated() && !playersWithRegions.contains(player.getId())) {
                player.setEliminated(true);
                playerRepository.save(player);
            }
        }

        List<GamePlayer> activePlayers = players.stream()
                .filter(p -> !p.isEliminated())
                .collect(Collectors.toList());

        if (activePlayers.size() <= 1) {
            game.setState(GameState.ENDED);
            return true;
        }
        return false;
    }
}
