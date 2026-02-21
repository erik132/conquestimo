package com.conquestimo.controller;

import com.conquestimo.dto.UpkeepPreviewDto;
import com.conquestimo.entity.*;
import com.conquestimo.repository.GamePlayerRepository;
import com.conquestimo.repository.GameRepository;
import com.conquestimo.service.TurnService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/games/{gameId}/turn")
public class TurnController {

    private final GameRepository gameRepository;
    private final GamePlayerRepository playerRepository;
    private final TurnService turnService;

    public TurnController(GameRepository gameRepository,
                          GamePlayerRepository playerRepository,
                          TurnService turnService) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.turnService = turnService;
    }

    @PostMapping("/end")
    public ResponseEntity<Map<String, String>> endTurn(
            @PathVariable Long gameId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Game game = findGame(gameId);
        GamePlayer player = findPlayer(gameId, userDetails.getUsername());
        turnService.endTurn(game, player);
        return ResponseEntity.ok(Map.of("status", "submitted"));
    }

    @GetMapping("/upkeep-preview")
    public ResponseEntity<UpkeepPreviewDto> getUpkeepPreview(
            @PathVariable Long gameId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Game game = findGame(gameId);
        GamePlayer player = findPlayer(gameId, userDetails.getUsername());
        return ResponseEntity.ok(turnService.getUpkeepPreview(game, player));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getTurnStatus(
            @PathVariable Long gameId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Game game = findGame(gameId);
        GamePlayer player = findPlayer(gameId, userDetails.getUsername());
        boolean submitted = turnService.hasSubmitted(game, player);
        return ResponseEntity.ok(Map.of(
                "turnNumber", game.getTurnNumber(),
                "season", game.getSeason().name(),
                "submitted", submitted
        ));
    }

    private Game findGame(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
    }

    private GamePlayer findPlayer(Long gameId, String username) {
        return playerRepository.findByGameIdAndUserUsername(gameId, username)
                .orElseThrow(() -> new IllegalStateException("Player not in game"));
    }
}
