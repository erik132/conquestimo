package com.conquestimo.service;

import com.conquestimo.entity.GameRegion;
import org.springframework.stereotype.Component;

@Component
public class RegionPotentialCalculator {

    public double calculate(GameRegion region) {
        double population = region.getFarmLevel() * 10_000.0;
        double cultureScore = region.getCulture().getScore();
        return (population / 10_000.0) * cultureScore;
    }

    public int goldPerTurn(GameRegion region) {
        double potential = calculate(region);
        return Math.max(1, (int) Math.floor(Math.pow(potential, 1.5)));
    }

    public double armyProductionPerTurn(GameRegion region) {
        return calculate(region) / 2.0;
    }

    public int farmConstructionPoints(int farmLevel) {
        return farmLevel + 6;
    }

    public int fortressConstructionPoints(int fortressLevel) {
        return fortressLevel * 4 + 3;
    }
}
