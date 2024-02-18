create table table3 (id int, name varchar(10), col varchar(20), primary key(id));
create DEFINER=`mysql_test`@`%` trigger trigger3 before insert on table3 for each row set new.col = concat(new.name, new.id);
insert into table3(id, name) values(1, 'test');
select * from table3;
-- table1的col列内容为test1