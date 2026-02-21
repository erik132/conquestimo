package com.conquestimo.service;

import com.conquestimo.dto.ArmyMovementDto;
import com.conquestimo.dto.QueueMovementRequest;
import com.conquestimo.entity.*;
import com.conquestimo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovementService {

    private final ArmyMovementRepository movementRepository;
    private final GameRegionRepository regionRepository;
    private final GamePlayerRepository playerRepository;
    private final TurnSubmissionRepository submissionRepository;
    private final TerritoryLoader territoryLoader;

    public MovementService(ArmyMovementRepository movementRepository,
                           GameRegionRepository regionRepository,
                           GamePlayerRepository playerRepository,
                           TurnSubmissionRepository submissionRepository,
                           TerritoryLoader territoryLoader) {
        this.movementRepository = movementRepository;
        this.regionRepository = regionRepository;
        this.playerRepository = playerRepository;
        this.submissionRepository = submissionRepository;
        this.territoryLoader = territoryLoader;
    }

    public List<ArmyMovementDto> getPlayerMovements(Long gameId, Long playerId, int turnNumber) {
        return movementRepository.findByGameIdAndPlayerIdAndTurnNumberAndExecutedFalse(gameId, playerId, turnNumber)
                .stream().map(ArmyMovementDto::from).collect(Collectors.toList());
    }

    @Transactional
    public ArmyMovementDto queueMovement(Game game, GamePlayer player, QueueMovementRequest request) {
        // Cannot queue after turn ended
        if (submissionRepository.existsByGameIdAndPlayerIdAndTurnNumber(game.getId(), player.getId(), game.getTurnNumber())) {
            throw new IllegalStateException("Turn already ended");
        }

        GameRegion from = regionRepository.findById(request.getFromRegionId())
                .orElseThrow(() -> new IllegalArgumentException("From region not found"));
        GameRegion to = regionRepository.findById(request.getToRegionId())
                .orElseThrow(() -> new IllegalArgumentException("To region not found"));

        // Validations
        if (!from.getGame().getId().equals(game.getId()) || !to.getGame().getId().equals(game.getId())) {
            throw new IllegalArgumentException("Region does not belong to this game");
        }
        if (from.getOwner() == null || !from.getOwner().getId().equals(player.getId())) {
            throw new IllegalStateException("You do not own the source region");
        }
        if (!territoryLoader.areAdjacent(from.getTerritoryId(), to.getTerritoryId())) {
            throw new IllegalArgumentException("Regions are not adjacent");
        }

        // Count reserved armies (already committed in other movements from same region this turn)
        int alreadyCommitted = movementRepository
                .findByGameIdAndPlayerIdAndTurnNumberAndExecutedFalse(game.getId(), player.getId(), game.getTurnNumber())
                .stream()
                .filter(m -> m.getFromRegion().getId().equals(from.getId()))
                .mapToInt(ArmyMovement::getArmyCount)
                .sum();

        if (from.getArmyCount() - alreadyCommitted < request.getArmyCount()) {
            throw new IllegalStateException("Not enough available armies in region");
        }

        // Check movement cap
        int usedMoves = movementRepository.countByGameIdAndPlayerIdAndTurnNumberAndExecutedFalse(
                game.getId(), player.getId(), game.getTurnNumber());
        if (usedMoves >= game.getMovementCap()) {
            throw new IllegalStateException("Movement cap reached");
        }

        boolean isAttack = to.getOwner() == null || !to.getOwner().getId().equals(player.getId());

        ArmyMovement movement = new ArmyMovement();
        movement.setGame(game);
        movement.setPlayer(player);
        movement.setFromRegion(from);
        movement.setToRegion(to);
        movement.setArmyCount(request.getArmyCount());
        movement.setAttack(isAttack);
        movement.setTurnNumber(game.getTurnNumber());
        movementRepository.save(movement);

        return ArmyMovementDto.from(movement);
    }

    @Transactional
    public void cancelMovement(Long movementId, GamePlayer player) {
        ArmyMovement movement = movementRepository.findById(movementId)
                .orElseThrow(() -> new IllegalArgumentException("Movement not found"));
        if (!movement.getPlayer().getId().equals(player.getId())) {
            throw new IllegalStateException("Not your movement");
        }
        if (movement.isExecuted()) {
            throw new IllegalStateException("Movement already executed");
        }
        if (submissionRepository.existsByGameIdAndPlayerIdAndTurnNumber(
                movement.getGame().getId(), player.getId(), movement.getTurnNumber())) {
            throw new IllegalStateException("Cannot cancel after ending turn");
        }
        movementRepository.delete(movement);
    }
}
