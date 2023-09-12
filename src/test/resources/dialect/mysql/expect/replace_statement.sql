DROP TABLE replace_test;
CREATE TABLE replace_test (
	id INTEGER NOT NULL,
	name VARCHAR(20) DEFAULT NULL,
	key1 VARCHAR(20) DEFAULT NULL,
	PRIMARY KEY (id)
);
REPLACE INTO replace_test
VALUES (1, 'aaaaa', 'aaaaa');
REPLACE INTO replace_test
VALUES (6, 'bbbbb', 'bbbbb');
REPLACE INTO replace_test
VALUES (15, 'ccccc', 'ccccc');
REPLACE INTO replace_test (id, name, key1)
VALUES (1, 'aaaaa', 'bbbbb');
REPLACE INTO replace_test
VALUES (3, DEFAULT, 'ccccc'), (4, 'ddddd', 'ddddd'), (5, 'eeeee', 'eeeee');
REPLACE INTO replace_test
	SELECT *
	FROM replace_test;