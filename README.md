# openGauss-tools-sql-translator

## 介绍

openGauss-tools-sql-translator是一个使用java编写的实现MySQL向openGauss语法转换的翻译器。其基于1.2.8版本Druid实现，利用Druid对AST的访问规则，继承MySQLOutPutVisitor并重载其visit方法，修改其对MySQL语句 AST的访问结果，最终输出openGauss语法的语句。

## 编译步骤

* 安装环境： java，maven，git
* 源数据库要求：MySQL 5.7。目的数据库要求：openGauss 3.0.0
* 打包命令：mvn package

## MySQL与openGauss的兼容性说明

根据SQL语句在MySQL5.7官方文档和openGauss 3.0.0官方文档的差异对比，对各SQL语句进行翻译。下表展示SQL语句的翻译情况。

| SQL语句                                                      | 翻译情况        |
| ------------------------------------------------------------ | --------------- |
| [ALTER EVENT Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-event.html)、[ALTER INSTANCE Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-instance.html)、[ALTER LOGFILE GROUP Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-logfile-group.html)、[CREATE EVENT Statement](https://dev.mysql.com/doc/refman/5.7/en/create-event.html)、[CREATE LOGFILE GROUP Statement](https://dev.mysql.com/doc/refman/5.7/en/create-logfile-group.html)、[DROP EVENT Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-event.html)、[DROP LOGFILE GROUP Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-logfile-group.html)、[HANDLER Statement](https://dev.mysql.com/doc/refman/5.7/en/handler.html)、[REPLACE Statement](https://dev.mysql.com/doc/refman/5.7/en/replace.html)、[Replication Statements](https://dev.mysql.com/doc/refman/5.7/en/sql-replication-statements.html)、[SET PASSWORD Statement](https://dev.mysql.com/doc/refman/5.7/en/set-password.html)、[CHECK TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/check-table.html)、[CHECKSUM TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/checksum-table.html)、[OPTIMIZE TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/optimize-table.html)、[REPAIR TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/repair-table.html)、[Plugin and Loadable Function Statements](https://dev.mysql.com/doc/refman/5.7/en/component-statements.html)、[SET NAMES Statement](https://dev.mysql.com/doc/refman/5.7/en/set-names.html)、[SET CHARACTER Statement](https://dev.mysql.com/doc/refman/5.7/en/set-character-set.html)、[SHOW Statements](https://dev.mysql.com/doc/refman/5.7/en/show.html)、[BINLOG Statement](https://dev.mysql.com/doc/refman/5.7/en/binlog.html)、[CACHE INDEX Statement](https://dev.mysql.com/doc/refman/5.7/en/cache-index.html)、[FLUSH Statement](https://dev.mysql.com/doc/refman/5.7/en/flush.html)、[KILL Statement](https://dev.mysql.com/doc/refman/5.7/en/kill.html)、[LOAD INDEX INTO CACHE Statement](https://dev.mysql.com/doc/refman/5.7/en/load-index.html)、[DESCRIBE Statement](https://dev.mysql.com/doc/refman/5.7/en/describe.html)、[HELP Statement](https://dev.mysql.com/doc/refman/5.7/en/help.html) | openGauss不支持 |
| [SHUTDOWN Statement](https://dev.mysql.com/doc/refman/5.7/en/shutdown.html)、[DO Statement](https://dev.mysql.com/doc/refman/5.7/en/do.html)、[ITERATE Statement](https://dev.mysql.com/doc/refman/5.7/en/iterate.html) | Druid不支持     |
| [ALTER DATABASE Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-database.html)、[ALTER SERVER Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-server.html)、[ALTER TABLESPACE Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-tablespace.html)、[CREATE SERVER Statement](https://dev.mysql.com/doc/refman/5.7/en/create-server.html)、[CREATE TABLESPACE Statement](https://dev.mysql.com/doc/refman/5.7/en/create-tablespace.html)、[Prepared Statements](https://dev.mysql.com/doc/refman/5.7/en/sql-prepared-statements.html)、[ANALYZE TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/analyze-table.html)、[RESET Statement](https://dev.mysql.com/doc/refman/5.7/en/reset.html)、[EXPLAIN Statement](https://dev.mysql.com/doc/refman/5.7/en/explain.html) | 无法兼容        |
| [ALTER TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-table.html)、[ALTER VIEW Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-view.html)、[CREATE DATABASE Statement](https://dev.mysql.com/doc/refman/5.7/en/create-database.html)、[CREATE FUNCTION Statement](https://dev.mysql.com/doc/refman/5.7/en/create-function.html)、[CREATE INDEX Statement](https://dev.mysql.com/doc/refman/5.7/en/create-index.html)、[CREATE PROCEDURE Statements](https://dev.mysql.com/doc/refman/5.7/en/create-procedure.html)、[CREATE TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/create-table.html)、[CREATE TRIGGER Statement](https://dev.mysql.com/doc/refman/5.7/en/create-trigger.html)、[CREATE VIEW Statement](https://dev.mysql.com/doc/refman/5.7/en/create-view.html)、[DROP DATABASE Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-database.html)、[DROP INDEX Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-index.html)、[DROP TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-table.html)、[DROP TABLESPACE Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-tablespace.html)、[DELETE Statement](https://dev.mysql.com/doc/refman/5.7/en/delete.html)、[INSERT Statement](https://dev.mysql.com/doc/refman/5.7/en/insert.html)、[SELECT Statement](https://dev.mysql.com/doc/refman/5.7/en/select.html)、[Subqueries](https://dev.mysql.com/doc/refman/5.7/en/subqueries.html)、[UPDATE Statement](https://dev.mysql.com/doc/refman/5.7/en/update.html)、[START TRANSACTION, COMMIT, and ROLLBACK Statements](https://dev.mysql.com/doc/refman/5.7/en/commit.html)、[SET TRANSACTION Statement](https://dev.mysql.com/doc/refman/5.7/en/set-transaction.html)、[ALTER USER Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-user.html)、[CREATE USER Statement](https://dev.mysql.com/doc/refman/5.7/en/create-user.html)、[RENAME USER Statement](https://dev.mysql.com/doc/refman/5.7/en/rename-user.html)、[GRANT Statement](https://dev.mysql.com/doc/refman/5.7/en/grant.html)、[REVOKE Statement](https://dev.mysql.com/doc/refman/5.7/en/revoke.html)、[SET param_name](https://dev.mysql.com/doc/refman/5.7/en/set-variable.html)、[USE Statement](https://dev.mysql.com/doc/refman/5.7/en/use.html)、[ALTER FUNCTION Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-function.html)、[ALTER PROCEDURE Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-procedure.html) | 部分支持        |
| [DROP FUNCTION Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-function.html)、[DROP PROCEDURE](https://dev.mysql.com/doc/refman/5.7/en/drop-procedure.html)[and DROP FUNCTION Statements](https://dev.mysql.com/doc/refman/5.7/en/drop-procedure.html)、[DROP SERVER Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-server.html)、[DROP VIEW Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-view.html)、[RENAME TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/rename-table.html)、[TRUNCATE TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/truncate-table.html)、[CALL Statement](https://dev.mysql.com/doc/refman/5.7/en/call.html)、[BEGIN ... END Compound Statement](https://dev.mysql.com/doc/refman/5.7/en/begin-end.html)、[Statement Labels](https://dev.mysql.com/doc/refman/5.7/en/statement-labels.html)、[DECLARE Statement](https://dev.mysql.com/doc/refman/5.7/en/declare.html)、[CASE Statement](https://dev.mysql.com/doc/refman/5.7/en/case.html)、[IF Statement](https://dev.mysql.com/doc/refman/5.7/en/if.html)、[LEAVE Statement](https://dev.mysql.com/doc/refman/5.7/en/leave.html)、[LOOP Statement](https://dev.mysql.com/doc/refman/5.7/en/loop.html)、[REPEAT Statement](https://dev.mysql.com/doc/refman/5.7/en/repeat.html)、[RETURN Statement](https://dev.mysql.com/doc/refman/5.7/en/return.html)、[WHILE Statement](https://dev.mysql.com/doc/refman/5.7/en/while.html)、[DROP USER Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-user.html)、[DROP TRIGGER Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-trigger.html)、[Cursors](https://dev.mysql.com/doc/refman/5.7/en/cursors.html)、[Condition Handling](https://dev.mysql.com/doc/refman/5.7/en/condition-handling.html) | 完全支持        |

* openGauss不支持，表明会在日志中输出ERROR，由于该语句存在于需迁移的数据库对象中，所以该数据库对象迁移失败；
* Druid不支持，表明该语句解析不成功，拥有该语句的数据库对象也迁移失败；
* 无法兼容，表明openGauss与MySQL一样支持该语句，但由于某些字段差异导致openGauss和MySQL之间无法转换。
* 完全支持指该语句的所有字段都可转化成openGauss的相应字段。
* 部分支持表明已尽可能地进行转换。若存在某些字段无法兼容但不影响整条语句的使用，则将该关键字进行注释处理；若某字段无法兼容且影响该语句的使用，则在日志中输出ERROR，说明该语句不能兼容的原因，拥有该语句的数据库对象迁移失败。比如在openGauss中[CREATE TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/create-table.html)的AS query_expression不兼容MySQL；在openGauss中[DELETE Statement](https://dev.mysql.com/doc/refman/5.7/en/delete.html)不支持多表删除语法；在openGauss中[SELECT Statement](https://dev.mysql.com/doc/refman/5.7/en/select.html)的select into字段仅支持变量是存储过程和函数参数或局部变量；在openGauss中[UPDATE Statement](https://dev.mysql.com/doc/refman/5.7/en/update.html)不支持同时更新多表；GRANT statement、REVOKE statement语句若出现openGauss不支持赋予的privilege也会导致该语句无法迁移的情况；与PARTITION分区相关的语句也有可能因某些字段导致迁移失败等等，具体原因请查看日志。

以下对MySQL5.7版本的所有SQL语句具体的翻译情况进行说明。

### [13.1.1 ALTER DATABASE Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-database.html)

> 1. 该语句openGauss和MySQL无法兼容,MySQL一定要有alter_option才能alter database，openGuass没有任何一个对应的alter_option

### [13.1.2 ALTER EVENT Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-event.html)

> 1. openGauss不存在该语句

### [13.1.3 ALTER FUNCTION Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-function.html)

> 1. 部分支持，druid不能解析NO SQL、READS SQL DATA、MODIFIES SQL DATA字段

### [13.1.4 ALTER INSTANCE Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-instance.html)

> 1. openGauss不存在该语句，同时Druid也没有该语句的解析

### [13.1.5 ALTER LOGFILE GROUP Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-logfile-group.html)

> 1. openGauss不存在该语句

### [13.1.6 ALTER PROCEDURE Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-procedure.html)

> 1. 部分支持，druid不能解析NO SQL、READS SQL DATA、MODIFIES SQL DATA字段

### [13.1.7 ALTER SERVER Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-server.html)

> 1. 该语句openGauss和MySQL无法兼容，MySQL对于foreign data wrapper的支持与openGauss不同

### [13.1.8 ALTER TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-table.html)

> 1. openGauss不支持first、after字段、index_type、index_option、fulltext、spatial字段，注释掉
> 2. openGauss的add foreign key、add unique key 不支持index_name；druid不解析add unique key的constraint  symbol
> 3. openGauss不支持algorithm、character set、 convert to character set、disable|enable keys、discard|import tablespace、drop index、drop primary key、drop foreign key、force、lock、order by、without|with validation
> 4. alter table 的 change和modify 关键字都翻译成先drop后add的形式
> 5. openGauss 不支持 rename index，支持重名表名字，但是需要分开成两个语句编写

### [13.1.9 ALTER TABLESPACE Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-tablespace.html)

> 1. 该语句openGauss和MySQL无法兼容。{ADD | DROP} DATAFILE 'file_name' 无法翻译，所以整个语句不支持翻译。

### [13.1.10 ALTER VIEW Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-view.html)

> 1. MySQL存在[WITH [CASCADED | LOCAL] CHECK OPTION]字段，openGauss不支持这些字段

### [13.1.11 CREATE DATABASE Statement](https://dev.mysql.com/doc/refman/5.7/en/create-database.html)

> 1. 翻译成openGauss的CREATE SCHEMA，MySQL的create_option有 CHARACTER SET或 COLLATE字段，openGauss不兼容

### [13.1.12 CREATE EVENT Statement](https://dev.mysql.com/doc/refman/5.7/en/create-event.html)

> 1. openGauss不存在该语句

### [13.1.13 CREATE FUNCTION Statement](https://dev.mysql.com/doc/refman/5.7/en/create-function.html)

> 1. MySQL存在SECURITY、CONTAINS SQL|NO SQL|READS SQL DATA|MODIFIES SQL DATA字段，openGauss不支持这些字段
> 2. Druid不支持解析CONTAINS SQL、NO SQL、READS SQL DATA、MODIFIES SQL DATA、SECURITY
> 3. 把DETERMINISTIC翻译成openGauss的DETERMINISTIC，当需要修改数据库时不能使用DETERMINISTIC，此时用VOLATILE替换

### [13.1.14 CREATE INDEX Statement](https://dev.mysql.com/doc/refman/5.7/en/create-index.html)

> 1. openGauss不支持FULLTEXT | SPATIAL字段openGauss不存在该语句
> 2. openGauss的CONCURRENTLY表示以不阻塞DML的方式创建索引。MySQL有以下两种情况可以转换为openGauss的CONCURRENTLY，和MySQL的lock和algorithm字段相关：
>
> * lock=none
> * algorithm=inplace ，lock=default
>
> 3. 其他情况的lock和algorithm字段，openGauss不支持
> 4. openGauss的key_part部分不支持(length)，即为col_name指定长度
> 5. 当using hash和asc|desc同时存在时会报错，因为openGauss的hash只能处理简单等值比较，using btree才能用asc|desc
> 6. openGauss不支持index_option（index_type除外）

### [13.1.15 CREATE LOGFILE GROUP Statement](https://dev.mysql.com/doc/refman/5.7/en/create-logfile-group.html)

> 1. openGauss不存在该语句

### [13.1.16 CREATE PROCEDURE Statements](https://dev.mysql.com/doc/refman/5.7/en/create-procedure.html)

> 1. MySQL存在CONTAINS SQL|NO SQL|READS SQL DATA|MODIFIES SQL 字段，openGauss不支持该字段
> 2. Druid不支持解析NO SQL|READS SQL DATA|MODIFIES SQL字段druid不支持解析SECURITY字
> 3. 把DETERMINISTIC翻译成openGauss的DETERMINISTIC，当需要修改数据库时不能使用DETERMINISTIC，此时用VOLATILE替换

### [13.1.17 CREATE SERVER Statement](https://dev.mysql.com/doc/refman/5.7/en/create-server.html)

> 1. 该语句openGauss和MySQL无法兼容

### [13.1.18 CREATE TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/create-table.html)

> 1. 数据类型: openGauss 不支持YEAR类型，ENUM和SET类型
> 2. MySQL的createde_finition中，openGauss不支持在表约束中创建INDEX | KEY、 {FULLTEXT | SPATIAL} [INDEX | KEY]，将其翻译为CREATE TABLE和CREATE INDEX ON TABLE两个语句。{FULLTEXT | SPATIAL}省略。
> 3. MySQL的createde_finition中，openGauss不支持index_type、index_option、index_name。此外，Druid无法识别unique约束的约束名symbol
> 4. MySQL的column_definition中，openGauss不支持列约束中的COMMENT 、COLUMNFORMAT、STORAGE、GENERATED ALWAYS、VIRTUAL | STORED字段。AUTO_INCREMENT在openGauss中用bigserial达到整数自增的效果。COLLATE字段两者不兼容
> 5. MySQL的table_option中，openGauss只支持TABLESPACE(不支持[STORAGE {DISK | MEMORY}]字段）
> 6. openGauss 的create table as query expression 和 mysql有本质不同。MySQL在旧表上新加字段，openGauss是完全复制一样的表，无法转换
> 7. MySQL的like 默认源表保留新表的默认值表达式，存储引擎属性，check 约束，注释。所以在openGauss添加额外的表属性信息，like语句的like_option默认为INCLUDING DEFAULTS INCLUDING CONSTRAINTS INCLUDING INDEXES INCLUDING STORAGE
> 8. partition：
>
> * openGauss不支持LINEAR字段
> * openGauss的PARTITION BY HASH的expr只翻译单个字段，openGauss 无法确保expr能够被正确解析；MySQL的PARTITION BY KEY需要翻译成 PARTITION BY HASH(column)，column_list只支持单列；PARTITION BY RANGE的expr仅支持单列，多列则解析失败，column_list支持多列;PARTITION BY LIST的expr只翻译单个字段，无法确保expr能够被正确解析,column_list也仅支持单列
> * 不支持PARTITIONS num、SUBPARTITION num字段
> * openGauss的partition name是必须的，所以MySQL要有partition_defition才可以翻译PARTITION
> * openGauss partition by 的某些数据类型和mysql有冲突，该需求比较复杂，先后置
> * Druid不解析SUBPARTITION BY KEY的ALGORITHM字段且openGauss不支持
>
> 8. 在MySQL的partition_definition中，openGauss仅支持VALUES、TABLESPACE字段；engine、max_rows、min_rows将注释掉（druid把min_rows解析成了max_rows)，comment、data directory、index directory将会引发语法错误，druid目前不解析
> 9. 对于从句是VALUES LESS THAN的语法格式，openGauss范围分区策略的分区键最多支持4列
> 10. 在MySQL的subpartition_definition中，openGauss仅支持TABLESPACE字段，engine、comment、data directory、index directory、max_rows、min_rows将注释掉

### [13.1.19 CREATE TABLESPACE Statement](https://dev.mysql.com/doc/refman/5.7/en/create-tablespace.html)

> 1. 该语句openGauss和MySQL无法兼容。ADD DATAFILE 'file_name'字段无法和openGauss的LOCATION 'directory'兼容。且durid代码存在无法解析add datafile的bug，已找到bug原因，考虑向durid仓库提出issue和pull_request修复

### [13.1.20 CREATE TRIGGER Statement](https://dev.mysql.com/doc/refman/5.7/en/create-trigger.html)

> 1. MySQL存在“[DEFINER = user]”字段，该DEFINER子句确定在触发器激活时检查访问权限时要使用的安全上下文。openGauss则不存在该字段
> 2. MySQL存在trigger_order字段，openGuass不存在
> 3. MySQL的trigger_body由一个有效的SQL例程语句或使用BEGIN AND编写的复合语句组成。openGauss通过EXECUTE PROCEDURE function_name来使用触发器函数，其中function_name 为用户定义的不带参数并返回类型为触发器的函数。所以需要把执行体转化成自定义函数，该函数名由UUID生成

### [13.1.21 CREATE VIEW Statement](https://dev.mysql.com/doc/refman/5.7/en/create-view.html)

> 1. MySQL存在 [WITH [CASCADED | LOCAL] CHECK OPTION]字段，openGauss不支持这些字段

### [13.1.22 DROP DATABASE Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-database.html)

> 1. 翻译成openGauss的DROP SCHEMA语句

### [13.1.23 DROP EVENT Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-event.html)

> 1. openGauss不存在该语句

### [13.1.24 DROP FUNCTION Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-function.html)

> 1. 无需翻译，完全支持

### [13.1.25 DROP INDEX Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-index.html)

> 1. openGauss不支持指定tbl_name
> 2. openGauss的CONCURRENTLY表示以不阻塞DML的方式创建索引
> 3. MySQL有以下两种情况可以转换为openGauss的CONCURRENTLY，和MySQL的lock和algorithm字段相关：
>
> * lock=none
> * algorithm=inplace ，lock=default
>
> 4. 其他情况的lock和algorithm字段，openGauss不支持

### [13.1.26 DROP LOGFILE GROUP Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-logfile-group.html)

> 1. openGauss不存在该语句

### [13.1.27 DROP PROCEDURE and DROP FUNCTION Statements](https://dev.mysql.com/doc/refman/5.7/en/drop-procedure.html)

> 1. 无需翻译，完全支持

### [13.1.28 DROP SERVER Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-server.html)

> 1. 无需翻译，完全支持

### [13.1.29 DROP TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-table.html)

> 1. MySQL可以拥有TEMPORARY字段，仅删除TEMPORARY表，openGauss不支持，目前做法是直接把TEMPORARY注释掉

### [13.1.30 DROP TABLESPACE Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-tablespace.html)

> 1. ENGINE字段不支持

### [13.1.31 DROP TRIGGER Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-trigger.html)

> 1. 在B兼容模式下部分支持，不支持trigger_name前指定schema_name格式[drop trigger](https://docs.opengauss.org/zh/docs/5.0.0/docs/SQLReference/DROP-TRIGGER.html)

### [13.1.32 DROP VIEW Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-view.html)

> 1. 无需翻译，完全支持

### [13.1.33 RENAME TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/rename-table.html)

> 1. openGauss通过ALTER TABLE重名名表，可以用ALTER TABLE直接翻译

### [13.1.34 TRUNCATE TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/truncate-table.html)

> 1. 无需翻译，完全支持

### [13.2.1 CALL Statement](https://dev.mysql.com/doc/refman/5.7/en/call.html)

> 1. 无需翻译，完全支持

### [13.2.2 DELETE Statement](https://dev.mysql.com/doc/refman/5.7/en/delete.html)

> 1. low_priority、quick、ignore关键词不支持
> 2. order by、limit不支持
> 3. openGauss不支持partition_name有多个
> 4. openGauss不支持多表删除语法

### [13.2.3 DO Statement](https://dev.mysql.com/doc/refman/5.7/en/do.html)

> 1. openGauss存在该语句，但是Druid不支持解析此语句

### [13.2.4 HANDLER Statement](https://dev.mysql.com/doc/refman/5.7/en/handler.html)

> 1. openGauss不存在该语句

### [13.2.5 INSERT Statement](https://dev.mysql.com/doc/refman/5.7/en/insert.html)

> 1. 不支持LOWPRIORITY | DELAYED | HIGHPRIORITY、IGNORE字段
> 2. openGauss不支持partition_name有多个

### [13.2.6 LOAD DATA Statement](https://dev.mysql.com/doc/refman/5.7/en/load-data.html)

> 1. 无需翻译，存储过程体、函数体不支持该语句

### [13.2.7 LOAD XML Statement](https://dev.mysql.com/doc/refman/5.7/en/load-xml.html)

> 1. 无需翻译，存储过程体、函数体不支持该语句

### [13.2.8 REPLACE Statement](https://dev.mysql.com/doc/refman/5.7/en/replace.html)

> 1. openGauss不存在该语句，与其同义的INSERT IGNORE也不存在

### [13.2.9 SELECT Statement](https://dev.mysql.com/doc/refman/5.7/en/select.html)

> 1. openGauss不支持DISTINCTROW、HIGH_PRIORITY、STRAIGHT_JOIN、SQL_SMALL_RESULT、SQL_BIG_RESULT、SQL_BUFFER_RESULT、SQL_CACHE、 SQL_NO_CACHE、 SQL_CALC_FOUND_ROWS、PROCEDURE procedurename(argumentlist)字段
> 2. Druid不解析PROCEDURE procedurename(argumentlist)字段，
> 3. druid不解析PARTITION partition_list字段
> 4. MySQL的SELECT... INTO var_list，openGauss仅支持变量是存储过程或函数参数，或存储过程或函数局部变量，不支持用户定义的变量（@开头的）。MySQL的SELECT ... INTO OUTFILE、SELECT ... INTO DUMPFILE，openGauss也不支持
> 5. druid不解析PARTITION partition_list字段、SELECT ... INTO DUMPFILE
> 6. 在字段table_references中，MySQL 支持 INNER、CROSS、LEFT [OUTER]、RIGHT [OUTER]、NATURAL、STRAIGHT_JOIN六种join类型，openGauss 不支持 STRAIGHT_JOIN 这种类型，STRAIGHT_JOIN 功能同 JOIN 类似，使用 JOIN 替代
> 7. openGauss的存储过程不能用select返回数据

### [13.2.10 Subqueries](https://dev.mysql.com/doc/refman/5.7/en/subqueries.html)

> 1. 无需翻译，完全支持

### [13.2.11 UPDATE Statement](https://dev.mysql.com/doc/refman/5.7/en/update.html)

> 1. openGauss不支持LOW_PRIORITY、IGNORE、ORDER BY ...、LIMIT rowcount字段
> 2. tablereference字段在openGauss中只支持一个表名tablename

### [13.3.1 START TRANSACTION, COMMIT, and ROLLBACK Statements](https://dev.mysql.com/doc/refman/5.7/en/commit.html) [13.3.6 SET TRANSACTION Statement](https://dev.mysql.com/doc/refman/5.7/en/set-transaction.html)

> 1. 在STRAT TRANSACTION语句中，openGuass不支持WITH CONSISTENT SNAPSHOT字段
> 2. 在BEGIN、COMMIT、ROLLBACK语句中,openGauss不支持AND [NO] CHAIN 、[NO] RELEASE字段
> 3. 在SET TRANSACTION语句中,openGuass不支持GLOBAL、READ UNCOMMITTED字段。MySQL字段SESSION在openGauss中翻译为SESSION CHARACTERISTICS AS
> 4. 目前的sql-translator不支持在对象中使用（如存储过程）COMMIT、ROLLBACK语句，解析出错

### [13.4 Replication Statements](https://dev.mysql.com/doc/refman/5.7/en/sql-replication-statements.html)

> 1. openGauss不存在该语句

### [13.5 Prepared Statements](https://dev.mysql.com/doc/refman/5.7/en/sql-prepared-statements.html)

> 1. 该语句openGauss和MySQL无法兼容。
>    * MySQL通过使用字符串文字来提供语句的文本或将语句文本作为用户变量提供来创建PREPARE语句，两种方式openGauss都不支持，openGauss的PREPARE语句直接指明参数类型
>    * MySQL的EXECUTE语句也需要用到用户变量，openGauss不支持

### [13.6.1 BEGIN ... END Compound Statement](https://dev.mysql.com/doc/refman/5.7/en/begin-end.html)

> 1. 完全支持

### [13.6.2 Statement Labels](https://dev.mysql.com/doc/refman/5.7/en/statement-labels.html)

> 1. Druid解析label时，需要使用"label: "，即冒号后需要有空格才能解析

### [13.6.3 DECLARE Statement](https://dev.mysql.com/doc/refman/5.7/en/declare.html)

> 1. 完全支持

### [13.6.5 Flow Control Statements](https://dev.mysql.com/doc/refman/5.7/en/flow-control-statements.html)

> 1. druid不支持解析ITERATE语句
> 2. openGauss存在REPEAT语句，当前用LOOP语句替换
> 3. druid不支持解析case语句的第二种语法，因此无法转换
> 4. CASE、IF、LEAVE、LOOP、REPEAT、RETURN、WHILE完全支持

### [13.6.6 Cursors](https://dev.mysql.com/doc/refman/5.7/en/cursors.html)

> 1. 完全兼容

### [13.6.7 Condition Handling](https://dev.mysql.com/doc/refman/5.7/en/condition-handling.html)

> 1. 完全兼容

### [13.7.1.1 ALTER USER Statement](https://dev.mysql.com/doc/refman/5.7/en/alter-user.html)

> 1. druid不解析REQUIRE、WITH resource*option、lock*option字段，其中lock_option字段openGauss中存在
> 2. openGauss不支持IF EXISTS、user()、auth_plugin字段
> 3. openGauss的Account names不需要单引号，且没有hostname的部分，目前解决方法是截取username的部分并去除双引号
> 4. openGauss的password_option字段仅支持 PASSWORD EXPIRE

### [13.7.1.2 CREATE USER Statement](https://dev.mysql.com/doc/refman/5.7/en/create-user.html)

> 1. druid不解析REQUIRE、WITH resourceoption、passwordoption、lock_option字段且openGauss也不支持
> 2. openGauss不支持auth_plugin字段

### [13.7.1.3 DROP USER Statement](https://dev.mysql.com/doc/refman/5.7/en/drop-user.html)

> 1. 无需翻译，完全支持

### [13.7.1.4 GRANT Statement](https://dev.mysql.com/doc/refman/5.7/en/grant.html)

> 1. opengauss无resource_option
> 2. opengauss无require子句
> 3. MySQL所授予的权限为全局、数据库、表和例程级别。opengauss不支持赋予权限给全局级别（*.*）、默认数据库（*）
> 4. MySQL若只是赋予CREATE权限给数据库级别（db_name.*）*，则可直接对应到openGauss的SCHEMA；若赋予权限ALTER、DELETE、INDEX、INSERT、REFERENCES、SELECT、UPDATE给数据库级别（db_name.），其实是对所有的表赋权，翻译到openGauss应该是 ALL TABLES IN SCHEMA schema_name；若赋予权限EXECUTE、ALTER ROUTINE给数据库级别（db_name.*）*,其实是对所有的函数和存储过程赋权，翻译到openGauss应该是ALL FUNCTIONS\PROCEDURE IN SCHEMA schema_name；MySQL若赋予权限ALL[PRIVILEAGE]、DROP给数据库级别（db_name.）,则翻译为不仅对openGauss的schema赋权还有对其下的所有表赋权。其余赋予给数据库级别（db_name.*）的权限CREATE ROUNTION、CREATE TEMPORARY TABLES、CREATE VIEW、EVENT、LOCK TABLES、SHOW VIEW、TRIGGER，openGauss不支持
> 5. MySQL若赋予权限给特定的FUNCTION或PROCEDURE，openGauss不能兼容,因为opengauss的FUNCTION或PROCEDURE都必须带有参数类型
> 6. MySQL如果赋予权限给特定的TABLE（db_name.tbl_name、tbl_name），其中权限ALTER、DELETE、DROP、INSERT、REFERENCES、SELECT、UPDATE、INDEX、GRANT OPTION、ALL可以成功翻译openGauss对应的特定TABLE的权限，并且需要把表所属模式的USAGE权限同时赋予该用户。其余权限CREATE、CREATE VIEW、LOCK TABLES、SHOW VIEW、TRIGGER、USAGE，openGauss不支持
> 7. opengauss的用户不带有hostname，mysql转换会丢失信息

### [13.7.1.5 RENAME USER Statement](https://dev.mysql.com/doc/refman/5.7/en/rename-user.html)

> 1. 通过ALTER USER改变用户名
> 2. druid无法解析该语句多个用户同时重命名

### [13.7.1.6 REVOKE Statement](https://dev.mysql.com/doc/refman/5.7/en/revoke.html)

> 1. 见[13.7.1.4 GRANT Statement](https://dev.mysql.com/doc/refman/5.7/en/grant.html)

### [13.7.1.7 SET PASSWORD Statement](https://dev.mysql.com/doc/refman/5.7/en/set-password.html)

> 1. openGauss不存在该语句

### [13.7.2 Table Maintenance Statements](https://dev.mysql.com/doc/refman/5.7/en/table-maintenance-statements.html)

> 1. [13.7.2.1 ANALYZE TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/analyze-table.html)无法兼容。其中MySQL的ANALYZEA TABLE语句与openGauss不兼容。openGuass的ANALYZE非临时表不能在一个匿名块、事务块、函数或存储过程内被执行。而MySQL没有此限制。
> 2. openGauss不存在[13.7.2.2 CHECK TABLE Statement、](https://dev.mysql.com/doc/refman/5.7/en/check-table.html)[13.7.2.3 CHECKSUM TABLE Statement、](https://dev.mysql.com/doc/refman/5.7/en/checksum-table.html)[13.7.2.4 OPTIMIZE TABLE Statement、](https://dev.mysql.com/doc/refman/5.7/en/optimize-table.html)[13.7.2.5 REPAIR TABLE Statement](https://dev.mysql.com/doc/refman/5.7/en/repair-table.html)

### [13.7.3 Plugin and Loadable Function Statements](https://dev.mysql.com/doc/refman/5.7/en/component-statements.html)

> 1. openGauss不存在该语句

### [13.7.4 SET Statements](https://dev.mysql.com/doc/refman/5.7/en/set-statement.html)

> 1. [13.7.4.1 SET Syntax for Variable Assignment](https://dev.mysql.com/doc/refman/5.7/en/set-variable.html) openGauss不支持user_var_name、local_var_name、 system_var_name
> 2.  openGauss不存在[13.7.4.2 SET CHARACTER SET Statement、](https://dev.mysql.com/doc/refman/5.7/en/set-character-set.html)[13.7.4.3 SET NAMES Statement](https://dev.mysql.com/doc/refman/5.7/en/set-names.html)

### [13.7.5 SHOW Statements](https://dev.mysql.com/doc/refman/5.7/en/show.html)

> 1. openGauss不存在该语句

### [13.7.6 Other Administrative Statements](https://dev.mysql.com/doc/refman/5.7/en/other-administrative-statements.html)

> 1. openGauss不存在[13.7.6.1 BINLOG Statement、](https://dev.mysql.com/doc/refman/5.7/en/binlog.html)[13.7.6.2 CACHE INDEX Statement、](https://dev.mysql.com/doc/refman/5.7/en/cache-index.html)[13.7.6.3 FLUSH Statement、](https://dev.mysql.com/doc/refman/5.7/en/flush.html)[13.7.6.4 KILL Statement、](https://dev.mysql.com/doc/refman/5.7/en/kill.html)[13.7.6.5 LOAD INDEX INTO CACHE Statement](https://dev.mysql.com/doc/refman/5.7/en/load-index.html)
> 2. [13.7.6.6 RESET Statement](https://dev.mysql.com/doc/refman/5.7/en/reset.html)无法兼容。openGauss不支持MySQL的reset_option

### [13.8 Utility Statements](https://dev.mysql.com/doc/refman/5.7/en/sql-utility-statements.html)

> 1. openGauss不存在[13.8.1 DESCRIBE Statement](https://dev.mysql.com/doc/refman/5.7/en/describe.html)[、](https://dev.mysql.com/doc/refman/5.7/en/cache-index.html)[13.8.3 HELP Statement](https://dev.mysql.com/doc/refman/5.7/en/help.html)
> 2. [13.8.2 EXPLAIN Statement](https://dev.mysql.com/doc/refman/5.7/en/explain.html)目前不翻译EXPLAIN语句，因为MySQL与openGauss的EXPLAIN输出不一样

### 注意事项

1. 对象迁移时所有包含大小写的字段名（包括表名、数据库名、列名、别名、用户名、变量名、索引名、分区名等）保持大小写迁移。当在openGauss访问这些字段时，需要使用双引号才能识别该字段。
