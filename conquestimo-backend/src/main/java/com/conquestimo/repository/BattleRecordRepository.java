package com.conquestimo.repository;

import com.conquestimo.entity.BattleRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BattleRecordRepository extends JpaRepository<BattleRecord, Long> {

    @Query("SELECT COUNT(b) FROM BattleRecord b WHERE b.region.id = :regionId AND b.attackerWon = false")
    int countFailedAttacksOnRegion(Long regionId);
}
