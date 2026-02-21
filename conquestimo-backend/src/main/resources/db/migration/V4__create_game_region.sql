CREATE TABLE game_region (
    id BIGSERIAL PRIMARY KEY,
    game_id BIGINT NOT NULL REFERENCES game(id) ON DELETE CASCADE,
    territory_id VARCHAR(50) NOT NULL,
    owner_player_id BIGINT REFERENCES game_player(id) ON DELETE SET NULL,
    army_count INT NOT NULL DEFAULT 0,
    farm_level INT NOT NULL DEFAULT 1,
    fortress_level INT NOT NULL DEFAULT 0,
    culture VARCHAR(20) NOT NULL DEFAULT 'PRIMAL',
    loyalty INT NOT NULL DEFAULT 100,
    current_task VARCHAR(20) NOT NULL DEFAULT 'NONE',
    army_production_progress DECIMAL(10,4) NOT NULL DEFAULT 0,
    construction_target VARCHAR(10),
    construction_progress DECIMAL(10,4) NOT NULL DEFAULT 0,
    culture_upgrade_progress DECIMAL(10,4) NOT NULL DEFAULT 0,
    UNIQUE (game_id, territory_id)
);
