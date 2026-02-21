CREATE TABLE battle_record (
    id BIGSERIAL PRIMARY KEY,
    game_id BIGINT NOT NULL REFERENCES game(id) ON DELETE CASCADE,
    region_id BIGINT NOT NULL REFERENCES game_region(id) ON DELETE CASCADE,
    attacking_player_id BIGINT REFERENCES game_player(id) ON DELETE SET NULL,
    defending_player_id BIGINT REFERENCES game_player(id) ON DELETE SET NULL,
    attacker_won BOOLEAN NOT NULL,
    turn_number INT NOT NULL
);
