CREATE TABLE test1 (
	"id" BIGSERIAL PRIMARY KEY,
	name CHAR(2) NOT NULL UNIQUE DEFAULT 'GA'
-- COMMENT 'char'
-- COLUMN_FORMAT fixed
-- STORAGE DISK

);
COMMENT ON COLUMN test1.name IS 'char';
CREATE TABLE "testIndexKey" (
	id INTEGER,
	name CHAR(2)
);
CREATE INDEX "idIndex" ON "testIndexKey" USING btree(id);
CREATE INDEX "nameIndex" ON "testIndexKey" USING hash(name);
CREATE TABLE "testIndexKey" (
	id INTEGER,
	name CHAR(2)
);
CREATE INDEX "idIndex" ON "testIndexKey" USING btree(id);
CREATE INDEX "nameIndex" ON "testIndexKey" USING hash(name);
CREATE TABLE "testPrimaryUniqueKey" (
	id INTEGER,
	name CHAR(2),
	parent_id INTEGER,
	CONSTRAINT pk_id PRIMARY KEY (id)
-- USING BTREE
,
	col3 INTEGER,
	UNIQUE
-- UNIQUE_CONSTRAINT
(name),
	col4 INTEGER,
	CONSTRAINT fk_pid FOREIGN KEY (parent_id) REFERENCES parent (id) MATCH SIMPLE ON DELETE CASCADE,
	col5 INTEGER,
	CONSTRAINT ck_con CHECK (id > 0)
--  NOT ENFORCED

);
CREATE TABLE "testIndexKey" (
	id INTEGER,
	name CHAR(2),
	t TEXT,
	col4 INTEGER
);
CREATE INDEX id_index ON "testIndexKey" USING btree(id);
CREATE INDEX name_key ON "testIndexKey" USING hash(name);
CREATE INDEX test_index ON "testIndexKey"(t);