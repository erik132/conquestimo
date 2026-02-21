package com.conquestimo.controller;

import com.conquestimo.dto.GameRegionDto;
import com.conquestimo.dto.SetTaskRequest;
import com.conquestimo.entity.*;
import com.conquestimo.repository.GamePlayerRepository;
import com.conquestimo.repository.GameRegionRepository;
import com.conquestimo.repository.GameRepository;
import com.conquestimo.service.RegionPotentialCalculator;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/games/{gameId}/regions")
public class RegionController {

    private final GameRepository gameRepository;
    private final GameRegionRepository regionRepository;
    private final GamePlayerRepository playerRepository;
    private final RegionPotentialCalculator potentialCalculator;

    public RegionController(GameRepository gameRepository,
                            GameRegionRepository regionRepository,
                            GamePlayerRepository playerRepository,
                            RegionPotentialCalculator potentialCalculator) {
        this.gameRepository = gameRepository;
        this.regionRepository = regionRepository;
        this.playerRepository = playerRepository;
        this.potentialCalculator = potentialCalculator;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<GameRegionDto>> getRegions(@PathVariable Long gameId) {
        List<GameRegion> regions = regionRepository.findByGameId(gameId);
        List<GameRegionDto> dtos = regions.stream()
                .map(r -> GameRegionDto.from(r, potentialCalculator.calculate(r)))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{regionId}/task")
    @Transactional
    public ResponseEntity<GameRegionDto> setTask(
            @PathVariable Long gameId,
            @PathVariable Long regionId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody SetTaskRequest request) {

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        if (game.getState() != GameState.IN_PROGRESS) {
            throw new IllegalStateException("Game is not in progress");
        }

        GameRegion region = regionRepository.findById(regionId)
                .orElseThrow(() -> new IllegalArgumentException("Region not found"));
        if (!region.getGame().getId().equals(gameId)) {
            throw new IllegalArgumentException("Region does not belong to this game");
        }

        GamePlayer player = playerRepository
                .findByGameIdAndUserUsername(gameId, userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Player not in game"));

        if (region.getOwner() == null || !region.getOwner().getId().equals(player.getId())) {
            throw new IllegalStateException("You do not own this region");
        }

        RegionTask task = RegionTask.valueOf(request.getTask());
        region.setCurrentTask(task);

        if (task == RegionTask.BUILDING) {
            if (request.getConstructionTarget() == null) {
                throw new IllegalArgumentException("Construction target required");
            }
            ConstructionTarget target = ConstructionTarget.valueOf(request.getConstructionTarget());
            region.setConstructionTarget(target);
        }

        regionRepository.save(region);
        return ResponseEntity.ok(GameRegionDto.from(region, potentialCalculator.calculate(region)));
    }
}
