-- DEFINER `mysql_test`
CREATE OR REPLACE FUNCTION createFunction_5e55ac4ac3e940efb7e838c4b62128cf() RETURNS TRIGGER AS
$$
DECLARE
BEGIN
SET @wongloong = @wongloong + NEW.amount;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER test1.ins_sum
BEFORE INSERT ON test1.account
FOR EACH ROW
EXECUTE PROCEDURE createFunction_5e55ac4ac3e940efb7e838c4b62128cf();