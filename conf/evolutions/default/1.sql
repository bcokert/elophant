# TestUser Schema

# --- !Ups

CREATE TABLE test_user (
  id SERIAL,
  email varchar(255) NOT NULL,
  name varchar(255) NOT NULL,
  PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE test_user;
