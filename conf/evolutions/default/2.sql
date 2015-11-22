# --- !Ups

CREATE TYPE permission_level AS ENUM ('NONE', 'READ', 'CREATE', 'UPDATE', 'DELETE');

CREATE TABLE app (
  id SERIAL,
  name varchar(255) NOT NULL,
  owner_name varchar(255) NOT NULL,
  description varchar(255) NOT NULL,
  auth_token varchar(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE permission_type (
  type varchar(50) NOT NULL CHECK (upper(type) = type),
  description varchar(255) NOT NULL,
  PRIMARY KEY (type)
);

CREATE TABLE permission (
  id SERIAL,
  permission_level permission_level NOT NULL,
  permission_type varchar(50) REFERENCES permission_type (type),
  auth_token_id integer REFERENCES app (id),
  PRIMARY KEY (id)
);

INSERT INTO permission_type(type, description) values('PLAYER', 'Create, Manage, and Delete Players');
INSERT INTO permission_type(type, description) values('GAME_TYPE', 'Create, Manage, and Delete GameTypes');
INSERT INTO permission_type(type, description) values('RATING', 'Add game results and view ratings');

# --- !Downs

DROP TABLE permission CASCADE;

DROP TABLE permission_type CASCADE;

DROP TABLE auth_token CASCADE;

DROP TYPE permission_level;
