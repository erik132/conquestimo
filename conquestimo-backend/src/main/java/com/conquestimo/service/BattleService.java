package com.conquestimo.service;

import com.conquestimo.entity.*;
import com.conquestimo.repository.BattleRecordRepository;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class BattleService {

    private final BattleRecordRepository battleRecordRepository;
    private final Random random = new Random();

    public BattleService(BattleRecordRepository battleRecordRepository) {
        this.battleRecordRepository = battleRecordRepository;
    }

    public record BattleResult(boolean attackerWon, int attackerArmiesLost, int defenderArmiesLost) {}

    /**
     * Resolves a battle. Modifies armyCount on both sides.
     * Returns whether the attacker won.
     */
    public BattleResult resolveBattle(int attackerArmies, GameRegion defenderRegion) {
        int attacker = attackerArmies;
        int defender = defenderRegion.getArmyCount();
        int fortressLimit = defenderRegion.getFortressArmyLimit();

        int attackerLost = 0;
        int defenderLost = 0;

        while (attacker > 0 && defender > 0) {
            int coveredDefenders = Math.min(defender, fortressLimit);
            int uncoveredDefenders = defender - coveredDefenders;

            // Uncovered defenders roll first
            for (int i = 0; i < uncoveredDefenders && attacker > 0 && defender > 0; i++) {
                int attackRoll = random.nextInt(21);
                int defRoll = random.nextInt(21);
                if (attackRoll < defRoll) {
                    attacker--;
                    attackerLost++;
                } else if (defRoll < attackRoll) {
                    defender--;
                    defenderLost++;
                } else {
                    // tie: defender rerolls
                    int reroll = random.nextInt(21);
                    if (attackRoll < reroll) {
                        attacker--;
                        attackerLost++;
                    } else {
                        defender--;
                        defenderLost++;
                    }
                }
            }

            // Covered defenders (fortress bonus: 2 dice, take higher)
            for (int i = 0; i < coveredDefenders && attacker > 0 && defender > 0; i++) {
                int attackRoll = random.nextInt(21);
                int defRoll = Math.max(random.nextInt(21), random.nextInt(21));
                if (attackRoll < defRoll) {
                    attacker--;
                    attackerLost++;
                } else if (defRoll < attackRoll) {
                    defender--;
                    defenderLost++;
                } else {
                    int reroll = Math.max(random.nextInt(21), random.nextInt(21));
                    if (attackRoll < reroll) {
                        attacker--;
                        attackerLost++;
                    } else {
                        defender--;
                        defenderLost++;
                    }
                }
            }
        }

        return new BattleResult(attacker > 0, attackerLost, defenderLost);
    }

    public void recordBattle(Game game, GameRegion region, GamePlayer attacker, GamePlayer defender, boolean attackerWon) {
        BattleRecord record = new BattleRecord();
        record.setGame(game);
        record.setRegion(region);
        record.setAttackingPlayer(attacker);
        record.setDefendingPlayer(defender);
        record.setAttackerWon(attackerWon);
        record.setTurnNumber(game.getTurnNumber());
        battleRecordRepository.save(record);
    }

    public int countFailedAttacksOnRegion(Long regionId) {
        return battleRecordRepository.countFailedAttacksOnRegion(regionId);
    }
}
