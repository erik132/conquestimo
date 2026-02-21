package com.conquestimo.dto;

import com.conquestimo.entity.GameState;

public class GameSummaryDto {
    private Long id;
    private String name;
    private GameState state;
    private int playerCount;
    private int maxPlayers;
    private boolean hasPassword;

    public GameSummaryDto(Long id, String name, GameState state, int playerCount, int maxPlayers, boolean hasPassword) {
        this.id = id;
        this.name = name;
        this.state = state;
        this.playerCount = playerCount;
        this.maxPlayers = maxPlayers;
        this.hasPassword = hasPassword;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public GameState getState() { return state; }
    public int getPlayerCount() { return playerCount; }
    public int getMaxPlayers() { return maxPlayers; }
    public boolean isHasPassword() { return hasPassword; }
}
