package com.conquestimo.dto;

import com.conquestimo.entity.GameState;
import com.conquestimo.entity.Season;

import java.time.LocalDateTime;
import java.util.List;

public class GameDetailDto {
    private Long id;
    private String name;
    private GameState state;
    private Season season;
    private int turnNumber;
    private int turnTimerSeconds;
    private int movementCap;
    private int maxPlayers;
    private boolean hasPassword;
    private String createdByUsername;
    private List<PlayerDto> players;
    private LocalDateTime turnStartedAt;

    public GameDetailDto(Long id, String name, GameState state, Season season, int turnNumber,
                         int turnTimerSeconds, int movementCap, int maxPlayers,
                         boolean hasPassword, String createdByUsername, List<PlayerDto> players,
                         LocalDateTime turnStartedAt) {
        this.id = id;
        this.name = name;
        this.state = state;
        this.season = season;
        this.turnNumber = turnNumber;
        this.turnTimerSeconds = turnTimerSeconds;
        this.movementCap = movementCap;
        this.maxPlayers = maxPlayers;
        this.hasPassword = hasPassword;
        this.createdByUsername = createdByUsername;
        this.players = players;
        this.turnStartedAt = turnStartedAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public GameState getState() { return state; }
    public Season getSeason() { return season; }
    public int getTurnNumber() { return turnNumber; }
    public int getTurnTimerSeconds() { return turnTimerSeconds; }
    public int getMovementCap() { return movementCap; }
    public int getMaxPlayers() { return maxPlayers; }
    public boolean isHasPassword() { return hasPassword; }
    public String getCreatedByUsername() { return createdByUsername; }
    public List<PlayerDto> getPlayers() { return players; }
    public LocalDateTime getTurnStartedAt() { return turnStartedAt; }
}
