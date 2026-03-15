package com.conquestimo.service;

import com.conquestimo.entity.*;
import com.conquestimo.repository.ArmyMovementRepository;
import com.conquestimo.repository.BattleRecordRepository;
import com.conquestimo.repository.GamePlayerRepository;
import com.conquestimo.repository.GameRegionRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Comparator;

@Service
public class AiService {

    private final GameRegionRepository regionRepository;
    private final GamePlayerRepository playerRepository;
    private final ArmyMovementRepository movementRepository;
    private final TerritoryLoader territoryLoader;
    private final RegionPotentialCalculator potentialCalculator;
    private final UpkeepService upkeepService;
    private final BattleRecordRepository battleRecordRepository;
    private final Random random = new Random();

    public AiService(GameRegionRepository regionRepository,
                     GamePlayerRepository playerRepository,
                     ArmyMovementRepository movementRepository,
                     TerritoryLoader territoryLoader,
                     RegionPotentialCalculator potentialCalculator,
                     UpkeepService upkeepService,
                     BattleRecordRepository battleRecordRepository) {
        this.regionRepository = regionRepository;
        this.playerRepository = playerRepository;
        this.movementRepository = movementRepository;
        this.territoryLoader = territoryLoader;
        this.potentialCalculator = potentialCalculator;
        this.upkeepService = upkeepService;
        this.battleRecordRepository = battleRecordRepository;
    }

    public void processTurn(Game game, GamePlayer aiPlayer) {
        List<GameRegion> allRegions = regionRepository.findByGameId(game.getId());
        List<GameRegion> myRegions = allRegions.stream()
                .filter(r -> r.getOwner() != null && r.getOwner().getId().equals(aiPlayer.getId()))
                .collect(Collectors.toList());

        if (myRegions.isEmpty()) return;

        if (game.getTurnNumber() <= 10) {
            processEarlyGame(game, aiPlayer, allRegions, myRegions);
            return;
        }

        List<GamePlayer> allPlayers = playerRepository.findByGameId(game.getId());
        List<GamePlayer> enemies = allPlayers.stream()
                .filter(p -> !p.getId().equals(aiPlayer.getId()) && !p.isEliminated())
                .collect(Collectors.toList());

        if (enemies.isEmpty()) return;

        // Pick or maintain attack target
        GamePlayer target = pickOrMaintainTarget(game, aiPlayer, enemies);

        // Attack logic
        int movesUsed = 0;
        movesUsed += executeAttacks(game, aiPlayer, target, allRegions, myRegions, movesUsed);
        movesUsed += executeNeutralOpportunism(game, aiPlayer, allRegions, myRegions, movesUsed);
        movesUsed += executeEmptyEnemyOpportunism(game, aiPlayer, allRegions, myRegions, movesUsed);

        // Assign tasks to regions
        for (GameRegion region : myRegions) {
            boolean borderingEnemy = isBorderingEnemy(region, allRegions, aiPlayer);
            if (borderingEnemy) {
                assignBorderTask(game, region, aiPlayer, allRegions);
            } else {
                assignDevelopmentTask(game, aiPlayer, region, allRegions);
            }
        }

        // Move interior armies toward target
        for (GameRegion region : myRegions) {
            if (movesUsed >= game.getMovementCap()) break;
            if (isBorderingEnemy(region, allRegions, aiPlayer)) continue;
            if (region.getCurrentTask() != RegionTask.ARMIES) continue;
            if (region.getArmyCount() < 2) continue;

            // Find adjacent region closer to target
            String nextStep = getStepTowardTarget(region, target, allRegions, aiPlayer);
            if (nextStep != null) {
                GameRegion dest = allRegions.stream()
                        .filter(r -> r.getTerritoryId().equals(nextStep))
                        .findFirst().orElse(null);
                if (dest != null && dest.getOwner() != null && dest.getOwner().getId().equals(aiPlayer.getId())) {
                    queueMovement(game, aiPlayer, region, dest, region.getArmyCount() - 1);
                    movesUsed++;
                }
            }
        }

        regionRepository.saveAll(myRegions);
    }

    private void processEarlyGame(Game game, GamePlayer aiPlayer, List<GameRegion> allRegions, List<GameRegion> myRegions) {
        // All owned regions focus on producing armies
        for (GameRegion region : myRegions) {
            region.setCurrentTask(RegionTask.ARMIES);
        }
        regionRepository.saveAll(myRegions);

        // Collect neutral regions sorted by fewest armies first, then by distance as tiebreaker
        List<GameRegion> neutralRegions = allRegions.stream()
                .filter(r -> r.getOwner() == null)
                .sorted(Comparator
                        .comparingInt(GameRegion::getArmyCount)
                        .thenComparingInt(neutral -> myRegions.stream()
                                .mapToInt(mine -> territoryLoader.shortestPath(mine.getTerritoryId(), neutral.getTerritoryId()).size())
                                .min().orElse(Integer.MAX_VALUE)))
                .collect(Collectors.toList());

        if (neutralRegions.isEmpty()) return;

        int movesUsed = 0;

        // Attack adjacent neutral regions in priority order
        for (GameRegion target : neutralRegions) {
            if (movesUsed >= game.getMovementCap()) break;

            List<GameRegion> adjacentOwned = myRegions.stream()
                    .filter(r -> territoryLoader.areAdjacent(r.getTerritoryId(), target.getTerritoryId()))
                    .filter(r -> r.getArmyCount() > 0)
                    .collect(Collectors.toList());

            if (adjacentOwned.isEmpty()) continue;

            int totalAiArmies = adjacentOwned.stream().mapToInt(GameRegion::getArmyCount).sum();

            // Attack if equal or greater armies than the neutral region
            if (totalAiArmies < target.getArmyCount()) continue;

            for (GameRegion attackFrom : adjacentOwned) {
                if (movesUsed >= game.getMovementCap()) break;
                queueMovement(game, aiPlayer, attackFrom, target, attackFrom.getArmyCount());
                movesUsed++;
            }
        }

        // Move non-border armies toward the nearest neutral region
        for (GameRegion region : myRegions) {
            if (movesUsed >= game.getMovementCap()) break;
            if (region.getArmyCount() < 2) continue;

            boolean borderingNeutral = neutralRegions.stream()
                    .anyMatch(n -> territoryLoader.areAdjacent(region.getTerritoryId(), n.getTerritoryId()));
            if (borderingNeutral) continue;

            String nextStep = getStepTowardNeutral(region, neutralRegions);
            if (nextStep == null) continue;

            GameRegion dest = allRegions.stream()
                    .filter(r -> r.getTerritoryId().equals(nextStep))
                    .findFirst().orElse(null);
            if (dest != null && dest.getOwner() != null && dest.getOwner().getId().equals(aiPlayer.getId())) {
                queueMovement(game, aiPlayer, region, dest, region.getArmyCount() - 1);
                movesUsed++;
            }
        }
    }

    private String getStepTowardNeutral(GameRegion region, List<GameRegion> neutralRegions) {
        int minDist = Integer.MAX_VALUE;
        String bestStep = null;
        for (GameRegion target : neutralRegions) {
            List<String> path = territoryLoader.shortestPath(region.getTerritoryId(), target.getTerritoryId());
            if (path.size() >= 2 && path.size() < minDist) {
                minDist = path.size();
                bestStep = path.get(1);
            }
        }
        return bestStep;
    }

    private GamePlayer pickOrMaintainTarget(Game game, GamePlayer aiPlayer, List<GamePlayer> enemies) {
        if (enemies.size() == 1) return enemies.get(0);

        if (aiPlayer.getAiTargetPlayerId() != null) {
            Optional<GamePlayer> current = enemies.stream()
                    .filter(e -> e.getId().equals(aiPlayer.getAiTargetPlayerId()))
                    .findFirst();
            if (current.isPresent() && aiPlayer.getAiAttackTurnCount() < 5) {
                aiPlayer.setAiAttackTurnCount(aiPlayer.getAiAttackTurnCount() + 1);
                return current.get();
            }
            // After 5 turns: re-evaluate
            if (current.isPresent()) {
                if (aiPlayer.getAiRegionsCapturedThisCycle() > 1) {
                    aiPlayer.setAiRegionsCapturedThisCycle(0);
                    aiPlayer.setAiAttackTurnCount(0);
                    return current.get();
                }
                if (aiPlayer.getAiRegionsCapturedThisCycle() == 1 && random.nextInt(100) > 50) {
                    aiPlayer.setAiRegionsCapturedThisCycle(0);
                    aiPlayer.setAiAttackTurnCount(0);
                    return current.get();
                }
            }
        }

        // Pick new target randomly
        GamePlayer newTarget = enemies.get(random.nextInt(enemies.size()));
        aiPlayer.setAiTargetPlayerId(newTarget.getId());
        aiPlayer.setAiAttackTurnCount(1);
        aiPlayer.setAiRegionsCapturedThisCycle(0);
        playerRepository.save(aiPlayer);
        return newTarget;
    }

    private int executeAttacks(Game game, GamePlayer aiPlayer, GamePlayer target,
                                List<GameRegion> allRegions, List<GameRegion> myRegions, int movesUsed) {
        int moves = 0;
        List<GameRegion> targetRegions = allRegions.stream()
                .filter(r -> r.getOwner() != null && r.getOwner().getId().equals(target.getId()))
                .collect(Collectors.toList());

        for (GameRegion targetRegion : targetRegions) {
            if (movesUsed + moves >= game.getMovementCap()) break;

            List<GameRegion> adjacentOwned = myRegions.stream()
                    .filter(r -> territoryLoader.areAdjacent(r.getTerritoryId(), targetRegion.getTerritoryId()))
                    .collect(Collectors.toList());

            if (adjacentOwned.isEmpty()) continue;

            int totalAiArmies = adjacentOwned.stream().mapToInt(GameRegion::getArmyCount).sum();
            int defenderArmies = targetRegion.getArmyCount();

            // Empty tile: send up to 3 armies from the best adjacent region
            if (defenderArmies == 0) {
                GameRegion attackFrom = adjacentOwned.stream()
                        .filter(r -> r.getArmyCount() > 1)
                        .max(Comparator.comparingInt(GameRegion::getArmyCount))
                        .orElse(null);
                if (attackFrom != null) {
                    int toSend = armiesForEmptyCapture(attackFrom);
                    queueMovement(game, aiPlayer, attackFrom, targetRegion, toSend);
                    moves++;
                }
                continue;
            }

            if (totalAiArmies <= defenderArmies) continue;
            if (targetRegion.getFortressLevel() > 0 && totalAiArmies < defenderArmies * 2) continue;

            // Commit armies
            for (GameRegion attackFrom : adjacentOwned) {
                if (movesUsed + moves >= game.getMovementCap()) break;
                if (attackFrom.getArmyCount() == 0) continue;

                boolean allIn = totalAiArmies >= defenderArmies * 3;
                int toCommit = allIn ? attackFrom.getArmyCount() : Math.max(1, attackFrom.getArmyCount() / 2);

                queueMovement(game, aiPlayer, attackFrom, targetRegion, toCommit);
                moves++;
            }
        }
        return moves;
    }

    private int executeNeutralOpportunism(Game game, GamePlayer aiPlayer,
                                           List<GameRegion> allRegions, List<GameRegion> myRegions, int movesUsed) {
        int moves = 0;
        for (GameRegion region : myRegions) {
            if (movesUsed + moves >= game.getMovementCap()) break;
            if (region.getArmyCount() < 2) continue;

            List<GameRegion> weakNeutrals = allRegions.stream()
                    .filter(r -> r.getOwner() == null)
                    .filter(r -> territoryLoader.areAdjacent(region.getTerritoryId(), r.getTerritoryId()))
                    .filter(r -> region.getArmyCount() >= r.getArmyCount() * 4)
                    .collect(Collectors.toList());

            int committed = 0;
            for (GameRegion neutral : weakNeutrals) {
                if (movesUsed + moves >= game.getMovementCap()) break;
                int available = region.getArmyCount() - 1 - committed;
                if (available <= 0) break;
                int toCommit = Math.min(Math.max(1, region.getArmyCount() / 2), available);
                queueMovement(game, aiPlayer, region, neutral, toCommit);
                committed += toCommit;
                moves++;
            }
        }
        return moves;
    }

    private int executeEmptyEnemyOpportunism(Game game, GamePlayer aiPlayer,
                                              List<GameRegion> allRegions, List<GameRegion> myRegions, int movesUsed) {
        int moves = 0;
        for (GameRegion region : myRegions) {
            if (movesUsed + moves >= game.getMovementCap()) break;
            if (region.getArmyCount() < 2) continue;

            List<GameRegion> emptyEnemyTiles = allRegions.stream()
                    .filter(r -> r.getOwner() != null && !r.getOwner().getId().equals(aiPlayer.getId()))
                    .filter(r -> r.getArmyCount() == 0)
                    .filter(r -> territoryLoader.areAdjacent(region.getTerritoryId(), r.getTerritoryId()))
                    .collect(Collectors.toList());

            for (GameRegion empty : emptyEnemyTiles) {
                if (movesUsed + moves >= game.getMovementCap()) break;
                int toSend = armiesForEmptyCapture(region);
                queueMovement(game, aiPlayer, region, empty, toSend);
                moves++;
                break; // one grab per region per turn
            }
        }
        return moves;
    }

    private void assignBorderTask(Game game, GameRegion region, GamePlayer aiPlayer, List<GameRegion> allRegions) {
        int failedAttacks = battleRecordRepository.countFailedAttacksOnRegion(region.getId());
        if (failedAttacks >= 3 && region.getFarmLevel() > 4 && region.getFortressLevel() == 0) {
            region.setCurrentTask(RegionTask.BUILDING);
            region.setConstructionTarget(ConstructionTarget.FORTRESS);
        } else {
            region.setCurrentTask(RegionTask.ARMIES);
        }
    }

    private void assignDevelopmentTask(Game game, GamePlayer aiPlayer, GameRegion region, List<GameRegion> allRegions) {
        // Every 5 turns roll for produce/develop
        boolean shouldDevelop;
        if (game.getTurnNumber() % 5 == 0) {
            shouldDevelop = random.nextInt(100) <= 50;
            aiPlayer.setAiDevRollTurn(game.getTurnNumber());
        } else {
            shouldDevelop = true; // default to develop until next roll
        }

        if (!shouldDevelop) {
            region.setCurrentTask(RegionTask.ARMIES);
            return;
        }

        // Priority 1: loyalty < 50
        if (region.getLoyalty() < 50) {
            region.setCurrentTask(RegionTask.DIPLOMATS);
            return;
        }

        // Priority 2: gold if winter is coming and not enough
        List<GameRegion> myRegions = allRegions.stream()
                .filter(r -> r.getOwner() != null && r.getOwner().getId().equals(aiPlayer.getId()))
                .collect(Collectors.toList());
        int requiredWinter = upkeepService.calculateRequiredGold(myRegions);
        if (requiredWinter > aiPlayer.getGold() * 1.3 / 1.3) {
            region.setCurrentTask(RegionTask.GOLD);
            return;
        }

        // Priority 3: culture upgrade if farm conditions met
        boolean shouldUpgrade = switch (region.getCulture()) {
            case PRIMAL -> region.getFarmLevel() > 2;
            case BASIC -> region.getFarmLevel() > 4;
            case INTERMEDIATE -> region.getFarmLevel() > 8;
            case ADVANCED -> false;
        };
        if (shouldUpgrade) {
            region.setCurrentTask(RegionTask.CULTURE);
            return;
        }

        // Priority 4: farm if below max
        if (region.getFarmLevel() < 12) {
            region.setCurrentTask(RegionTask.BUILDING);
            region.setConstructionTarget(ConstructionTarget.FARM);
            return;
        }

        // Priority 5: fallback
        region.setCurrentTask(RegionTask.ARMIES);
    }

    private boolean isBorderingEnemy(GameRegion region, List<GameRegion> allRegions, GamePlayer aiPlayer) {
        TerritoryLoader.TerritoryInfo info = territoryLoader.get(region.getTerritoryId());
        if (info == null) return false;
        for (String adjId : info.adjacencies()) {
            GameRegion adj = allRegions.stream()
                    .filter(r -> r.getTerritoryId().equals(adjId))
                    .findFirst().orElse(null);
            if (adj != null && adj.getOwner() != null && !adj.getOwner().getId().equals(aiPlayer.getId())) {
                return true;
            }
        }
        return false;
    }

    private String getStepTowardTarget(GameRegion region, GamePlayer target, List<GameRegion> allRegions, GamePlayer aiPlayer) {
        List<GameRegion> targetRegions = allRegions.stream()
                .filter(r -> r.getOwner() != null && r.getOwner().getId().equals(target.getId()))
                .collect(Collectors.toList());
        if (targetRegions.isEmpty()) return null;

        // Find closest target region and first step toward it
        int minDist = Integer.MAX_VALUE;
        String bestStep = null;

        TerritoryLoader.TerritoryInfo info = territoryLoader.get(region.getTerritoryId());
        if (info == null) return null;

        for (GameRegion targetRegion : targetRegions) {
            List<String> path = territoryLoader.shortestPath(region.getTerritoryId(), targetRegion.getTerritoryId());
            if (path.size() >= 2 && path.size() < minDist) {
                minDist = path.size();
                bestStep = path.get(1);
            }
        }
        return bestStep;
    }

    // Send up to 3 armies to claim an empty tile, always keeping at least 1 behind
    private int armiesForEmptyCapture(GameRegion from) {
        return Math.min(3, from.getArmyCount() - 1);
    }

    private void queueMovement(Game game, GamePlayer player, GameRegion from, GameRegion to, int count) {
        if (count <= 0) return;
        ArmyMovement m = new ArmyMovement();
        m.setGame(game);
        m.setPlayer(player);
        m.setFromRegion(from);
        m.setToRegion(to);
        m.setArmyCount(count);
        m.setAttack(to.getOwner() == null || !to.getOwner().getId().equals(player.getId()));
        m.setTurnNumber(game.getTurnNumber());
        movementRepository.save(m);
    }
}
