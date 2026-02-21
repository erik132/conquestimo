package com.conquestimo.repository;

import com.conquestimo.entity.Game;
import com.conquestimo.entity.GameState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByStateNot(GameState state);
    List<Game> findByState(GameState state);
}
