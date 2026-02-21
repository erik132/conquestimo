package com.conquestimo.dto;

import com.conquestimo.entity.ArmyMovement;

public class ArmyMovementDto {
    private Long id;
    private Long fromRegionId;
    private String fromTerritoryId;
    private Long toRegionId;
    private String toTerritoryId;
    private int armyCount;
    private boolean attack;

    public static ArmyMovementDto from(ArmyMovement m) {
        ArmyMovementDto dto = new ArmyMovementDto();
        dto.id = m.getId();
        dto.fromRegionId = m.getFromRegion().getId();
        dto.fromTerritoryId = m.getFromRegion().getTerritoryId();
        dto.toRegionId = m.getToRegion().getId();
        dto.toTerritoryId = m.getToRegion().getTerritoryId();
        dto.armyCount = m.getArmyCount();
        dto.attack = m.isAttack();
        return dto;
    }

    public Long getId() { return id; }
    public Long getFromRegionId() { return fromRegionId; }
    public String getFromTerritoryId() { return fromTerritoryId; }
    public Long getToRegionId() { return toRegionId; }
    public String getToTerritoryId() { return toTerritoryId; }
    public int getArmyCount() { return armyCount; }
    public boolean isAttack() { return attack; }
}
