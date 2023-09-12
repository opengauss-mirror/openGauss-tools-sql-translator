create  
definer = `root`@`%`
FUNCTION doIterate(p int,q int) 
returns int 
deterministic 
begin 
    declare x INT default 5; 
 
    label1: LOOP 
        set p=p-1;
        insert into testFunction values( 
                                           default,concat('a',p) 
                                       ); 

        if p<10 then 
            leave label1; 
        end if; 
    end loop label1; 
 
 
    while x>0 do 
        set x=x-1; 
    end while; 
 
    repeat 
        set x=x-1;
    until  x>10 end repeat; 
 
    case x 
    when 10 then set x=100; 
    when 11 then set x=110; 
    else 
        set x=120; 
        end case; 
 
    return x; 
end;

CREATE DEFINER=`mysql_test`@`%` FUNCTION `double_input`(x INT) RETURNS int(11)
    COMMENT 'test function comment'
BEGIN DECLARE result INT; SET result = x * 2; RETURN result; END

CREATE DEFINER=`mysql_test`@`%` FUNCTION `double_input1`(x INT, y INT, z INT) RETURNS int(11)
    COMMENT 'test function comment'
BEGIN
DECLARE result1 INT;
DECLARE result2 INT;
DECLARE result3 INT;
SET result1 = x * y * z;
SET result2 = x + y + z;
SET result3 = x * y - z;
RETURN result1 + result2 + result3;
END;

CREATE  FUNCTION  name_from_employee1 (emp_id INT )
 RETURNS VARCHAR(20) LANGUAGE SQL
 BEGIN
 RETURN  (SELECT name FROM employee WHERE  num=emp_id );
 END;

CREATE  FUNCTION  name_from_employee1 (emp_id INT )
 RETURNS VARCHAR(20)
 BEGIN
 RETURN  (SELECT name FROM employee WHERE  num=emp_id );
 END;

 create function fun12345(p int) returns int begin while p>= 0 do set p=p-1;
insert into testdbc.testdd1 values(p); end while;  return p; end;

create function mysql_func1(s char(20)) returns char(50) deterministic return concat('mysql_func1, ',s,'!');