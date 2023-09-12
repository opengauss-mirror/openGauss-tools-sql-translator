-- *.*
REVOKE ALL ON SCHEMA mysql_database FROM 'mysql_test'@'%';
REVOKE ALL ON ALL FUNCTIONS IN SCHEMA mysql_database FROM 'mysql_test'@'%';
REVOKE ALL ON ALL PROCEDURE IN SCHEMA mysql_database FROM 'mysql_test'@'%';
REVOKE ALL ON ALL TABLES IN SCHEMA mysql_database FROM 'mysql_test'@'%';
REVOKE ALTER, CREATE, DROP, USAGE ON SCHEMA mysql_database FROM 'mysql_test'@'%';
REVOKE ALTER, DROP, EXECUTE ON ALL FUNCTIONS IN SCHEMA mysql_database FROM 'mysql_test'@'%';
REVOKE ALTER, DROP, EXECUTE ON ALL PROCEDURE IN SCHEMA mysql_database FROM 'mysql_test'@'%';
REVOKE ALTER, DELETE, DROP, INDEX, INSERT, REFERENCES, SELECT, UPDATE ON ALL TABLES IN SCHEMA mysql_database FROM 'mysql_test'@'%';
-- incompatible privilege
-- incompatible privilege
REVOKE ALL ON "testRevoke" FROM 'mysql_test'@'%';
REVOKE ALTER, DELETE, DROP, INDEX, INSERT, REFERENCES, SELECT, UPDATE ON "testRevoke" FROM 'mysql_test'@'%';
REVOKE INSERT(id), REFERENCES(id), SELECT(id), UPDATE(id) ON "testRevoke" FROM 'mysql_test'@'%';
REVOKE ALTER, DELETE, DROP, INDEX, INSERT, REFERENCES, SELECT, UPDATE ON "testRevoke" FROM 'mysql_test'@'%';
REVOKE EXECUTE ON ALL FUNCTIONS IN SCHEMA mysql_database FROM 'mysql_test'@'%';
REVOKE EXECUTE ON ALL PROCEDURE IN SCHEMA mysql_database FROM 'mysql_test'@'%';
-- single rountine
-- single rountine
REVOKE  ALL PRIVILEGE  FROM 'mysql_test'@'%';