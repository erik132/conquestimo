package com.conquestimo.entity;

public enum Culture {
    PRIMAL(1.0, 2),
    BASIC(1.5, 4),
    INTERMEDIATE(2.0, 6),
    ADVANCED(3.0, 8);

    private final double score;
    private final int loyaltyPerTurn;

    Culture(double score, int loyaltyPerTurn) {
        this.score = score;
        this.loyaltyPerTurn = loyaltyPerTurn;
    }

    public double getScore() { return score; }
    public int getLoyaltyPerTurn() { return loyaltyPerTurn; }

    public Culture next() {
        return switch (this) {
            case PRIMAL -> BASIC;
            case BASIC -> INTERMEDIATE;
            case INTERMEDIATE -> ADVANCED;
            case ADVANCED -> ADVANCED;
        };
    }

    public int upgradePointCost() {
        return switch (this) {
            case PRIMAL -> 10;   // cost to reach BASIC
            case BASIC -> 17;    // cost to reach INTERMEDIATE
            case INTERMEDIATE -> 28; // cost to reach ADVANCED
            case ADVANCED -> Integer.MAX_VALUE;
        };
    }

    public boolean isMaxLevel() { return this == ADVANCED; }
}
