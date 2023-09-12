-- DEFINER `root`
CREATE OR REPLACE FUNCTION createFunction_1692ab45b3a540ca863d5612ed34a1b0() RETURNS TRIGGER AS
$$
DECLARE
BEGIN
new.work_year := 0;
new.work_year := 1;
WHILE new.work_year > 0 LOOP
	new.work_year := new.work_year - 1;
END LOOP;
LOOP
	new.work_year := new.work_year + 1;
	IF new.work_year > 10 THEN
		EXIT;
	END IF;
END LOOP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_before_insert_employee
BEFORE UPDATE ON t_employee
FOR EACH ROW
EXECUTE PROCEDURE createFunction_1692ab45b3a540ca863d5612ed34a1b0();
