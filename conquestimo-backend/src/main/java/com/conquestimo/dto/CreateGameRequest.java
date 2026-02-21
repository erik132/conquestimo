package com.conquestimo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateGameRequest {

    @NotBlank
    @Size(min = 1, max = 100)
    private String name;

    @Min(30) @Max(600)
    private int timerSeconds = 120;

    @Min(1) @Max(10)
    private int movementCap = 5;

    @Min(2) @Max(5)
    private int maxPlayers = 5;

    private String password;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getTimerSeconds() { return timerSeconds; }
    public void setTimerSeconds(int timerSeconds) { this.timerSeconds = timerSeconds; }

    public int getMovementCap() { return movementCap; }
    public void setMovementCap(int movementCap) { this.movementCap = movementCap; }

    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
