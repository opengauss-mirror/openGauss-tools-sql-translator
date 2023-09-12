<<begin_lable>>LOOP
	a := a + 13;
	IF x > a THEN
		EXIT;
	END IF;
END LOOP begin_lable;
LOOP
	a := a + 13;
	IF x > a THEN
		EXIT;
	END IF;
END LOOP;