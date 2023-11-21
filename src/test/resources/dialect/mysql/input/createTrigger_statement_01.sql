create table t_employee(work_year int);
CREATE Definer=`root`@`%` TRIGGER tr_before_insert_employee
                BEFORE UPDATE
                ON t_employee
                FOR EACH ROW
                BEGIN
                SET new.work_year = 0;
                SET new.work_year = 1;
                    while new.work_year>0 do
                        set new.work_year=new.work_year-1;
                    end while;

                    repeat
                        set new.work_year=new.work_year+1;
                    until  new.work_year>10 end repeat;
                END;


