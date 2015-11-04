# TestUser Schema

# --- !Ups

CREATE TABLE player (
  id SERIAL,
  first_name varchar(255) NOT NULL,
  last_name varchar(255) NOT NULL,
  email varchar(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE league (
  id SERIAL,
  name varchar(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE game_type (
  id SERIAL,
  name varchar(255) NOT NULL,
  description text,
  PRIMARY KEY (id)
);

CREATE TABLE rating (
  id SERIAL,
  elo_rating integer NOT NULL,
  player_id int REFERENCES player (id),
  league_id int REFERENCES league (id),
  game_type_id int REFERENCES game_type (id),
  PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE player;

DROP TABLE league;

DROP TABLE game_type;
