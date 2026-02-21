package com.conquestimo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "game_player")
public class GamePlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(name = "is_ai", nullable = false)
    private boolean ai = false;

    @Column(nullable = false)
    private int gold = 10;

    @Column(nullable = false, length = 20)
    private String color;

    @Column(name = "is_eliminated", nullable = false)
    private boolean eliminated = false;

    @Column(name = "ai_target_player_id")
    private Long aiTargetPlayerId;

    @Column(name = "ai_attack_turn_count", nullable = false)
    private int aiAttackTurnCount = 0;

    @Column(name = "ai_regions_captured_this_cycle", nullable = false)
    private int aiRegionsCapturedThisCycle = 0;

    @Column(name = "ai_dev_roll_turn", nullable = false)
    private int aiDevRollTurn = 0;

    @Column(name = "ai_name", length = 50)
    private String aiName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }

    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }

    public boolean isAi() { return ai; }
    public void setAi(boolean ai) { this.ai = ai; }

    public int getGold() { return gold; }
    public void setGold(int gold) { this.gold = gold; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public boolean isEliminated() { return eliminated; }
    public void setEliminated(boolean eliminated) { this.eliminated = eliminated; }

    public Long getAiTargetPlayerId() { return aiTargetPlayerId; }
    public void setAiTargetPlayerId(Long aiTargetPlayerId) { this.aiTargetPlayerId = aiTargetPlayerId; }

    public int getAiAttackTurnCount() { return aiAttackTurnCount; }
    public void setAiAttackTurnCount(int aiAttackTurnCount) { this.aiAttackTurnCount = aiAttackTurnCount; }

    public int getAiRegionsCapturedThisCycle() { return aiRegionsCapturedThisCycle; }
    public void setAiRegionsCapturedThisCycle(int aiRegionsCapturedThisCycle) { this.aiRegionsCapturedThisCycle = aiRegionsCapturedThisCycle; }

    public int getAiDevRollTurn() { return aiDevRollTurn; }
    public void setAiDevRollTurn(int aiDevRollTurn) { this.aiDevRollTurn = aiDevRollTurn; }

    public String getAiName() { return aiName; }
    public void setAiName(String aiName) { this.aiName = aiName; }
}
