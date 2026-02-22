package com.conquestimo.service;

import com.conquestimo.dto.GameDetailDto;
import com.conquestimo.dto.GameRegionDto;
import com.conquestimo.dto.TurnEventDto;
import com.conquestimo.dto.TurnResolutionResultDto;
import com.conquestimo.entity.*;
import com.conquestimo.repository.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.conquestimo.entity.TurnSubmission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TurnResolutionService {

    private final GameRepository gameRepository;
    private final GamePlayerRepository playerRepository;
    private final GameRegionRepository regionRepository;
    private final ArmyMovementRepository movementRepository;
    private final BattleService battleService;
    private final LoyaltyService loyaltyService;
    private final UpkeepService upkeepService;
    private final GameEndService gameEndService;
    private final AiService aiService;
    private final RegionPotentialCalculator potentialCalculator;
    private final SimpMessagingTemplate messagingTemplate;
    private final GameService gameService;
    private final TurnSubmissionRepository submissionRepository;

    public TurnResolutionService(GameRepository gameRepository,
                                 GamePlayerRepository playerRepository,
                                 GameRegionRepository regionRepository,
                                 ArmyMovementRepository movementRepository,
                                 BattleService battleService,
                                 LoyaltyService loyaltyService,
                                 UpkeepService upkeepService,
                                 GameEndService gameEndService,
                                 AiService aiService,
                                 RegionPotentialCalculator potentialCalculator,
                                 SimpMessagingTemplate messagingTemplate,
                                 GameService gameService,
                                 TurnSubmissionRepository submissionRepository) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.regionRepository = regionRepository;
        this.movementRepository = movementRepository;
        this.battleService = battleService;
        this.loyaltyService = loyaltyService;
        this.upkeepService = upkeepService;
        this.gameEndService = gameEndService;
        this.aiService = aiService;
        this.potentialCalculator = potentialCalculator;
        this.messagingTemplate = messagingTemplate;
        this.gameService = gameService;
        this.submissionRepository = submissionRepository;
    }

    @Transactional
    public TurnResolutionResultDto resolveTurn(Game game) {
        List<TurnEventDto> events = new ArrayList<>();
        List<GameRegion> allRegions = regionRepository.findByGameId(game.getId());
        List<GamePlayer> allPlayers = playerRepository.findByGameId(game.getId());

        // 1. Army production (ARMIES task)
        events.addAll(processArmyProduction(allRegions));

        // 2. Gold collection (GOLD task)
        processGoldCollection(allRegions, allPlayers);

        // 3. Loyalty growth (culture natural + DIPLOMATS bonus)
        loyaltyService.applyLoyaltyGrowth(allRegions);

        // 4. Winter upkeep
        if (game.getSeason() == Season.WINTER) {
            List<TurnEventDto> upkeepEvents = upkeepService.processWinterUpkeep(allPlayers, allRegions);
            events.addAll(upkeepEvents);
        }

        // 5. Rebellions
        List<TurnEventDto> rebellionEvents = loyaltyService.processRebellions(allRegions, game);
        events.addAll(rebellionEvents);

        // 6. Building / culture construction
        events.addAll(processConstruction(allRegions));

        // 7. Execute army movements and battles
        events.addAll(executeMovements(game, allRegions, allPlayers));

        // Persist region/player state after all processing
        regionRepository.saveAll(allRegions);
        playerRepository.saveAll(allPlayers);

        // 8. Advance season/turn
        Season nextSeason = game.getSeason().next();
        game.setSeason(nextSeason);
        game.setTurnNumber(game.getTurnNumber() + 1);
        events.add(TurnEventDto.seasonChanged(nextSeason.name()));

        // 9. Check game end
        // Re-fetch regions after saves
        List<GameRegion> freshRegions = regionRepository.findByGameId(game.getId());
        boolean ended = gameEndService.checkAndHandleGameEnd(game, freshRegions);
        if (!ended) {
            game.setTurnStartedAt(java.time.LocalDateTime.now());
        }
        game = gameRepository.save(game);

        if (!ended) {
            // 10. AI players take their turns
            List<GamePlayer> activePlayers = playerRepository.findByGameId(game.getId()).stream()
                    .filter(p -> !p.isEliminated())
                    .collect(Collectors.toList());
            for (GamePlayer player : activePlayers) {
                if (player.isAi()) {
                    aiService.processTurn(game, player);
                    TurnSubmission aiSubmission = new TurnSubmission();
                    aiSubmission.setGame(game);
                    aiSubmission.setPlayer(player);
                    aiSubmission.setTurnNumber(game.getTurnNumber());
                    submissionRepository.save(aiSubmission);
                }
            }
        }

        // 11. Build result DTOs
        List<GameRegion> finalRegions = regionRepository.findByGameId(game.getId());
        List<GameRegionDto> regionDtos = finalRegions.stream()
                .map(r -> GameRegionDto.from(r, potentialCalculator.calculate(r)))
                .collect(Collectors.toList());

        GameDetailDto gameDto = gameService.getGameDetail(game.getId());

        TurnResolutionResultDto result = new TurnResolutionResultDto(events, gameDto, regionDtos);

        // 12. Broadcast to all players
        messagingTemplate.convertAndSend("/topic/game/" + game.getId() + "/resolution", result);
        messagingTemplate.convertAndSend("/topic/game/" + game.getId(), gameDto);

        return result;
    }

    private List<TurnEventDto> processArmyProduction(List<GameRegion> regions) {
        List<TurnEventDto> events = new ArrayList<>();
        for (GameRegion region : regions) {
            if (region.getOwner() == null) continue;
            if (region.getCurrentTask() != RegionTask.ARMIES) continue;

            double rate = potentialCalculator.armyProductionPerTurn(region);
            double newProgress = region.getArmyProductionProgress() + rate;
            int produced = (int) Math.floor(newProgress);
            region.setArmyProductionProgress(newProgress - produced);
            if (produced > 0) {
                region.setArmyCount(region.getArmyCount() + produced);
            }
        }
        return events;
    }

    private void processGoldCollection(List<GameRegion> regions, List<GamePlayer> players) {
        Map<Long, GamePlayer> playerMap = players.stream()
                .collect(Collectors.toMap(GamePlayer::getId, p -> p));

        for (GameRegion region : regions) {
            if (region.getOwner() == null) continue;
            if (region.getCurrentTask() != RegionTask.GOLD) continue;

            int gold = potentialCalculator.goldPerTurn(region);
            GamePlayer owner = playerMap.get(region.getOwner().getId());
            if (owner != null) {
                owner.setGold(owner.getGold() + gold);
            }
        }
    }

    private List<TurnEventDto> processConstruction(List<GameRegion> regions) {
        List<TurnEventDto> events = new ArrayList<>();
        for (GameRegion region : regions) {
            if (region.getOwner() == null) continue;

            if (region.getCurrentTask() == RegionTask.BUILDING && region.getConstructionTarget() != null) {
                double points = potentialCalculator.calculate(region);
                double newProgress = region.getConstructionProgress() + points;
                region.setConstructionProgress(newProgress);

                ConstructionTarget target = region.getConstructionTarget();
                if (target == ConstructionTarget.FARM) {
                    int required = potentialCalculator.farmConstructionPoints(region.getFarmLevel());
                    if (newProgress >= required) {
                        region.setFarmLevel(region.getFarmLevel() + 1);
                        region.setConstructionProgress(0);
                        region.setConstructionTarget(null);
                        region.setCurrentTask(RegionTask.NONE);
                        events.add(TurnEventDto.buildingCompleted(region.getTerritoryId(), region.getId(), "FARM", region.getFarmLevel()));
                    }
                } else if (target == ConstructionTarget.FORTRESS) {
                    int required = potentialCalculator.fortressConstructionPoints(region.getFortressLevel());
                    if (newProgress >= required) {
                        region.setFortressLevel(region.getFortressLevel() + 1);
                        region.setConstructionProgress(0);
                        region.setConstructionTarget(null);
                        region.setCurrentTask(RegionTask.NONE);
                        events.add(TurnEventDto.buildingCompleted(region.getTerritoryId(), region.getId(), "FORTRESS", region.getFortressLevel()));
                    }
                }

            } else if (region.getCurrentTask() == RegionTask.CULTURE) {
                if (region.getCulture().isMaxLevel()) {
                    region.setCurrentTask(RegionTask.NONE);
                    continue;
                }
                double points = potentialCalculator.calculate(region);
                double newProgress = region.getCultureUpgradeProgress() + points;
                region.setCultureUpgradeProgress(newProgress);

                int required = region.getCulture().upgradePointCost();
                if (newProgress >= required) {
                    Culture upgraded = region.getCulture().next();
                    region.setCulture(upgraded);
                    region.setCultureUpgradeProgress(0);
                    region.setCurrentTask(RegionTask.NONE);
                    region.setLoyalty(region.getLoyalty() + 30);
                    events.add(TurnEventDto.cultureUpgraded(region.getTerritoryId(), region.getId(), upgraded.name()));
                }
            }
        }
        return events;
    }

    private List<TurnEventDto> executeMovements(Game game, List<GameRegion> allRegions, List<GamePlayer> allPlayers) {
        List<TurnEventDto> events = new ArrayList<>();
        List<ArmyMovement> movements = movementRepository.findByGameIdAndTurnNumberAndExecutedFalse(
                game.getId(), game.getTurnNumber());

        // Build a mutable region map for quick lookup
        Map<Long, GameRegion> regionMap = allRegions.stream()
                .collect(Collectors.toMap(GameRegion::getId, r -> r));
        Map<Long, GamePlayer> playerMap = allPlayers.stream()
                .collect(Collectors.toMap(GamePlayer::getId, p -> p));

        for (ArmyMovement movement : movements) {
            GameRegion from = regionMap.get(movement.getFromRegion().getId());
            GameRegion to = regionMap.get(movement.getToRegion().getId());

            if (from == null || to == null) continue;

            int armies = movement.getArmyCount();

            // Clamp to available armies (may have changed due to previous movements)
            armies = Math.min(armies, from.getArmyCount());
            if (armies <= 0) {
                movement.setExecuted(true);
                movementRepository.save(movement);
                continue;
            }

            if (!movement.isAttack()) {
                // Friendly move
                from.setArmyCount(from.getArmyCount() - armies);
                to.setArmyCount(to.getArmyCount() + armies);
                events.add(TurnEventDto.armyMoved(
                        from.getTerritoryId(), to.getTerritoryId(),
                        from.getId(), to.getId(), armies, false));
            } else {
                // Attack
                from.setArmyCount(from.getArmyCount() - armies);

                String attackerName = resolvePlayerName(movement.getPlayer());
                String defenderName = to.getOwner() != null ? resolvePlayerName(to.getOwner()) : "Neutral";

                BattleService.BattleResult result = battleService.resolveBattle(armies, to);

                GamePlayer attackingPlayer = playerMap.get(movement.getPlayer().getId());
                GamePlayer defendingPlayer = to.getOwner() != null ? playerMap.get(to.getOwner().getId()) : null;

                battleService.recordBattle(game, to, attackingPlayer, defendingPlayer, result.attackerWon());

                events.add(TurnEventDto.armyMoved(
                        from.getTerritoryId(), to.getTerritoryId(),
                        from.getId(), to.getId(), armies, true));
                events.add(TurnEventDto.battle(
                        to.getTerritoryId(), to.getId(),
                        attackerName, defenderName, result.attackerWon()));

                if (result.attackerWon()) {
                    int survivors = armies - result.attackerArmiesLost();
                    to.setOwner(attackingPlayer);
                    to.setArmyCount(Math.max(0, survivors));
                    to.setCurrentTask(RegionTask.ARMIES);
                    loyaltyService.halveOnCapture(to);

                    // Track captured regions for AI cycle
                    if (attackingPlayer != null && attackingPlayer.isAi()) {
                        attackingPlayer.setAiRegionsCapturedThisCycle(
                                attackingPlayer.getAiRegionsCapturedThisCycle() + 1);
                    }
                } else {
                    int defenderSurvivors = to.getArmyCount() - result.defenderArmiesLost();
                    to.setArmyCount(Math.max(0, defenderSurvivors));
                }
            }

            movement.setExecuted(true);
            movementRepository.save(movement);
        }

        return events;
    }

    private String resolvePlayerName(GamePlayer player) {
        if (player == null) return "Unknown";
        if (player.isAi()) return player.getAiName();
        return player.getUser() != null ? player.getUser().getUsername() : "Unknown";
    }
}
