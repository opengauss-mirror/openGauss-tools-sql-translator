create table t_employee(work_year int);
-- delimiter $
CREATE DEFINER=`mysql_test`@`%` TRIGGER tr_before_insert_employee BEFORE UPDATE ON t_employee FOR EACH ROW
BEGIN
    SET new.work_year = 10;
    while new.work_year>0 do
        set new.work_year=new.work_year-1;
    end while;

    repeat
       set new.work_year=new.work_year+1;
    until new.work_year>10
    end repeat;
END;
-- $
-- delimiter ;
insert into t_employee values(5);
update t_employee set work_year = 20;
select * from t_employee;
-- 针对update，总是返回11


