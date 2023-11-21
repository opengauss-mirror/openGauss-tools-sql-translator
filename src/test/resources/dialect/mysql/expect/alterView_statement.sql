ALTER ALGORITHM = UNDEFINED
	DEFINER = 'mysql_test'@'%'
	SQL SECURITY DEFINER
	VIEW "view1"
AS
SELECT "test"."id" AS "id"
FROM "test";
ALTER ALGORITHM = UNDEFINED
	DEFINER = mysql_test
	SQL SECURITY DEFINER
	VIEW "view1"
AS
SELECT "test"."id" AS "id"
FROM "test";
ALTER ALGORITHM = merge
	DEFINER = current_user
	SQL SECURITY definer
	VIEW "vwEmployeesByDepartment"
AS
SELECT emp."ID", emp."Name", emp."Salary", CAST(emp."DOB" AS DATE) AS "DOB", emp."Gender"
	, dept."Name" AS "DepartmentName"
FROM "Employee" emp
	INNER JOIN "Department" dept ON emp."DeptID" = dept."ID";
CREATE TABLE test (
	id INTEGER
);
ALTER ALGORITHM = UNDEFINED
	DEFINER = 'mysql_test'@'%'
	SQL SECURITY DEFINER
	VIEW "view1"
AS
SELECT "test"."id" AS "id"
FROM "test";
ALTER ALGORITHM = UNDEFINED
	DEFINER = mysql_test
	SQL SECURITY DEFINER
	VIEW "view1"
AS
SELECT "test"."id" AS "id"
FROM "test";
ALTER ALGORITHM = UNDEFINED
	DEFINER = mysql_test
	SQL SECURITY DEFINER
	VIEW view1
AS
SELECT test.id AS id
FROM test
WITH
CHECK OPTION;
ALTER ALGORITHM = UNDEFINED
	DEFINER = mysql_test
	SQL SECURITY DEFINER
	VIEW view2
AS
SELECT test.id AS id
FROM test
WITH
LOCAL
CHECK OPTION;
ALTER ALGORITHM = UNDEFINED
	DEFINER = mysql_test
	SQL SECURITY DEFINER
	VIEW view3
AS
SELECT test.id AS id
FROM test
WITH
CASCADED
CHECK OPTION;