package com.conquestimo.dto;

public class PlayerDto {
    private Long id;
    private String username;
    private boolean ai;
    private String color;
    private int gold;
    private boolean eliminated;

    public PlayerDto(Long id, String username, boolean ai, String color, int gold, boolean eliminated) {
        this.id = id;
        this.username = username;
        this.ai = ai;
        this.color = color;
        this.gold = gold;
        this.eliminated = eliminated;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public boolean isAi() { return ai; }
    public String getColor() { return color; }
    public int getGold() { return gold; }
    public boolean isEliminated() { return eliminated; }
}
