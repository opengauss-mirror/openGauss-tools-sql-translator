create  definer = `root`@`%` procedure p1 (in id int,out res int,inout a int) deterministic
    language sql contains sql sql security INVOKER
begin
    select count(id) into res from data where data.id > id;

end;

create   definer = `root`@`%` procedure p2 ()
    language sql contains sql sql security DEFINER
begin
   create table data2(id int);
end;

create   procedure p3 () deterministic
    language sql contains sql
begin
DROP TABLE IF EXISTS data1 RESTRICT;
end;

create   procedure p4 () deterministic
    language sql contains sql
begin
ALTER TABLE testAlterTable1
	ADD col0 decimal(4, 2);
end;

create   procedure p5 () deterministic
    comment 'good'
begin
update  data
set data.id = data.id*1.2;
end;

create user usr2@'%' identified by 'Test@123';
CREATE DEFINER=`usr2`@`%` PROCEDURE definer() SQL SECURITY DEFINER
BEGIN
   update data set data.id = data.id*1.2;
END;

CREATE DEFINER = `usr2`@`%` PROCEDURE definer1()
BEGIN
	UPDATE data SET data.id = data.id * 1.2;
END;

-- demiliter ;
create table user1 (id int primary key,username varchar ( 50 ),password varchar ( 50 ));
-- delimiter $$
create procedure proc16_while(in insertcount int)
begin
declare
 i int default 1;
label: while i<=insertcount
do
insert into user1(id,username,`password`) values(i,concat('user-',i),'123456');
set i=i+1;
end while label;
end
-- $$
-- delimiter ;
-- call proc16_while(10);
create procedure yy_dd() select * from test4;

create procedure proc_object0055()
begin
declare no_such_table condition for 1146;
declare continue handler for no_such_table
begin
insert tb_object0055 values(1,'Mr.Wang'),(2,'Mr.Li'),(3,'Mr.shi'),(4,'Mr.zhang');
end;
drop table if exists tb_object0055;
create table tb_object0055(id int,name varchar(64) comment '姓名');
insert tb_object0055 values(1,'Mr.Wang'),(2,'Mr.Li'),(3,'Mr.shi'),(4,'Mr.zhang');
end;