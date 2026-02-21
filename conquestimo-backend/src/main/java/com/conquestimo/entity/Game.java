package com.conquestimo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;

@Entity
@Table(name = "game")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameState state = GameState.LOBBY;

    @Column(name = "turn_number", nullable = false)
    private int turnNumber = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Season season = Season.SPRING;

    @Column(name = "turn_timer_seconds", nullable = false)
    private int turnTimerSeconds = 120;

    @Column(name = "movement_cap", nullable = false)
    private int movementCap = 5;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "max_players", nullable = false)
    private int maxPlayers = 5;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "turn_started_at")
    private LocalDateTime turnStartedAt;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GamePlayer> players = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }

    public int getTurnNumber() { return turnNumber; }
    public void setTurnNumber(int turnNumber) { this.turnNumber = turnNumber; }

    public Season getSeason() { return season; }
    public void setSeason(Season season) { this.season = season; }

    public int getTurnTimerSeconds() { return turnTimerSeconds; }
    public void setTurnTimerSeconds(int turnTimerSeconds) { this.turnTimerSeconds = turnTimerSeconds; }

    public int getMovementCap() { return movementCap; }
    public void setMovementCap(int movementCap) { this.movementCap = movementCap; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }

    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getTurnStartedAt() { return turnStartedAt; }
    public void setTurnStartedAt(LocalDateTime turnStartedAt) { this.turnStartedAt = turnStartedAt; }

    public List<GamePlayer> getPlayers() { return players; }
    public void setPlayers(List<GamePlayer> players) { this.players = players; }
}
