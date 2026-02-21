package com.conquestimo.service;

import com.conquestimo.entity.*;
import com.conquestimo.repository.GamePlayerRepository;
import com.conquestimo.repository.GameRegionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameInitializationService {

    private final GameRegionRepository regionRepository;
    private final GamePlayerRepository playerRepository;
    private final TerritoryLoader territoryLoader;

    public GameInitializationService(GameRegionRepository regionRepository,
                                     GamePlayerRepository playerRepository,
                                     TerritoryLoader territoryLoader) {
        this.regionRepository = regionRepository;
        this.playerRepository = playerRepository;
        this.territoryLoader = territoryLoader;
    }

    @Transactional
    public void initializeGame(Game game) {
        List<TerritoryLoader.TerritoryInfo> allTerritories = territoryLoader.getAll();
        List<GamePlayer> players = playerRepository.findByGameId(game.getId());
        Random random = new Random();

        // Create all region records first
        List<GameRegion> regions = new ArrayList<>();
        for (TerritoryLoader.TerritoryInfo territory : allTerritories) {
            GameRegion region = new GameRegion();
            region.setGame(game);
            region.setTerritoryId(territory.id());
            region.setFarmLevel(1);
            region.setCulture(Culture.PRIMAL);
            region.setLoyalty(100);
            regions.add(region);
        }
        regions = regionRepository.saveAll(regions);

        Map<String, GameRegion> regionByTerritoryId = regions.stream()
                .collect(Collectors.toMap(GameRegion::getTerritoryId, r -> r));

        // Assign spawn regions to players (non-adjacent)
        List<String> allIds = allTerritories.stream()
                .map(TerritoryLoader.TerritoryInfo::id)
                .collect(Collectors.toList());

        List<String> spawnIds = pickNonAdjacentSpawns(allIds, players.size(), random);

        for (int i = 0; i < players.size(); i++) {
            GamePlayer player = players.get(i);
            String spawnId = spawnIds.get(i);
            GameRegion spawnRegion = regionByTerritoryId.get(spawnId);
            spawnRegion.setOwner(player);
            spawnRegion.setFarmLevel(3);
            spawnRegion.setFortressLevel(1);
            spawnRegion.setCulture(Culture.PRIMAL);
            spawnRegion.setLoyalty(100);
            spawnRegion.setArmyCount(0);
        }

        // Set up neutral regions
        Set<String> spawnSet = new HashSet<>(spawnIds);
        List<GameRegion> neutralRegions = regions.stream()
                .filter(r -> !spawnSet.contains(r.getTerritoryId()))
                .collect(Collectors.toList());

        int basicCultureCount = neutralRegions.size() / 3;
        Collections.shuffle(neutralRegions, random);

        for (int i = 0; i < neutralRegions.size(); i++) {
            GameRegion region = neutralRegions.get(i);
            region.setArmyCount(1 + random.nextInt(4));  // 1-4
            region.setFarmLevel(1 + random.nextInt(3));  // 1-3
            if (i < basicCultureCount) {
                region.setCulture(Culture.BASIC);
            }
            region.setLoyalty(100);
        }

        regionRepository.saveAll(regions);

        game.setTurnStartedAt(LocalDateTime.now());
    }

    private List<String> pickNonAdjacentSpawns(List<String> allIds, int count, Random random) {
        List<String> shuffled = new ArrayList<>(allIds);
        int maxAttempts = 1000;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            Collections.shuffle(shuffled, random);
            List<String> chosen = new ArrayList<>();
            for (String id : shuffled) {
                if (chosen.size() == count) break;
                boolean adjacent = chosen.stream().anyMatch(c -> territoryLoader.areAdjacent(c, id));
                if (!adjacent) chosen.add(id);
            }
            if (chosen.size() == count) return chosen;
        }
        // Fallback: just pick the first N (shouldn't happen with 42 territories and max 5 players)
        return shuffled.subList(0, Math.min(count, shuffled.size()));
    }
}
