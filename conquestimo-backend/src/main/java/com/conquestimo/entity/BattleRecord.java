package com.conquestimo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "battle_record")
public class BattleRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private GameRegion region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attacking_player_id")
    private GamePlayer attackingPlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defending_player_id")
    private GamePlayer defendingPlayer;

    @Column(name = "attacker_won", nullable = false)
    private boolean attackerWon;

    @Column(name = "turn_number", nullable = false)
    private int turnNumber;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }

    public GameRegion getRegion() { return region; }
    public void setRegion(GameRegion region) { this.region = region; }

    public GamePlayer getAttackingPlayer() { return attackingPlayer; }
    public void setAttackingPlayer(GamePlayer attackingPlayer) { this.attackingPlayer = attackingPlayer; }

    public GamePlayer getDefendingPlayer() { return defendingPlayer; }
    public void setDefendingPlayer(GamePlayer defendingPlayer) { this.defendingPlayer = defendingPlayer; }

    public boolean isAttackerWon() { return attackerWon; }
    public void setAttackerWon(boolean attackerWon) { this.attackerWon = attackerWon; }

    public int getTurnNumber() { return turnNumber; }
    public void setTurnNumber(int turnNumber) { this.turnNumber = turnNumber; }
}
