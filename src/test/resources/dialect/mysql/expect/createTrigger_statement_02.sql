CREATE TABLE account (
	amount INTEGER
);
SET @wongloong = 1;
-- DEFINER `mysql_test`@`%`
CREATE OR REPLACE FUNCTION createFunction_5e55ac4ac3e940efb7e838c4b62128cf() RETURNS TRIGGER AS
$$
DECLARE
BEGIN
SET @wongloong = @wongloong + NEW.amount;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER ins_sum
BEFORE INSERT ON account
FOR EACH ROW
EXECUTE PROCEDURE createFunction_5e55ac4ac3e940efb7e838c4b62128cf();