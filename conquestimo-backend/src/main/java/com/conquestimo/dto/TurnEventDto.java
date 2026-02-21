package com.conquestimo.dto;

public class TurnEventDto {
    private String type;
    private Long regionId;
    private String territoryId;
    private Long fromRegionId;
    private String fromTerritoryId;
    private Long toRegionId;
    private String toTerritoryId;
    private String playerName;
    private String attackerName;
    private String defenderName;
    private Integer armyCount;
    private Integer goldAmount;
    private Integer armiesLost;
    private Boolean attackerWon;
    private Boolean playerWon;
    private String newSeason;
    private String newCulture;
    private String buildingType;
    private Integer buildingLevel;

    public static TurnEventDto armyMoved(String fromTId, String toTId, Long fromRId, Long toRId, int count, boolean isAttack) {
        TurnEventDto e = new TurnEventDto();
        e.type = isAttack ? "ATTACK" : "MOVE";
        e.fromTerritoryId = fromTId;
        e.toTerritoryId = toTId;
        e.fromRegionId = fromRId;
        e.toRegionId = toRId;
        e.armyCount = count;
        return e;
    }

    public static TurnEventDto battle(String territoryId, Long regionId, String attacker, String defender, boolean attackerWon) {
        TurnEventDto e = new TurnEventDto();
        e.type = "BATTLE";
        e.territoryId = territoryId;
        e.regionId = regionId;
        e.attackerName = attacker;
        e.defenderName = defender;
        e.attackerWon = attackerWon;
        return e;
    }

    public static TurnEventDto rebellion(String territoryId, Long regionId, int armyCount, boolean playerWon) {
        TurnEventDto e = new TurnEventDto();
        e.type = "REBELLION";
        e.territoryId = territoryId;
        e.regionId = regionId;
        e.armyCount = armyCount;
        e.playerWon = playerWon;
        return e;
    }

    public static TurnEventDto upkeep(String playerName, int goldPaid, int armiesLost) {
        TurnEventDto e = new TurnEventDto();
        e.type = "UPKEEP";
        e.playerName = playerName;
        e.goldAmount = goldPaid;
        e.armiesLost = armiesLost;
        return e;
    }

    public static TurnEventDto buildingCompleted(String territoryId, Long regionId, String buildingType, int level) {
        TurnEventDto e = new TurnEventDto();
        e.type = "BUILDING_COMPLETED";
        e.territoryId = territoryId;
        e.regionId = regionId;
        e.buildingType = buildingType;
        e.buildingLevel = level;
        return e;
    }

    public static TurnEventDto cultureUpgraded(String territoryId, Long regionId, String newCulture) {
        TurnEventDto e = new TurnEventDto();
        e.type = "CULTURE_UPGRADED";
        e.territoryId = territoryId;
        e.regionId = regionId;
        e.newCulture = newCulture;
        return e;
    }

    public static TurnEventDto seasonChanged(String newSeason) {
        TurnEventDto e = new TurnEventDto();
        e.type = "SEASON_CHANGED";
        e.newSeason = newSeason;
        return e;
    }

    public String getType() { return type; }
    public Long getRegionId() { return regionId; }
    public String getTerritoryId() { return territoryId; }
    public Long getFromRegionId() { return fromRegionId; }
    public String getFromTerritoryId() { return fromTerritoryId; }
    public Long getToRegionId() { return toRegionId; }
    public String getToTerritoryId() { return toTerritoryId; }
    public String getPlayerName() { return playerName; }
    public String getAttackerName() { return attackerName; }
    public String getDefenderName() { return defenderName; }
    public Integer getArmyCount() { return armyCount; }
    public Integer getGoldAmount() { return goldAmount; }
    public Integer getArmiesLost() { return armiesLost; }
    public Boolean getAttackerWon() { return attackerWon; }
    public Boolean getPlayerWon() { return playerWon; }
    public String getNewSeason() { return newSeason; }
    public String getNewCulture() { return newCulture; }
    public String getBuildingType() { return buildingType; }
    public Integer getBuildingLevel() { return buildingLevel; }
}
