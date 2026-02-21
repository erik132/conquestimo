package com.conquestimo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "army_movement")
public class ArmyMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private GamePlayer player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_region_id", nullable = false)
    private GameRegion fromRegion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_region_id", nullable = false)
    private GameRegion toRegion;

    @Column(name = "army_count", nullable = false)
    private int armyCount;

    @Column(name = "is_attack", nullable = false)
    private boolean attack;

    @Column(name = "turn_number", nullable = false)
    private int turnNumber;

    @Column(name = "is_executed", nullable = false)
    private boolean executed = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }

    public GamePlayer getPlayer() { return player; }
    public void setPlayer(GamePlayer player) { this.player = player; }

    public GameRegion getFromRegion() { return fromRegion; }
    public void setFromRegion(GameRegion fromRegion) { this.fromRegion = fromRegion; }

    public GameRegion getToRegion() { return toRegion; }
    public void setToRegion(GameRegion toRegion) { this.toRegion = toRegion; }

    public int getArmyCount() { return armyCount; }
    public void setArmyCount(int armyCount) { this.armyCount = armyCount; }

    public boolean isAttack() { return attack; }
    public void setAttack(boolean attack) { this.attack = attack; }

    public int getTurnNumber() { return turnNumber; }
    public void setTurnNumber(int turnNumber) { this.turnNumber = turnNumber; }

    public boolean isExecuted() { return executed; }
    public void setExecuted(boolean executed) { this.executed = executed; }
}
