-- Reference schema for the UNO persistence layer.
-- Hibernate generates this automatically from the JPA entities
-- (hibernate.hbm2ddl.auto=update); this file documents the resulting tables.
-- Dialect: H2.

CREATE TABLE players (
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE games (
    id            BIGINT    NOT NULL AUTO_INCREMENT,
    played_at     TIMESTAMP NOT NULL,
    rounds_played INTEGER   NOT NULL,
    winner_id     BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_games_winner FOREIGN KEY (winner_id) REFERENCES players (id)
);

CREATE TABLE rounds (
    id        BIGINT  NOT NULL AUTO_INCREMENT,
    game_id   BIGINT  NOT NULL,
    round_no  INTEGER NOT NULL,
    winner_id BIGINT,
    points    INTEGER NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_rounds_game   FOREIGN KEY (game_id)   REFERENCES games (id),
    CONSTRAINT fk_rounds_winner FOREIGN KEY (winner_id) REFERENCES players (id)
);

CREATE TABLE scores (
    id        BIGINT  NOT NULL AUTO_INCREMENT,
    game_id   BIGINT  NOT NULL,
    player_id BIGINT  NOT NULL,
    points    INTEGER NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_scores_game   FOREIGN KEY (game_id)   REFERENCES games (id),
    CONSTRAINT fk_scores_player FOREIGN KEY (player_id) REFERENCES players (id)
);
