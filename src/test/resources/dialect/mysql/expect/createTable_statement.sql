CREATE TABLE testlike10 (LIKE testlike2 INCLUDING DEFAULTS INCLUDING CONSTRAINTS INCLUDING INDEXES INCLUDING STORAGE);
CREATE TABLE t1 (
	col1 INTEGER,
	col2 CHAR(5)
)
PARTITION BY HASH (col1) (
	PARTITION p1,
	PARTITION p2
);
-- err
-- err
CREATE TABLE t4 (
	col1 INTEGER,
	col2 CHAR(5),
	col3 DATE
)
PARTITION BY HASH
-- ALGORITHM=2
(col3)
-- PARTITIONS 2
 (
	PARTITION p1
		TABLESPACE ts1,
	PARTITION p2
		TABLESPACE ts1
);
-- err
CREATE TABLE t6 (
	a INTEGER NOT NULL,
	b INTEGER NOT NULL
)
PARTITION BY RANGE (a, b) (
	PARTITION p0 VALUES LESS THAN (10, 5),
	PARTITION p1 VALUES LESS THAN (20, 10),
	PARTITION p2 VALUES LESS THAN (50, MAXVALUE),
	PARTITION p3 VALUES LESS THAN (65, MAXVALUE),
	PARTITION p4 VALUES LESS THAN (MAXVALUE, MAXVALUE)
);
-- err
-- err
CREATE TABLE client_firms (
	id INTEGER,
	name VARCHAR(35)
)
PARTITION BY LIST (id) (
	PARTITION r0 VALUES (1, 5, 9, 13, 17, 21),
	PARTITION r1 VALUES (2, 6, 10, 14, 18, 22),
	PARTITION r2 VALUES (3, 7, 11, 15, 19, 23),
	PARTITION r3 VALUES (4, 8, 12, 16, 20, 24)
);
CREATE TABLE t9 (
	col1 INTEGER,
	col2 CHAR(5),
	col3 DATE
)
PARTITION BY HASH
-- ALGORITHM=2
(col3) (
	PARTITION p1
		TABLESPACE ts1
--  COMMENT 'p1'
,
	PARTITION p2
		TABLESPACE ts1
--  COMMENT 'p2'

);
CREATE TABLE testTableOptions (
	id INTEGER,
	name CHAR(2)
) TABLESPACE ts1
-- PASSWORD = 'STRING'
-- AUTO_INCREMENT = 1
-- AVG_ROW_LENGTH = 1
-- CHARACTER SET = 'UTF8'
-- CHECKSUM = 0
-- COMPRESSION = 'NONE'
-- CONNECTION = 'CONNECT_STRING'
-- DATA DIRECTORY = 'ABSOLUTE PATH TO DIRECTORY'
-- DELAY_KEY_WRITE = 0
-- ENCRYPTION = 'Y'
-- ENGINE = 'ENGINE_NAME'
-- INSERT_METHOD = NO
-- KEY_BLOCK_SIZE = 1
-- MAX_ROWS = 1
-- MIN_ROWS = VALUE
-- PACK_KEYS = 0
-- ROW_FORMAT = DEFAULT
-- STATS_AUTO_RECALC = DEFAULT
-- STATS_PERSISTENT = DEFAULT
-- STATS_SAMPLE_PAGES = 1
-- COMMENT 'STRING'
;
CREATE TABLE t10 (
	col1 INTEGER PRIMARY KEY,
	CONSTRAINT constraint_name FOREIGN KEY (col1) REFERENCES table2 (col1) MATCH PARTIAL ON UPDATE SET NULL,
	CONSTRAINT constraint_name1 PRIMARY KEY (col1),
	UNIQUE
-- INDEXNAME2
(col1)
-- USING BTREE
,

-- FULLTEXT INDEX()
CONSTRAINT W_CONSTR_KEY2 CHECK (col1 > 0
		AND col2 IS NOT NULL),
	col3 CHAR(1)
) TABLESPACE h1
-- COMPRESSION = 'NONE'
;
CREATE TABLE t11 (
	col1 INTEGER PRIMARY KEY NOT NULL DEFAULT 1,
	col2 BIGSERIAL
-- COLLATE UTF8_BIN
 UNIQUE
-- COMMENT 'string'
-- COLUMN_FORMAT fixed
-- STORAGE DISK
,
	col3 INTEGER
-- GENERATED ALWAYS AS (CONCAT(FIRST_NAME, ' ', LAST_NAME))
-- VIRTUAL

);
-- hash(methodInvoke)
-- subPartition name
-- improper number of columns
CREATE TABLE t15 (
	id INTEGER,
	purchased INTEGER
)
-- ENGINE = MYISAM

PARTITION BY RANGE (purchased)
SUBPARTITION BY HASH (purchased) (
	PARTITION p0 VALUES LESS THAN (1990) (
		SUBPARTITION s0
-- DATA DIRECTORY '/disk0/data'
-- INDEX DIRECTORY '/disk0/idx'

			TABLESPACE ts1
--  COMMENT 'ts1'
 TABLESPACE ts1,
		SUBPARTITION s1
-- DATA DIRECTORY '/disk1/data'
-- INDEX DIRECTORY '/disk1/idx'

			TABLESPACE ts1 TABLESPACE ts1
	),
	PARTITION p1 VALUES LESS THAN (2000) (
		SUBPARTITION s2
-- DATA DIRECTORY '/disk2/data'
-- INDEX DIRECTORY '/disk2/idx'

			TABLESPACE ts1 TABLESPACE ts1,
		SUBPARTITION s3
-- DATA DIRECTORY '/disk3/data'
-- INDEX DIRECTORY '/disk3/idx'

			TABLESPACE ts1 TABLESPACE ts1
	),
	PARTITION p2 VALUES LESS THAN MAXVALUE (
		SUBPARTITION s4
-- DATA DIRECTORY '/disk4/data'
-- INDEX DIRECTORY '/disk4/idx'

			TABLESPACE ts1 TABLESPACE ts1,
		SUBPARTITION s5
-- DATA DIRECTORY '/disk5/data'
-- INDEX DIRECTORY '/disk5/idx'

			TABLESPACE ts1 TABLESPACE ts1
	)
);
