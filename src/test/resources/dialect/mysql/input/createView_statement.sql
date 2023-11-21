create
or replace          algorithm = merge                           definer=current_user()                     sql security definer                       VIEW vwEmployeesByDepartment               AS
SELECT emp.ID, emp.Name, emp.Salary, CAST(emp.DOB AS Date) AS DOB, emp.Gender, dept.Name AS DepartmentName
FROM Employee emp
         INNER JOIN Department dept ON emp.DeptID = dept.ID with cascaded check
option;

create table test(id int);
CREATE ALGORITHM=UNDEFINED DEFINER=`mysql_test`@`%` SQL SECURITY DEFINER VIEW `view1` AS select `test`.`id` AS `id` from `test`;
CREATE ALGORITHM=UNDEFINED DEFINER=mysql_test SQL SECURITY DEFINER VIEW `view1` AS select `test`.`id` AS `id` from `test`;

drop table if exists chameleon_0012;
create table chameleon_0012(id int comment '字段1', name varchar(20) comment 'col_2');
insert into chameleon_0012 values(10,'tom'),(8,'lili');

create or replace algorithm = merge DEFINER = `mysql_test`@`%` view v_chameleon_0012_1 as select id,name from chameleon_0012 where id <10 with cascaded check option;
create or replace algorithm = merge DEFINER = `mysql_test`@`%` view v_chameleon_0012_2 as select id,name from chameleon_0012 where id <10 with local check option;
create or replace algorithm = merge DEFINER = `mysql_test`@`%` view v_chameleon_0012_3 as select id,name from chameleon_0012 where id <10 with check option;
create or replace algorithm = merge DEFINER = `mysql_test`@`%` view v_chameleon_0012_4 as select id,name from chameleon_0012 where id <10;