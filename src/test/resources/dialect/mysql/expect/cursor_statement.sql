CREATE PROCEDURE curdemo ()
AS
DECLARE done INTEGER DEFAULT false;
DECLARE a CHAR(16);
DECLARE b INTEGER;
DECLARE c INTEGER;
BEGIN
DECLARE cur1 CURSOR FOR
	SELECT id, data
	FROM test.t1;
DECLARE cur2 CURSOR FOR
	SELECT i
	FROM test.t2;

	DECLARE CONTINUE HANDLER FOR NOT FOUND
	SET done = true
	;
	OPEN cur1;
	OPEN cur2;
	<<read_loop>>LOOP
		FETCH cur1 INTO a, b;
		FETCH cur2 INTO c;
		IF done THEN
			EXIT read_loop;
		END IF;
		IF b < c THEN
			INSERT
			INTO test.t3
			VALUES (a, b);
		ELSE
			INSERT
			INTO test.t3
			VALUES (a, c);
		END IF;
	END LOOP read_loop;
	CLOSE cur1;
	CLOSE cur2;
END;
/