CREATE TABLE army_movement (
    id BIGSERIAL PRIMARY KEY,
    game_id BIGINT NOT NULL REFERENCES game(id) ON DELETE CASCADE,
    player_id BIGINT NOT NULL REFERENCES game_player(id) ON DELETE CASCADE,
    from_region_id BIGINT NOT NULL REFERENCES game_region(id) ON DELETE CASCADE,
    to_region_id BIGINT NOT NULL REFERENCES game_region(id) ON DELETE CASCADE,
    army_count INT NOT NULL,
    is_attack BOOLEAN NOT NULL,
    turn_number INT NOT NULL,
    is_executed BOOLEAN NOT NULL DEFAULT FALSE
);
