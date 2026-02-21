CREATE TABLE game_player (
    id BIGSERIAL PRIMARY KEY,
    game_id BIGINT NOT NULL REFERENCES game(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES app_user(id) ON DELETE SET NULL,
    is_ai BOOLEAN NOT NULL DEFAULT FALSE,
    gold INT NOT NULL DEFAULT 10,
    color VARCHAR(20) NOT NULL,
    is_eliminated BOOLEAN NOT NULL DEFAULT FALSE,
    ai_target_player_id BIGINT,
    ai_attack_turn_count INT NOT NULL DEFAULT 0,
    ai_regions_captured_this_cycle INT NOT NULL DEFAULT 0,
    ai_dev_roll_turn INT NOT NULL DEFAULT 0
);
