package com.conquestimo.dto;

import com.conquestimo.entity.*;

public class GameRegionDto {
    private Long id;
    private String territoryId;
    private Long ownerPlayerId;
    private String ownerUsername;
    private String ownerColor;
    private int armyCount;
    private int farmLevel;
    private int fortressLevel;
    private String culture;
    private int loyalty;
    private String currentTask;
    private String constructionTarget;
    private double constructionProgress;
    private double cultureUpgradeProgress;
    private double armyProductionProgress;
    private double regionPotential;
    private int fortressArmyLimit;

    public static GameRegionDto from(GameRegion r, double potential) {
        GameRegionDto dto = new GameRegionDto();
        dto.id = r.getId();
        dto.territoryId = r.getTerritoryId();
        dto.armyCount = r.getArmyCount();
        dto.farmLevel = r.getFarmLevel();
        dto.fortressLevel = r.getFortressLevel();
        dto.culture = r.getCulture().name();
        dto.loyalty = r.getLoyalty();
        dto.currentTask = r.getCurrentTask().name();
        dto.constructionTarget = r.getConstructionTarget() != null ? r.getConstructionTarget().name() : null;
        dto.constructionProgress = r.getConstructionProgress();
        dto.cultureUpgradeProgress = r.getCultureUpgradeProgress();
        dto.armyProductionProgress = r.getArmyProductionProgress();
        dto.regionPotential = potential;
        dto.fortressArmyLimit = r.getFortressArmyLimit();
        if (r.getOwner() != null) {
            dto.ownerPlayerId = r.getOwner().getId();
            dto.ownerUsername = r.getOwner().isAi() ? r.getOwner().getAiName() : (r.getOwner().getUser() != null ? r.getOwner().getUser().getUsername() : "Unknown");
            dto.ownerColor = r.getOwner().getColor();
        }
        return dto;
    }

    public Long getId() { return id; }
    public String getTerritoryId() { return territoryId; }
    public Long getOwnerPlayerId() { return ownerPlayerId; }
    public String getOwnerUsername() { return ownerUsername; }
    public String getOwnerColor() { return ownerColor; }
    public int getArmyCount() { return armyCount; }
    public int getFarmLevel() { return farmLevel; }
    public int getFortressLevel() { return fortressLevel; }
    public String getCulture() { return culture; }
    public int getLoyalty() { return loyalty; }
    public String getCurrentTask() { return currentTask; }
    public String getConstructionTarget() { return constructionTarget; }
    public double getConstructionProgress() { return constructionProgress; }
    public double getCultureUpgradeProgress() { return cultureUpgradeProgress; }
    public double getArmyProductionProgress() { return armyProductionProgress; }
    public double getRegionPotential() { return regionPotential; }
    public int getFortressArmyLimit() { return fortressArmyLimit; }
}
