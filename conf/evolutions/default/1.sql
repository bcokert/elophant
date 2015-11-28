# --- !Ups

CREATE TABLE player (
  id SERIAL,
  first_name varchar(255) NOT NULL,
  last_name varchar(255) NOT NULL,
  email varchar(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE INDEX player_id_index ON player (id);

CREATE TABLE game_type (
  id SERIAL,
  name varchar(255) NOT NULL,
  description text,
  PRIMARY KEY (id)
);

CREATE INDEX game_type_id_index ON game_type (id);

CREATE TABLE rating (
  id SERIAL,
  elo_rating integer NOT NULL,
  player_id int REFERENCES player (id),
  game_type_id int REFERENCES game_type (id),
  PRIMARY KEY (id)
);

CREATE INDEX rating_player_game_type_ids_index ON rating (player_id, game_type_id);

# --- !Downs

DROP TABLE rating CASCADE;

DROP TABLE player CASCADE;

DROP TABLE game_type CASCADE;

DROP INDEX IF EXISTS player_id_index CASCADE;

DROP INDEX IF EXISTS game_type_id_index CASCADE;

DROP INDEX IF EXISTS rating_player_game_type_ids_index CASCADE;
