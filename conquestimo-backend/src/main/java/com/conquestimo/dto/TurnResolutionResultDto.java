package com.conquestimo.dto;

import java.util.List;

public class TurnResolutionResultDto {
    private List<TurnEventDto> events;
    private GameDetailDto updatedGame;
    private List<GameRegionDto> updatedRegions;

    public TurnResolutionResultDto(List<TurnEventDto> events, GameDetailDto updatedGame, List<GameRegionDto> updatedRegions) {
        this.events = events;
        this.updatedGame = updatedGame;
        this.updatedRegions = updatedRegions;
    }

    public List<TurnEventDto> getEvents() { return events; }
    public GameDetailDto getUpdatedGame() { return updatedGame; }
    public List<GameRegionDto> getUpdatedRegions() { return updatedRegions; }
}
