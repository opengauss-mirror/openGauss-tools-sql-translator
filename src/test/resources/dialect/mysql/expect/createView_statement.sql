CREATE OR REPLACE
	ALGORITHM = merge
	DEFINER = current_user
	SQL SECURITY definer
	VIEW "vwEmployeesByDepartment"
AS
SELECT emp."ID", emp."Name", emp."Salary", CAST(emp."DOB" AS DATE) AS "DOB", emp."Gender"
	, dept."Name" AS "DepartmentName"
FROM "Employee" emp
	INNER JOIN "Department" dept ON emp."DeptID" = dept."ID"
WITH CASCADED
CHECK OPTION
;
CREATE TABLE test (
	id INTEGER
);
CREATE
	ALGORITHM = UNDEFINED
	DEFINER = `mysql_test`@`%`
	SQL SECURITY DEFINER
	VIEW "view1"
AS
SELECT "test"."id" AS "id"
FROM "test";
CREATE
	ALGORITHM = UNDEFINED
	DEFINER = mysql_test
	SQL SECURITY DEFINER
	VIEW "view1"
AS
SELECT "test"."id" AS "id"
FROM "test";
DROP TABLE IF EXISTS chameleon_0012;
CREATE TABLE chameleon_0012 (
	id INTEGER COMMENT '字段1',
	name VARCHAR(20) COMMENT 'col_2'
);
INSERT
INTO chameleon_0012
VALUES (10, 'tom'),
	(8, 'lili');
CREATE OR REPLACE
	ALGORITHM = merge
	DEFINER = `mysql_test`@`%`
	VIEW v_chameleon_0012_1
AS
SELECT id, name
FROM chameleon_0012
WHERE id < 10
WITH CASCADED
CHECK OPTION
;
CREATE OR REPLACE
	ALGORITHM = merge
	DEFINER = `mysql_test`@`%`
	VIEW v_chameleon_0012_2
AS
SELECT id, name
FROM chameleon_0012
WHERE id < 10
WITH LOCAL
CHECK OPTION
;
CREATE OR REPLACE
	ALGORITHM = merge
	DEFINER = `mysql_test`@`%`
	VIEW v_chameleon_0012_3
AS
SELECT id, name
FROM chameleon_0012
WHERE id < 10
WITH CHECK OPTION
;
CREATE OR REPLACE
	ALGORITHM = merge
	DEFINER = `mysql_test`@`%`
	VIEW v_chameleon_0012_4
AS
SELECT id, name
FROM chameleon_0012
WHERE id < 10;