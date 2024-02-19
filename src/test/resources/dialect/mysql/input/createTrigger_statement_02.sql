create table account(amount int);
set @wongloong=1;
CREATE DEFINER=`mysql_test`@`%` TRIGGER ins_sum BEFORE INSERT ON account FOR EACH ROW SET @wongloong = @wongloong + NEW.amount;
insert into account values(3);
select @wongloong;
-- 返回4