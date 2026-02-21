package com.conquestimo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class QueueMovementRequest {
    @NotNull
    private Long fromRegionId;
    @NotNull
    private Long toRegionId;
    @Min(1)
    private int armyCount;

    public Long getFromRegionId() { return fromRegionId; }
    public void setFromRegionId(Long fromRegionId) { this.fromRegionId = fromRegionId; }

    public Long getToRegionId() { return toRegionId; }
    public void setToRegionId(Long toRegionId) { this.toRegionId = toRegionId; }

    public int getArmyCount() { return armyCount; }
    public void setArmyCount(int armyCount) { this.armyCount = armyCount; }
}
