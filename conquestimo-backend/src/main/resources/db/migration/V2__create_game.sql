CREATE TABLE game (
    id BIGSERIAL PRIMARY KEY,
    state VARCHAR(20) NOT NULL DEFAULT 'LOBBY',
    turn_number INT NOT NULL DEFAULT 1,
    season VARCHAR(10) NOT NULL DEFAULT 'SPRING',
    turn_timer_seconds INT NOT NULL DEFAULT 120,
    movement_cap INT NOT NULL DEFAULT 5,
    password_hash VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
