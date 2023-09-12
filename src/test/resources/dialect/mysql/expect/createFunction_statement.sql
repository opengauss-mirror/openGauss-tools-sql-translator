CREATE DEFINER = `root`
FUNCTION "doIterate"(
		p INTEGER,
		q INTEGER
	)
	RETURNS INTEGER DETERMINISTIC
	AS $$
	DECLARE x INTEGER DEFAULT 5;
	BEGIN
		<<label1>>LOOP
			p := p - 1;
			INSERT
			INTO "testFunction"
			VALUES (DEFAULT, concat('a', p));
			IF p < 10 THEN
				EXIT label1;
			END IF;
		END LOOP label1;
		WHILE x > 0 LOOP
			x := x - 1;
		END LOOP;
		LOOP
			x := x - 1;
			IF x > 10 THEN
				EXIT;
			END IF;
		END LOOP;
		CASE x
		WHEN 10 THEN
			x := 100;
		WHEN 11 THEN
			x := 110;
		ELSE
			x := 120;
		END CASE;
	RETURN x;
END;
$$language plpgsql;

CREATE DEFINER = `mysql_test`
FUNCTION "double_input"(
		x INTEGER
	)
	RETURNS INTEGER
	 COMMENT 'test function comment'

	AS $$
	DECLARE result INTEGER;
	BEGIN
		result := x * 2;
		RETURN result;
	END
	$$language plpgsql;

	CREATE DEFINER = `mysql_test`
	FUNCTION "double_input1"(
			x INTEGER,
			y INTEGER,
			z INTEGER
		)
		RETURNS INTEGER
		 COMMENT 'test function comment'

		AS $$
		DECLARE result1 INTEGER;
		DECLARE result2 INTEGER;
		DECLARE result3 INTEGER;
		BEGIN
			result1 := x * y * z;
			result2 := x + y + z;
			result3 := x * y - z;
			RETURN result1 + result2 + result3;
		END;
		$$language plpgsql;

		CREATE FUNCTION name_from_employee1(
				emp_id INTEGER
			)
			RETURNS VARCHAR(20)
			AS $$
			BEGIN
				RETURN (
					SELECT name
					FROM employee
					WHERE num = emp_id);
			END;
			$$language plpgsql;

			CREATE FUNCTION name_from_employee1(
					emp_id INTEGER
				)
				RETURNS VARCHAR(20)
				AS $$
				BEGIN
					RETURN (
						SELECT name
						FROM employee
						WHERE num = emp_id);
				END;
				$$language plpgsql;

				CREATE FUNCTION fun12345(
						p INTEGER
					)
					RETURNS INTEGER
					AS $$
					BEGIN
						WHILE p >= 0 LOOP
							p := p - 1;
							INSERT
							INTO testdbc.testdd1
							VALUES (p);
						END LOOP;
						RETURN p;
					END;
					$$language plpgsql;

					CREATE FUNCTION mysql_func1(
							s CHAR(20)
						)
						RETURNS CHAR(50) DETERMINISTIC
						AS $$
						BEGIN
						RETURN concat('mysql_func1, ', s, '!');
						END
						$$language plpgsql;