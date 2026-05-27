# openGauss-tools-sql-translator

## Introduction

openGauss-tools-sql-translator is a translator written in Java that converts MySQL syntax to openGauss syntax. It is implemented based on Druid 1.2.8. It uses Druid's access rules for ASTs, inherits MySQLOutPutVisitor, and overloads its visit method. It modifies the access results of MySQL statement ASTs and outputs statements in openGauss syntax.

## Compilation Procedure

* Installation environment: Java, Maven, and Git
* Source database: MySQL 5.7 Destination database: openGauss 3.0.0
* Packaging command: mvn package

## Compatibility between MySQL and openGauss

The following table lists the SQL statements translated based on the comparison between MySQL 5.7 official documents and OpenGauss 3.0.0 official documents.

| SQL Statements                                                     | Translation       |
| ------------------------------------------------------------ | --------------- |
| [ALTER EVENT Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-event.html), [ALTER INSTANCE Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-instance.html), [ALTER LOGFILE GROUP Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-logfile-group.html), [CREATE EVENT Statement](https://dev.mysql.com/doc/refman/5.7/en/create-event.html), [CREATE LOGFILE GROUP Statement](https://dev.mysql.com/doc/refman/5.7/en/create-logfile-group.html), [DROP EVENT Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-event.html), [DROP LOGFILE GROUP Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-logfile-group.html), [HANDLER Statement](https://dev.mysql.com/doc/refman/5.7/en/handler.html), [REPLACE Statement](https://dev.mysql.com/doc/refman/5.7/en/replace.html), [Replication Statements](https://dev.mysql.com/doc/refman/5.7/en/sql-replication-statements.html), [SET PASSWORD Statement](https://dev.mysql.com/doc/refman/5.7/en/set-password.html), [CHECK TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/check-table.html), [CHECKSUM TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/checksum-table.html), [OPTIMIZE TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/optimize-table.html), [REPAIR TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/repair-table.html), [Plugin and Loadable Function Statements](https://dev.mysql.com/doc/refman/5.7/en/component-statements.html), [SET NAMES Statement](https://dev.mysql.com/doc/refman/5.7/en/set-names.html), [SET CHARACTER Statement](https://dev.mysql.com/doc/refman/5.7/en/set-character-set.html), [SHOW Statements](https://dev.mysql.com/doc/refman/5.7/en/show.html), [BINLOG Statement](https://dev.mysql.com/doc/refman/5.7/en/binlog.html), [CACHE INDEX Statement](https://dev.mysql.com/doc/refman/5.7/en/cache-index.html), [FLUSH Statement](https://dev.mysql.com/doc/refman/5.7/en/flush.html), [KILL Statement](https://dev.mysql.com/doc/refman/5.7/en/kill.html), [LOAD INDEX INTO CACHE Statement](https://dev.mysql.com/doc/refman/5.7/en/load-index.html), [DESCRIBE Statement](https://dev.mysql.com/doc/refman/5.7/en/describe.html), [HELP Statement](https://dev.mysql.com/doc/refman/5.7/en/help.html)| Not supported by openGauss|
| [SHUTDOWN Statement](https://dev.mysql.com/doc/refman/5.7/en/shutdown.html), [DO Statement](https://dev.mysql.com/doc/refman/5.7/en/do.html), [ITERATE Statement](https://dev.mysql.com/doc/refman/5.7/en/iterate.html)| Not supported by Druid    |
| [ALTER DATABASE Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-database.html), [ALTER SERVER Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-server.html), [ALTER TABLESPACE Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-tablespace.html), [CREATE SERVER Statement](https://dev.mysql.com/doc/refman/5.7/en/create-server.html), [CREATE TABLESPACE Statement](https://dev.mysql.com/doc/refman/5.7/en/create-tablespace.html), [Prepared Statements](https://dev.mysql.com/doc/refman/5.7/en/sql-prepared-statements.html), [ANALYZE TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/analyze-table.html), [RESET Statement](https://dev.mysql.com/doc/refman/5.7/en/reset.html), [EXPLAIN Statement](https://dev.mysql.com/doc/refman/5.7/en/explain.html)| Incompatible       |
| [ALTER TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-table.html), [ALTER VIEW Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-view.html), [CREATE DATABASE Statement](https://dev.mysql.com/doc/refman/5.7/en/create-database.html), [CREATE FUNCTION Statement](https://dev.mysql.com/doc/refman/5.7/en/create-function.html), [CREATE INDEX Statement](https://dev.mysql.com/doc/refman/5.7/en/create-index.html), [CREATE PROCEDURE Statements](https://dev.mysql.com/doc/refman/5.7/en/create-procedure.html), [CREATE TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/create-table.html), [CREATE TRIGGER Statement](https://dev.mysql.com/doc/refman/5.7/en/create-trigger.html), [CREATE VIEW Statement](https://dev.mysql.com/doc/refman/5.7/en/create-view.html), [DROP DATABASE Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-database.html), [DROP INDEX Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-index.html), [DROP TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-table.html), [DROP TABLESPACE Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-tablespace.html), [DELETE Statement](https://dev.mysql.com/doc/refman/5.7/en/delete.html), [INSERT Statement](https://dev.mysql.com/doc/refman/5.7/en/insert.html), [SELECT Statement](https://dev.mysql.com/doc/refman/5.7/en/select.html), [Subqueries](https://dev.mysql.com/doc/refman/5.7/en/subqueries.html), [UPDATE Statement](https://dev.mysql.com/doc/refman/5.7/en/update.html), [START TRANSACTION, COMMIT, and ROLLBACK Statements](https://dev.mysql.com/doc/refman/5.7/en/commit.html), [SET TRANSACTION Statement](https://dev.mysql.com/doc/refman/5.7/en/set-transaction.html), [ALTER USER Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-user.html), [CREATE USER Statement](https://dev.mysql.com/doc/refman/5.7/en/create-user.html), [RENAME USER Statement](https://dev.mysql.com/doc/refman/5.7/en/rename-user.html), [GRANT Statement](https://dev.mysql.com/doc/refman/5.7/en/grant.html), [REVOKE Statement](https://dev.mysql.com/doc/refman/5.7/en/revoke.html), [SET param_name](https://dev.mysql.com/doc/refman/5.7/en/set-variable.html), [USE Statement](https://dev.mysql.com/doc/refman/5.7/en/use.html), [ALTER FUNCTION Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-function.html), [ALTER PROCEDURE Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-procedure.html)| Partially supported       |
| [DROP FUNCTION Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-function.html), [DROP PROCEDURE](https://dev.mysql.com/doc/refman/5.7/en/drop-procedure.html)[and DROP FUNCTION Statements](https://dev.mysql.com/doc/refman/5.7/en/drop-procedure.html), [DROP SERVER Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-server.html), [DROP VIEW Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-view.html), [RENAME TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/rename-table.html), [TRUNCATE TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/truncate-table.html), [CALL Statement](https://dev.mysql.com/doc/refman/5.7/en/call.html), [BEGIN ... END Compound Statement](https://dev.mysql.com/doc/refman/5.7/en/begin-end.html), [Statement Labels](https://dev.mysql.com/doc/refman/5.7/en/statement-labels.html), [DECLARE Statement](https://dev.mysql.com/doc/refman/5.7/en/declare.html), [CASE Statement](https://dev.mysql.com/doc/refman/5.7/en/case.html), [IF Statement](https://dev.mysql.com/doc/refman/5.7/en/if.html), [LEAVE Statement](https://dev.mysql.com/doc/refman/5.7/en/leave.html), [LOOP Statement](https://dev.mysql.com/doc/refman/5.7/en/loop.html), [REPEAT Statement](https://dev.mysql.com/doc/refman/5.7/en/repeat.html), [RETURN Statement](https://dev.mysql.com/doc/refman/5.7/en/return.html), [WHILE Statement](https://dev.mysql.com/doc/refman/5.7/en/while.html), [DROP USER Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-user.html), [DROP TRIGGER Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-trigger.html), [Cursors](https://dev.mysql.com/doc/refman/5.7/en/cursors.html), [Condition Handling](https://dev.mysql.com/doc/refman/5.7/en/condition-handling.html)| Fully supported       |

* Not supported by openGauss: An error will be recorded in the log. The database objects fail to be migrated as the statement exists in the database objects to be migrated.
* Not supported by Druid: The statement fails to be parsed. The database objects that contain the statement also fail to be migrated.
* Incompatible: Though openGauss and MySQL both support this statement, conversion between openGauss and MySQL fails due to field differences.
* Fully supported: All fields of the statement can be converted into the corresponding fields of openGauss.
* Partially supported: The conversion has been performed as much as possible. If some fields are incompatible but do not affect the use of the entire statement, the keyword is commented out. If a field is incompatible and affects the use of the statement, an error is recorded in the log to describe the reason why the statement is incompatible. As a result, the database object that contains the statement fails to be migrated. For example, in openGauss, the AS query_expression of [CREATE TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/create-table.html) is incompatible with MySQL. In openGauss, [DELETE Statement](https://dev.mysql.com/doc/refman/5.7/en/delete.html) does not support the syntax for deleting multiple tables. In openGauss, the SELECT INTO field of [SELECT Statement](https://dev.mysql.com/doc/refman/5.7/en/select.html) supports only stored procedure/function parameters, or local variables. In openGauss, [UPDATE Statement](https://dev.mysql.com/doc/refman/5.7/en/update.html) does not support the update of multiple tables at the same time. If the GRANT statement or REVOKE statement contains privileges that are not supported by openGauss, the statement cannot be migrated. Statements related to PARTITION may fail to be migrated due to some fields. For details, see the logs.

The following describes the translation of all SQL statements in MySQL 5.7.

### [13.1.1 ALTER DATABASE Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-database.html)

> 1. This statement is incompatible between openGauss and MySQL. While MySQL mandates an alter_option for ALTER DATABASE operations, openGauss lacks any functional equivalent for this.

### [13.1.2 ALTER EVENT Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-event.html)

> 1. The statement does not exist in openGauss.

### [13.1.3 ALTER FUNCTION Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-function.html)

> 1. Partially supported. Druid cannot parse the NO SQL, READS SQL DATA, and MODIFIES SQL DATA fields.

### [13.1.4 ALTER INSTANCE Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-instance.html)

> 1. The statement does not exist in openGauss, and Druid cannot parse the statement.

### [13.1.5 ALTER LOGFILE GROUP Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-logfile-group.html)

> 1. The statement does not exist in openGauss.

### [13.1.6 ALTER PROCEDURE Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-procedure.html)

> 1. Partially supported. Druid cannot parse the NO SQL, READS SQL DATA, and MODIFIES SQL DATA fields.

### [13.1.7 ALTER SERVER Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-server.html)

> 1. This statement is incompatible between openGauss and MySQL. MySQL's support for foreign data wrappers is different from that of openGauss.

### [13.1.8 ALTER TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-table.html)

> 1. openGauss does not support first, after, index_type, index_option, fulltext, and spatial. Comment them out.
> 2. The add foreign key and add unique key of openGauss do not support index_name. Druid does not parse the constraint symbol of add unique key.
> 3. openGauss does not support algorithm, character set, convert to character set, disable|enable keys, discard|import tablespace, drop index, drop primary key, drop foreign key, force, lock, order by and without|with validation.
> 4. The change and modify keywords of alter table are translated into the drop and add forms.
> 5. openGauss does not support rename index, but supports duplicate table names. However, the table names need to be written in two separate statements.

### [13.1.9 ALTER TABLESPACE Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-tablespace.html)

> 1. This statement is incompatible between openGauss and MySQL. {ADD | DROP} DATAFILE 'file_name' cannot be translated. Therefore, the entire statement cannot be translated.

### [13.1.10 ALTER VIEW Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-view.html)

> 1. MySQL has the [WITH [CASCADED | LOCAL] CHECK OPTION], but openGauss does not support them.

### [13.1.11 CREATE DATABASE Statement](https://dev.mysql.com/doc/refman/5.7/en/create-database.html)

> 1. CREATE SCHEMA translated to openGauss and create_option of MySQL contain CHARACTER SET or COLLATE, which are incompatible with openGauss.

### [13.1.12 CREATE EVENT Statement](https://dev.mysql.com/doc/refman/5.7/en/create-event.html)

> 1. The statement does not exist in openGauss.

### [13.1.13 CREATE FUNCTION Statement](https://dev.mysql.com/doc/refman/5.7/en/create-function.html)

> 1. MySQL has SECURITY, CONTAINS SQL|NO SQL|READS SQL DATA|MODIFIES SQL DATA, which are not supported by openGauss.
> 2. Druid cannot parse CONTAINS SQL, NO SQL, READS SQL DATA, MODIFIES SQL DATA, and SECURITY.
> 3. DETERMINISTIC is translated into DETERMINISTIC in openGauss. When the database needs to be modified, DETERMINISTIC cannot be used. In this case, use VOLATILE instead.

### [13.1.14 CREATE INDEX Statement](https://dev.mysql.com/doc/refman/5.7/en/create-index.html)

> 1. openGauss does not support FULLTEXT | SPATIAL. openGauss does not have this statement.
> 2. CONCURRENTLY in openGauss indicates that indexes are created without blocking DML operations. MySQL can be converted to openGauss CONCURRENTLY in the following two cases, which are related to LOCK and ALGORITHM of MySQL:
>
> * lock=none
> * algorithm=inplace , lock=default
>
> 3. For other cases, they are not supported by OpenGauss.
> 4. The key_part part of openGauss does not support length, that is, the length specified by col_name.
> 5. If both using hash and asc|desc exist, an error is reported. This is because the hash of openGauss can only process simple equality comparison, and asc|desc can be used only for using btree.
> 6. openGauss does not support index_option (except index_type).

### [13.1.15 CREATE LOGFILE GROUP Statement](https://dev.mysql.com/doc/refman/5.7/en/create-logfile-group.html)

> 1. The statement does not exist in openGauss.

### [13.1.16 CREATE PROCEDURE Statement](https://dev.mysql.com/doc/refman/5.7/en/create-procedure.html)

> 1. MySQL has CONTAINS SQL|NO SQL|READS SQL DATA|MODIFIES SQL, which are not supported by openGauss.
> 2. Druid cannot parse NO SQL|READS SQL DATA|MODIFIES SQL. Druid cannot parse SECURITY.
> 3. DETERMINISTIC is translated into DETERMINISTIC in openGauss. When the database needs to be modified, DETERMINISTIC cannot be used. In this case, use VOLATILE instead.

### [13.1.17 CREATE SERVER Statement](https://dev.mysql.com/doc/refman/5.7/en/create-server.html)

> 1. This statement is incompatible between openGauss and MySQL.

### [13.1.18 CREATE TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/create-table.html)

> 1. Data type: openGauss does not support the YEAR, ENUM, or SET type.
> 2. In MySQL's createde_finition, openGauss does not support creating INDEX | KEY or {FULLTEXT | SPATIAL} [INDEX | KEY] in table constraints. Instead, it translates them into two statements: CREATE TABLE and CREATE INDEX ON TABLE. {FULLTEXT | SPATIAL} is omitted.
> 3. In MySQL's createde_finition, openGauss does not support index_type, index_option, or index_name. In addition, Druid cannot identify the constraint name symbol of the unique constraint.
> 4. In MySQL's column_definition, openGauss does not support COMMENT, COLUMNFORMAT, STORAGE, GENERATED ALWAYS, or VIRTUAL | STORED in column constraints. In openGauss, AUTO_INCREMENT uses bigserial to achieve auto-increment of integers. COLLATE is incompatible between the two.
> 5. In MySQL's table_option, openGauss supports only TABLESPACE ([STORAGE {DISK | MEMORY}] is not supported).
> 6. The create table as query expression in openGauss is essentially different from that in MySQL. When a keyword is added to an old table in MySQL, openGauss copies the entire table and cannot convert the table.
> 7. In MySQL, the LIKE statement retains the default value expression, storage engine attributes, CHECK constraints, and comments of the new table in the source table by default. Therefore, additional table attribute information is added to openGauss, and the default value of like_option in the LIKE statement is INCLUDING DEFAULTS INCLUDING CONSTRAINTS INCLUDING INDEXES INCLUDING STORAGE.
> 8. Partition:
>
> * openGauss does not support LINEAR.
> For PARTITION BY HASH in openGauss, the expr is restricted to a single column mapping, as openGauss cannot guarantee successful parsing of complex expressions. MySQL's PARTITION BY KEY must be mapped to PARTITION BY HASH (column), with column_list limited to a single column. Regarding PARTITION BY RANGE, while expr is restricted to a single column to avoid parsing failures, column_list supports multi-column configurations. Finally, for PARTITION BY LIST, both expr and column_list are restricted to single-column mappings to ensure syntax integrity.
> * PARTITIONS num and SUBPARTITION num are not supported.
> * The partition name of openGauss is mandatory. Therefore, MySQL must have partition_defition to translate PARTITION.
> * Some data types of openGauss partition by conflict with those of MySQL. This  is complex and will be discussed later.
> * Druid does not parse the ALGORITHM of SUBPARTITION BY KEY, and openGauss does not support it.
>
> 8. In MySQL's partition_definition, openGauss supports only VALUES and TABLESPACE. Engine, max_rows, and min_rows will be commented out (Druid parses min_rows as max_rows). Comment, data directory, and index directory will cause syntax errors and are not parsed by Druid.
> 9. For the VALUES LESS THAN clause, the openGauss range partitioning policy supports a maximum of four columns as partition keys.
> 10. In MySQL's subpartition_definition, openGauss supports only TABLESPACE. Engine, comment, data directory, index directory, max_rows, and min_rows will be commented out.

### [13.1.19 CREATE TABLESPACE Statement](https://dev.mysql.com/doc/refman/5.7/en/create-tablespace.html)

> 1. This statement is incompatible between openGauss and MySQL. ADD DATAFILE 'file_name' field is incompatible with openGauss's LOCATION 'directory'. In addition, the Druid code has a bug that cannot parse ADD DATAFILE. The cause of the bug has been found. It is recommended that an issue and a pull request be submitted to the Druid repository for fixing.

### [13.1.20 CREATE TRIGGER Statement](https://dev.mysql.com/doc/refman/5.7/en/create-trigger.html)

> 1. MySQL has [DEFINER = user]. The DEFINER clause specifies the security context to be used when access permissions are checked upon trigger activation. openGauss does not have this clause.
> 2. MySQL has trigger_order but openGauss does not.
> 3. In MySQL, trigger_body consists of a valid SQL routine statement or a compound statement written using BEGIN AND. openGauss uses EXECUTE PROCEDURE function_name to use a trigger function, where function_name is a user-defined function that does not take parameters and returns a trigger. Therefore, the executor needs to be converted into a custom function, whose name is generated by UUID.

### [13.1.21 CREATE VIEW Statement](https://dev.mysql.com/doc/refman/5.7/en/create-view.html)

> 1. MySQL has the [WITH [CASCADED | LOCAL] CHECK OPTION], but openGauss does not support them.

### [13.1.22 DROP DATABASE Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-database.html)

> 1. Translate the DROP SCHEMA statement into openGauss.

### [13.1.23 DROP EVENT Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-event.html)

> 1. The statement does not exist in openGauss.

### [13.1.24 DROP FUNCTION Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-function.html)

> 1. Fully supported without translation.

### [13.1.25 DROP INDEX Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-index.html)

> 1. openGauss does not support specifying tbl_name.
> 2. CONCURRENTLY in openGauss indicates that indexes are created without blocking DML operations.
> 3. MySQL can be converted to openGauss CONCURRENTLY in the following two cases, which are related to LOCK and ALGORITHM of MySQL:
>
> * lock=none
> * algorithm=inplace , lock=default
>
> 4. For other cases, they are not supported by OpenGauss.

### [13.1.26 DROP LOGFILE GROUP Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-logfile-group.html)

> 1. The statement does not exist in openGauss.

### [13.1.27 DROP PROCEDURE and DROP FUNCTION Statements](https://dev.mysql.com/doc/refman/5.7/en/drop-procedure.html)

> 1. Fully supported without translation.

### [13.1.28 DROP SERVER Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-server.html)

> 1. Fully supported without translation.

### [13.1.29 DROP TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-table.html)

> 1. MySQL can have TEMPORARY. openGauss does not support deleting only the TEMPORARY table. Comment it out instead.

### [13.1.30 DROP TABLESPACE Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-tablespace.html)

> 1. ENGINE is not supported.

### [13.1.31 DROP TRIGGER Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-trigger.html)

> 1. Partially supported in B-compatible mode. The [drop trigger](https://docs.opengauss.org/zh/docs/5.0.0/docs/SQLReference/DROP-TRIGGER.html) format of specifying schema_name before trigger_name is not supported.

### [13.1.32 DROP VIEW Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-view.html)

> 1. Fully supported without translation.

### [13.1.33 RENAME TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/rename-table.html)

> 1. openGauss uses ALTER TABLE to rename a table. ALTER TABLE can be directly translated.

### [13.1.34 TRUNCATE TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/truncate-table.html)

> 1. Fully supported without translation.

### [13.2.1 CALL Statement](https://dev.mysql.com/doc/refman/5.7/en/call.html)

> 1. Fully supported without translation.

### [13.2.2 DELETE Statement](https://dev.mysql.com/doc/refman/5.7/en/delete.html)

> 1. Low_priority, quick, and ignore are not supported.
> 2. ORDER BY and LIMIT are not supported.
> 3. openGauss does not support multiple partition_names.
> 4. openGauss does not support multiple-table DELETE syntax.

### [13.2.3 DO Statement](https://dev.mysql.com/doc/refman/5.7/en/do.html)

> 1. The statement exists in openGauss, but Druid cannot parse the statement.

### [13.2.4 HANDLER Statement](https://dev.mysql.com/doc/refman/5.7/en/handler.html)

> 1. The statement does not exist in openGauss.

### [13.2.5 INSERT Statement](https://dev.mysql.com/doc/refman/5.7/en/insert.html)

> 1. LOWPRIORITY | DELAYED | HIGHPRIORITY and IGNORE are not supported.
> 2. openGauss does not support multiple partition_names.

### [13.2.6 LOAD DATA Statement](https://dev.mysql.com/doc/refman/5.7/en/load-data.html)

> 1. Stored procedure bodies and function bodies do not support this statement.

### [13.2.7 LOAD XML Statement](https://dev.mysql.com/doc/refman/5.7/en/load-xml.html)

> 1. Stored procedure bodies and function bodies do not support this statement.

### [13.2.8 REPLACE Statement](https://dev.mysql.com/doc/refman/5.7/en/replace.html)

> 1. The statement does not exist in openGauss, and the INSERT IGNORE statement that is synonymous with it does not exist either.

### [13.2.9 SELECT Statement](https://dev.mysql.com/doc/refman/5.7/en/select.html)

> 1. openGauss does not support DISTINCTROW, HIGH_PRIORITY, STRAIGHT_JOIN, SQL_SMALL_RESULT, SQL_BIG_RESULT, SQL_BUFFER_RESULT, SQL_CACHE, SQL_NO_CACHE, SQL_CALC_FOUND_ROWS and PROCEDURE procedurename(argumentlist).
> 2. Druid does not parse PROCEDURE procedurename(argumentlist).
> 3. Druid does not parse PARTITION partition_list.
> 4. SELECT... INTO var_list in MySQL supports only variables that are stored procedures or function parameters, or local variables of stored procedures or functions. User-defined variables (starting with @) are not supported. SELECT ... INTO OUTFILE and SELECT ... INTO DUMPFILE in MySQL are not supported in openGauss.
> 5. Druid does not parse PARTITION partition_list or SELECT ... INTO DUMPFILE.
> 6. In table_references, MySQL supports six join types: INNER, CROSS, LEFT [OUTER], RIGHT [OUTER], NATURAL, and STRAIGHT_JOIN. openGauss does not support STRAIGHT_JOIN. The function of STRAIGHT_JOIN is similar to that of JOIN, so JOIN can be used as a substitute.
> 7. openGauss stored procedures cannot use SELECT to return data.

### [13.2.10 Subqueries](https://dev.mysql.com/doc/refman/5.7/en/subqueries.html)

> 1. Fully supported without translation.

### [13.2.11 UPDATE Statement](https://dev.mysql.com/doc/refman/5.7/en/update.html)

> 1. openGauss does not suppor LOW_PRIORITY, IGNORE, ORDER BY ..., or LIMIT rowcount.
> 2. In openGauss, the tablereference keyword supports only one table name.

### [13.3.1 START TRANSACTION, COMMIT, and ROLLBACK Statements](https://dev.mysql.com/doc/refman/5.7/en/commit.html) [13.3.6 SET TRANSACTION Statement](https://dev.mysql.com/doc/refman/5.7/en/set-transaction.html)

> 1. In START TRANSACTION, openGauss does not support WITH CONSISTENT SNAPSHOT.
> 2. In BEGIN, COMMIT, and ROLLBACK, openGauss does not support AND [NO] CHAIN and [NO] RELEASE.
> 3. In SET TRANSACTION, openGauss does not support GLOBAL and READ UNCOMMITTED. SESSION in MySQL is translated into SESSION CHARACTERISTICS AS in openGauss.
> 4. Currently, the sql-translator does not support the COMMIT and ROLLBACK statements in objects (such as stored procedures). As a result, a parsing error occurs.

### [13.4 Replication Statements](https://dev.mysql.com/doc/refman/5.7/en/sql-replication-statements.html)

> 1. The statement does not exist in openGauss.

### [13.5 Prepared Statements](https://dev.mysql.com/doc/refman/5.7/en/sql-prepared-statements.html)

> 1. This statement is incompatible between openGauss and MySQL.
>    * MySQL creates PREPARE statements by using string literals to provide the text of the statement or by providing the text of the statement as a user variable. Neither of these methods is supported by openGauss. In openGauss, the PREPARE statement directly specifies the parameter type.
>    * The EXECUTE statement in MySQL also requires user variables, which is not supported by openGauss.

### [13.6.1 BEGIN ... END Compound Statement](https://dev.mysql.com/doc/refman/5.7/en/begin-end.html)

> 1. Fully supported

### [13.6.2 Statement Labels](https://dev.mysql.com/doc/refman/5.7/en/statement-labels.html)

> 1. When Druid parses labels, the format "label: " must be used, meaning there must be a space after the colon for successful parsing.

### [13.6.3 DECLARE Statement](https://dev.mysql.com/doc/refman/5.7/en/declare.html)

> 1. Fully supported

### [13.6.5 Flow Control Statements](https://dev.mysql.com/doc/refman/5.7/en/flow-control-statements.html)

> 1. Druid cannot parse the ITERATE statement.
> 2. The REPEAT statement exists in openGauss and is currently replaced with the LOOP statement.
> 3. Druid cannot parse the second syntax of the CASE statement. Therefore, it cannot be converted.
> 4. CASE, IF, LEAVE, LOOP, REPEAT, RETURN, and WHILE are fully supported.

### [13.6.6 Cursors](https://dev.mysql.com/doc/refman/5.7/en/cursors.html)

> 1. Compatible

### [13.6.7 Condition Handling](https://dev.mysql.com/doc/refman/5.7/en/condition-handling.html)

> 1. Compatible

### [13.7.1.1 ALTER USER Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-user.html)

> 1. Druid does not support parsing for REQUIRE, WITH resourceoption, and lockoption clauses. Note that lock_option exists in openGauss.
> 2. openGauss does not support IF EXISTS, user(), and auth_plugin.
> 3. Account names in openGauss do not require single quotation marks and do not contain hostname. The current solution is to extract the username part and remove the double quotation marks.
> 4. Password_option in openGauss supports only PASSWORD EXPIRE.

### [13.7.1.2 CREATE USER Statement](https://dev.mysql.com/doc/refman/5.7/en/create-user.html)

> 1. Druid does not parse REQUIRE, WITH resourceoption, passwordoption, or lock_option, and openGauss does not support them either.
> 2. openGauss does not support auth_plugin.

### [13.7.1.3 DROP USER Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-user.html)

> 1. Fully supported without translation.

### [13.7.1.4 GRANT Statement](https://dev.mysql.com/doc/refman/5.7/en/grant.html)

> 1. openGauss does not have resource_option.
> 2. openGauss does not have the require clause.
> 3. MySQL grants privileges at the global, database, table, and routine levels. openGauss does not support granting privileges to the global level (*.*) or default database (*).
> 4. If MySQL grants only the CREATE privilege at the database level (db_name.*), it maps directly to the openGauss SCHEMA level. Granting ALTER, DELETE, INDEX, INSERT, REFERENCES, SELECT, or UPDATE at the database level effectively applies to all tables; therefore, this should be mapped to ALL TABLES IN SCHEMA schema_name in openGauss. Granting EXECUTE or ALTER ROUTINE at the database level targets all functions and stored procedures, which maps to ALL FUNCTIONS IN SCHEMA schema_name in openGauss. If ALL[PRIVILEGES] or DROP is granted at the MySQL database level (db_name.*), it is translated as granting privileges to both the openGauss schema and all tables contained within it. Other database-level privileges, including CREATE ROUTINE, CREATE TEMPORARY TABLES, CREATE VIEW, EVENT, LOCK TABLES, SHOW VIEW, and TRIGGER, are not supported by openGauss.
> 5. If MySQL grants privileges to a specific FUNCTION or PROCEDURE, openGauss is not compatible because the FUNCTION or PROCEDURE of openGauss must contain parameter types.
> 6. If MySQL grants privileges to a specific table (db_name.tbl_name or tbl_name), the privileges ALTER, DELETE, DROP, INSERT, REFERENCES, SELECT, UPDATE, INDEX, GRANT OPTION, and ALL can be successfully translated into the corresponding privileges of the specific table in openGauss. In addition, the USAGE permission of the schema to which the table belongs must be granted to the user. Other privileges such as CREATE, CREATE VIEW, LOCK TABLES, SHOW VIEW, TRIGGER, USAGE are not supported by openGauss.
> 7. The username of openGauss does not contain the hostname. Information will be lost during conversion to MySQL.

### [13.7.1.5 RENAME USER Statement](https://dev.mysql.com/doc/refman/5.7/en/rename-user.html)

> 1. Change the username by ALTER USER
> 2. Druid cannot parse this statement. Multiple users cannot be renamed at the same time.

### [13.7.1.6 REVOKE Statement](https://dev.mysql.com/doc/refman/5.7/en/revoke.html)

> 1. See [13.7.1.4 GRANT Statement](https://dev.mysql.com/doc/refman/5.7/en/grant.html).

### [13.7.1.7 SET PASSWORD Statement](https://dev.mysql.com/doc/refman/5.7/en/set-password.html)

> 1. The statement does not exist in openGauss.

### [13.7.2 Table Maintenance Statement](https://dev.mysql.com/doc/refman/5.7/en/table-maintenance-statements.html)

> 1. [13.7.2.1 ANALYZE TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/analyze-table.html) is incompatible. The ANALYZEA TABLE statement in MySQL is incompatible with openGauss. openGauss' non-temporary tables cannot be analyzed in an anonymous block, transaction block, function, or stored procedure. However, MySQL does not have this restriction.
> 2. openGauss does not have [13.7.2.2 CHECK TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/check-table.html), [13.7.2.3 CHECKSUM TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/checksum-table.html), or [13.7.2.4 OPTIMIZE TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/optimize-table.html)[13.7.2.5 REPAIR TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/repair-table.html).

### [13.7.3 Plugin and Loadable Function Statements](https://dev.mysql.com/doc/refman/5.7/en/component-statements.html)

> 1. The statement does not exist in openGauss.

### [13.7.4 SET Statement](https://dev.mysql.com/doc/refman/5.7/en/set-statement.html)

> 1. [13.7.4.1 SET Syntax for Variable Assignment](https://dev.mysql.com/doc/refman/5.7/en/set-variable.html) openGauss does not support user_var_name, local_var_name, or system_var_name.
> 2.  openGauss does not have [13.7.4.2 SET CHARACTER SET Statement](https://dev.mysql.com/doc/refman/5.7/en/set-character-set.html) or [13.7.4.3 SET NAMES Statement](https://dev.mysql.com/doc/refman/5.7/en/set-names.html).

### [13.7.5 SHOW Statement](https://dev.mysql.com/doc/refman/5.7/en/show.html)

> 1. The statement does not exist in openGauss.

### [13.7.6 Other Administrative Statements](https://dev.mysql.com/doc/refman/5.7/en/other-administrative-statements.html)

> 1. openGauss does not have [13.7.6.1 BINLOG Statement](https://dev.mysql.com/doc/refman/5.7/en/binlog.html), [13.7.6.2 CACHE INDEX Statement](https://dev.mysql.com/doc/refman/5.7/en/cache-index.html), [13.7.6.3 FLUSH Statement](https://dev.mysql.com/doc/refman/5.7/en/flush.html), [13.7.6.4 KILL Statement](https://dev.mysql.com/doc/refman/5.7/en/kill.html), or [13.7.6.5 LOAD INDEX INTO CACHE Statement](https://dev.mysql.com/doc/refman/5.7/en/load-index.html).
> 2. [13.7.6.6 RESET Statement](https://dev.mysql.com/doc/refman/5.7/en/reset.html) is incompatible. openGauss does not support reset_option of MySQL.

### [13.8 Utility Statement](https://dev.mysql.com/doc/refman/5.7/en/sql-utility-statements.html)

> 1. openGauss does not have [13.8.1 DESCRIBE Statement](https://dev.mysql.com/doc/refman/5.7/en/describe.html) or (https://dev.mysql.com/doc/refman/5.7/en/cache-index.html)[13.8.3 HELP Statement](https://dev.mysql.com/doc/refman/5.7/en/help.html).
> 2. [13.8.2 EXPLAIN Statement](https://dev.mysql.com/doc/refman/5.7/en/explain.html) Currently, the EXPLAIN statement is not translated because the EXPLAIN output of MySQL is different from that of openGauss.

### Precautions

1. During object migration, all field names (including table names, database names, column names, aliases, usernames, variable names, index names, and partition names) must be case-sensitive during migration. Use double quotation marks ("") to identify them in OpenGauss.
