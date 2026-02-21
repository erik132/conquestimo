package com.conquestimo.repository;

import com.conquestimo.entity.TurnSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TurnSubmissionRepository extends JpaRepository<TurnSubmission, Long> {
    boolean existsByGameIdAndPlayerIdAndTurnNumber(Long gameId, Long playerId, int turnNumber);
    int countByGameIdAndTurnNumber(Long gameId, int turnNumber);
}
