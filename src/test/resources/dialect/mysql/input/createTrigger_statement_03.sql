create table table1(id int, name varchar(50));
create table table2(id int, name varchar(50));
create DEFINER=`mysql_test`@`%` trigger trigger1 before insert on table1 for each row insert into table2 values(new.id, new.name);
insert into table1 values(1, 'test1');
select * from table2;
-- table2新增一行和table1相同的数据