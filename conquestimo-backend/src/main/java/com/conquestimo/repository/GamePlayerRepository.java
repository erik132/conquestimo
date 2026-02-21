package com.conquestimo.repository;

import com.conquestimo.entity.GamePlayer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {
    List<GamePlayer> findByGameId(Long gameId);
    Optional<GamePlayer> findByGameIdAndUserId(Long gameId, Long userId);
    Optional<GamePlayer> findByGameIdAndUserUsername(Long gameId, String username);
    boolean existsByGameIdAndUserId(Long gameId, Long userId);
    int countByGameId(Long gameId);
}
