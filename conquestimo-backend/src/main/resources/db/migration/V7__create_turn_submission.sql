CREATE TABLE turn_submission (
    id BIGSERIAL PRIMARY KEY,
    game_id BIGINT NOT NULL REFERENCES game(id) ON DELETE CASCADE,
    player_id BIGINT NOT NULL REFERENCES game_player(id) ON DELETE CASCADE,
    turn_number INT NOT NULL,
    submitted_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (game_id, player_id, turn_number)
);
