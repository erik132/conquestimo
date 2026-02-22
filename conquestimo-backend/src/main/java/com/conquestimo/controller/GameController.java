package com.conquestimo.controller;

import com.conquestimo.dto.*;
import com.conquestimo.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public ResponseEntity<List<GameSummaryDto>> listGames() {
        return ResponseEntity.ok(gameService.listGames());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameDetailDto> getGame(@PathVariable Long id) {
        return ResponseEntity.ok(gameService.getGameDetail(id));
    }

    @PostMapping
    public ResponseEntity<GameDetailDto> createGame(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateGameRequest request) {
        return ResponseEntity.ok(gameService.createGame(userDetails.getUsername(), request));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<GameDetailDto> joinGame(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody JoinGameRequest request) {
        return ResponseEntity.ok(gameService.joinGame(userDetails.getUsername(), id, request));
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<Void> leaveGame(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        gameService.leaveGame(userDetails.getUsername(), id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<GameDetailDto> startGame(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(gameService.startGame(userDetails.getUsername(), id));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }
}
