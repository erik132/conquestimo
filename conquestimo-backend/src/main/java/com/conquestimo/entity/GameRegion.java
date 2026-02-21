package com.conquestimo.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "game_region")
public class GameRegion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "territory_id", nullable = false, length = 50)
    private String territoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_player_id")
    private GamePlayer owner;

    @Column(name = "army_count", nullable = false)
    private int armyCount = 0;

    @Column(name = "farm_level", nullable = false)
    private int farmLevel = 1;

    @Column(name = "fortress_level", nullable = false)
    private int fortressLevel = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Culture culture = Culture.PRIMAL;

    @Column(nullable = false)
    private int loyalty = 100;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_task", nullable = false)
    private RegionTask currentTask = RegionTask.NONE;

    @Column(name = "army_production_progress", nullable = false)
    private BigDecimal armyProductionProgress = BigDecimal.valueOf(0.0);

    @Enumerated(EnumType.STRING)
    @Column(name = "construction_target")
    private ConstructionTarget constructionTarget;

    @Column(name = "construction_progress", nullable = false)
    private BigDecimal constructionProgress = BigDecimal.valueOf(0.0);

    @Column(name = "culture_upgrade_progress", nullable = false)
    private BigDecimal cultureUpgradeProgress = BigDecimal.valueOf(0.0);

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }

    public String getTerritoryId() { return territoryId; }
    public void setTerritoryId(String territoryId) { this.territoryId = territoryId; }

    public GamePlayer getOwner() { return owner; }
    public void setOwner(GamePlayer owner) { this.owner = owner; }

    public int getArmyCount() { return armyCount; }
    public void setArmyCount(int armyCount) { this.armyCount = armyCount; }

    public int getFarmLevel() { return farmLevel; }
    public void setFarmLevel(int farmLevel) { this.farmLevel = farmLevel; }

    public int getFortressLevel() { return fortressLevel; }
    public void setFortressLevel(int fortressLevel) { this.fortressLevel = fortressLevel; }

    public Culture getCulture() { return culture; }
    public void setCulture(Culture culture) { this.culture = culture; }

    public int getLoyalty() { return loyalty; }
    public void setLoyalty(int loyalty) { this.loyalty = Math.min(100, Math.max(0, loyalty)); }

    public RegionTask getCurrentTask() { return currentTask; }
    public void setCurrentTask(RegionTask currentTask) { this.currentTask = currentTask; }

    public double getArmyProductionProgress() { return armyProductionProgress.doubleValue(); }
    public void setArmyProductionProgress(double armyProductionProgress) { this.armyProductionProgress = BigDecimal.valueOf(armyProductionProgress); }

    public ConstructionTarget getConstructionTarget() { return constructionTarget; }
    public void setConstructionTarget(ConstructionTarget constructionTarget) { this.constructionTarget = constructionTarget; }

    public double getConstructionProgress() { return constructionProgress.doubleValue(); }
    public void setConstructionProgress(double constructionProgress) { this.constructionProgress = BigDecimal.valueOf(constructionProgress); }

    public double getCultureUpgradeProgress() { return cultureUpgradeProgress.doubleValue(); }
    public void setCultureUpgradeProgress(double cultureUpgradeProgress) { this.cultureUpgradeProgress = BigDecimal.valueOf(cultureUpgradeProgress); }

    public int getFortressArmyLimit() { return fortressLevel * 5; }
}
