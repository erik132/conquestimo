package com.conquestimo.repository;

import com.conquestimo.entity.ArmyMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArmyMovementRepository extends JpaRepository<ArmyMovement, Long> {
    List<ArmyMovement> findByGameIdAndTurnNumberAndExecutedFalse(Long gameId, int turnNumber);
    List<ArmyMovement> findByGameIdAndPlayerIdAndTurnNumberAndExecutedFalse(Long gameId, Long playerId, int turnNumber);
    int countByGameIdAndPlayerIdAndTurnNumberAndExecutedFalse(Long gameId, Long playerId, int turnNumber);
}
