package com.conquestimo.dto;

public class UpkeepPreviewDto {
    private int requiredGold;
    private int currentGold;

    public UpkeepPreviewDto(int requiredGold, int currentGold) {
        this.requiredGold = requiredGold;
        this.currentGold = currentGold;
    }

    public int getRequiredGold() { return requiredGold; }
    public int getCurrentGold() { return currentGold; }
}
