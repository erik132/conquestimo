package com.conquestimo.entity;

public enum Season {
    SPRING, SUMMER, AUTUMN, WINTER;

    public Season next() {
        Season[] values = Season.values();
        return values[(this.ordinal() + 1) % values.length];
    }
}
