#!/usr/bin/env bash

read -d '' COMMANDS << EOF || true
DELETE FROM player;

INSERT INTO player(id, first_name, last_name, email) values(1, 'test1first', 'test1last', 'test1@test.com');
INSERT INTO player(id, first_name, last_name, email) values(2, 'test2first', 'test2last', 'test2@test.com');
INSERT INTO player(id, first_name, last_name, email) values(3, 'test3first', 'test3last', 'test3@test.com');
INSERT INTO player(id, first_name, last_name, email) values(4, 'test4first', 'test4last', 'test4@test.com');
INSERT INTO player(id, first_name, last_name, email) values(5, 'test5first', 'test5last', 'test5@test.com');

DELETE FROM game_type;
INSERT INTO game_type(id, name, description) values(1, 'testGameType1', 'Test Game Type 1');
INSERT INTO game_type(id, name, description) values(2, 'testGameType2', 'Test Game Type 2');
INSERT INTO game_type(id, name, description) values(3, 'testGameType3', 'Test Game Type 3');

DELETE FROM rating;
INSERT INTO rating(elo_rating, player_id, game_type_id) values(1100, 1, 1);
INSERT INTO rating(elo_rating, player_id, game_type_id) values(950, 2, 1);
INSERT INTO rating(elo_rating, player_id, game_type_id) values(1322, 3, 1);
INSERT INTO rating(elo_rating, player_id, game_type_id) values(655, 4, 1);
INSERT INTO rating(elo_rating, player_id, game_type_id) values(1011, 5, 1);

INSERT INTO rating(elo_rating, player_id, game_type_id) values(1011, 2, 2);
INSERT INTO rating(elo_rating, player_id, game_type_id) values(989, 4, 2);

INSERT INTO rating(elo_rating, player_id, game_type_id) values(1010, 1, 3);
INSERT INTO rating(elo_rating, player_id, game_type_id) values(990, 2, 3);
INSERT INTO rating(elo_rating, player_id, game_type_id) values(1121, 4, 3);
INSERT INTO rating(elo_rating, player_id, game_type_id) values(879, 5, 3);

DELETE FROM app;
INSERT INTO app(id, name, owner_name, description, auth_token) values(1, 'TestApp1', 'Owner1', 'Test App 1', 'e>gC<3mif5zV>2oMHpaihH<dPvFVq`FSLThtxOMUEo@lM`Y??<SXzs^SW/NsjVAY');
INSERT INTO app(id, name, owner_name, description, auth_token) values(2, 'TestApp2', 'Owner2', 'Test App 2', '1ax?R=fJ?SgzWbC@RI9NLmA=HF_`BOmt;1@ZgxpqkT`S_^rk>CkYgZ?9[y;MkMxu');

DELETE FROM permission;
INSERT INTO permission(permission_level, permission_type, app_id) values('READ', 'PLAYER', 1);
INSERT INTO permission(permission_level, permission_type, app_id) values('READ', 'GAME_TYPE', 1);
INSERT INTO permission(permission_level, permission_type, app_id) values('UPDATE', 'RATING', 1);

INSERT INTO permission(permission_level, permission_type, app_id) values('DELETE', 'PLAYER', 2);
INSERT INTO permission(permission_level, permission_type, app_id) values('DELETE', 'GAME_TYPE', 2);
INSERT INTO permission(permission_level, permission_type, app_id) values('UPDATE', 'RATING', 2);
EOF

psql -h $1 -p $2 -d elophant -U elophantuser -c "${COMMANDS}"
