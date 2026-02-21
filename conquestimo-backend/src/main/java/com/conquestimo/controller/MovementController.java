package com.conquestimo.controller;

import com.conquestimo.dto.ArmyMovementDto;
import com.conquestimo.dto.QueueMovementRequest;
import com.conquestimo.entity.*;
import com.conquestimo.repository.GamePlayerRepository;
import com.conquestimo.repository.GameRepository;
import com.conquestimo.service.MovementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games/{gameId}/movements")
public class MovementController {

    private final GameRepository gameRepository;
    private final GamePlayerRepository playerRepository;
    private final MovementService movementService;

    public MovementController(GameRepository gameRepository,
                              GamePlayerRepository playerRepository,
                              MovementService movementService) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.movementService = movementService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<ArmyMovementDto>> getMovements(
            @PathVariable Long gameId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Game game = findGame(gameId);
        GamePlayer player = findPlayer(gameId, userDetails.getUsername());
        List<ArmyMovementDto> movements = movementService.getPlayerMovements(
                gameId, player.getId(), game.getTurnNumber());
        return ResponseEntity.ok(movements);
    }

    @PostMapping
    public ResponseEntity<ArmyMovementDto> queueMovement(
            @PathVariable Long gameId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody QueueMovementRequest request) {
        Game game = findGame(gameId);
        GamePlayer player = findPlayer(gameId, userDetails.getUsername());
        ArmyMovementDto dto = movementService.queueMovement(game, player, request);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{movementId}")
    public ResponseEntity<Void> cancelMovement(
            @PathVariable Long gameId,
            @PathVariable Long movementId,
            @AuthenticationPrincipal UserDetails userDetails) {
        GamePlayer player = findPlayer(gameId, userDetails.getUsername());
        movementService.cancelMovement(movementId, player);
        return ResponseEntity.ok().build();
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
