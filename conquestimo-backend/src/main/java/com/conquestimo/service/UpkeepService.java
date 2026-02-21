package com.conquestimo.service;

import com.conquestimo.dto.TurnEventDto;
import com.conquestimo.dto.UpkeepPreviewDto;
import com.conquestimo.entity.*;
import com.conquestimo.repository.GamePlayerRepository;
import com.conquestimo.repository.GameRegionRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UpkeepService {

    private final GamePlayerRepository playerRepository;
    private final GameRegionRepository regionRepository;
    private final Random random = new Random();

    public UpkeepService(GamePlayerRepository playerRepository, GameRegionRepository regionRepository) {
        this.playerRepository = playerRepository;
        this.regionRepository = regionRepository;
    }

    public int calculateRequiredGold(List<GameRegion> playerRegions) {
        return playerRegions.stream()
                .mapToInt(r -> (int) Math.floor(Math.pow(r.getArmyCount(), 1.3)))
                .sum();
    }

    public List<TurnEventDto> processWinterUpkeep(List<GamePlayer> players, List<GameRegion> allRegions) {
        List<TurnEventDto> events = new ArrayList<>();
        for (GamePlayer player : players) {
            if (player.isEliminated()) continue;
            List<GameRegion> playerRegions = allRegions.stream()
                    .filter(r -> r.getOwner() != null && r.getOwner().getId().equals(player.getId()))
                    .toList();
            if (playerRegions.isEmpty()) continue;

            int required = calculateRequiredGold(playerRegions);
            if (required == 0) continue;

            int armiesLost = 0;
            if (player.getGold() < required) {
                double payRatio = (double) player.getGold() / required * 100.0;
                for (GameRegion region : playerRegions) {
                    int newCount = region.getArmyCount();
                    for (int i = 0; i < region.getArmyCount(); i++) {
                        int roll = random.nextInt(101);
                        if (roll > payRatio) {
                            newCount--;
                            armiesLost++;
                        }
                    }
                    region.setArmyCount(Math.max(0, newCount));
                }
                player.setGold(0);
            } else {
                player.setGold(player.getGold() - required);
            }

            String name = player.isAi() ? player.getAiName() : (player.getUser() != null ? player.getUser().getUsername() : "Unknown");
            events.add(TurnEventDto.upkeep(name, Math.min(required, player.getGold() + required), armiesLost));
        }
        return events;
    }

    public UpkeepPreviewDto getPreview(GamePlayer player, List<GameRegion> allRegions) {
        List<GameRegion> playerRegions = allRegions.stream()
                .filter(r -> r.getOwner() != null && r.getOwner().getId().equals(player.getId()))
                .toList();
        int required = calculateRequiredGold(playerRegions);
        return new UpkeepPreviewDto(required, player.getGold());
    }
}
