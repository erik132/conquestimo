package com.conquestimo.repository;

import com.conquestimo.entity.GamePlayer;
import com.conquestimo.entity.GameRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GameRegionRepository extends JpaRepository<GameRegion, Long> {
    List<GameRegion> findByGameId(Long gameId);
    List<GameRegion> findByGameIdAndOwner(Long gameId, GamePlayer owner);
    List<GameRegion> findByGameIdAndOwnerIsNull(Long gameId);
    Optional<GameRegion> findByGameIdAndTerritoryId(Long gameId, String territoryId);

    @Query("SELECT COUNT(r) FROM GameRegion r WHERE r.game.id = :gameId AND r.owner.id = :playerId")
    int countByGameIdAndOwnerId(Long gameId, Long playerId);
}
