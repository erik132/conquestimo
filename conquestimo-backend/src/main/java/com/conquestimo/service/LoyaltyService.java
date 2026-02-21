package com.conquestimo.service;

import com.conquestimo.dto.TurnEventDto;
import com.conquestimo.entity.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class LoyaltyService {

    private final BattleService battleService;
    private final RegionPotentialCalculator potentialCalculator;
    private final Random random = new Random();

    public LoyaltyService(BattleService battleService, RegionPotentialCalculator potentialCalculator) {
        this.battleService = battleService;
        this.potentialCalculator = potentialCalculator;
    }

    public void applyLoyaltyGrowth(List<GameRegion> regions) {
        for (GameRegion region : regions) {
            if (region.getOwner() == null) continue;
            int increase = region.getCulture().getLoyaltyPerTurn();
            if (region.getCurrentTask() == RegionTask.DIPLOMATS) {
                increase += (int) Math.floor(potentialCalculator.calculate(region));
            }
            region.setLoyalty(Math.min(100, region.getLoyalty() + increase));
        }
    }

    public List<TurnEventDto> processRebellions(List<GameRegion> regions, Game game) {
        List<TurnEventDto> events = new ArrayList<>();
        for (GameRegion region : regions) {
            if (region.getOwner() == null) continue;
            if (region.getLoyalty() >= 70) continue;

            int roll = random.nextInt(70);
            if (roll <= region.getLoyalty()) continue;

            // Rebellion triggered
            int rebelCount = random.nextInt(region.getFarmLevel() + 1); // 0 to farmLevel
            TurnEventDto event;

            if (rebelCount == 0) {
                event = TurnEventDto.rebellion(region.getTerritoryId(), region.getId(), 0, true);
            } else {
                // Rebels attack, player defends
                BattleService.BattleResult result = battleService.resolveBattle(rebelCount, region);
                battleService.recordBattle(game, region, null, region.getOwner(), !result.attackerWon());

                if (result.attackerWon()) {
                    // Rebels win — region goes neutral
                    int survivingRebels = rebelCount - result.attackerArmiesLost();
                    region.setOwner(null);
                    region.setArmyCount(survivingRebels);
                    region.setCurrentTask(RegionTask.NONE);
                    event = TurnEventDto.rebellion(region.getTerritoryId(), region.getId(), rebelCount, false);
                } else {
                    // Player wins
                    int remainingArmies = region.getArmyCount() - result.defenderArmiesLost();
                    region.setArmyCount(Math.max(0, remainingArmies));
                    event = TurnEventDto.rebellion(region.getTerritoryId(), region.getId(), rebelCount, true);
                }
            }
            events.add(event);
        }
        return events;
    }

    public void halveOnCapture(GameRegion region) {
        region.setLoyalty(region.getLoyalty() / 2);
    }
}
