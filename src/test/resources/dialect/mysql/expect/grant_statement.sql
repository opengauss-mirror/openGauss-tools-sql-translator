-- *
-- *.*
GRANT ALL ON SCHEMA delphis TO 'usr_replica'@'%';
GRANT ALL ON ALL TABLES IN SCHEMA delphis TO 'usr_replica'@'%';
GRANT ALL ON ALL FUNCTIONS IN SCHEMA delphis TO 'usr_replica'@'%';
GRANT ALL ON ALL PROCEDURE IN SCHEMA delphis TO 'usr_replica'@'%';
GRANT ALTER, CREATE, DROP, USAGE ON SCHEMA delphis TO 'usr_replica'@'%' WITH GRANT OPTION;
GRANT ALTER, DELETE, DROP, INDEX, INSERT, REFERENCES, SELECT, UPDATE ON ALL TABLES IN SCHEMA delphis TO 'usr_replica'@'%' WITH GRANT OPTION;
GRANT ALTER, DROP, EXECUTE ON ALL FUNCTIONS IN SCHEMA delphis TO 'usr_replica'@'%' WITH GRANT OPTION;
GRANT ALTER, DROP, EXECUTE ON ALL PROCEDURE IN SCHEMA delphis TO 'usr_replica'@'%' WITH GRANT OPTION;
-- incompatible privilege
GRANT ALL ON "waitCopy" TO 'usr_replica'@'%';
GRANT ALTER, DELETE, DROP, INDEX, INSERT, REFERENCES, SELECT, UPDATE ON "waitCopy" TO 'usr_replica'@'%' WITH GRANT OPTION;
-- incompatible privilege
GRANT INSERT("ID"), REFERENCES(id), SELECT(id), UPDATE(id) ON "waitCopy" TO 'usr_replica'@'%';
GRANT ALTER, DELETE, DROP, INDEX, INSERT, REFERENCES, SELECT, UPDATE ON "waitCopy" TO 'usr_replica'@'%' WITH GRANT OPTION;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA delphis TO 'usr_replica'@'%' WITH GRANT OPTION;
GRANT EXECUTE ON ALL PROCEDURE IN SCHEMA delphis TO 'usr_replica'@'%' WITH GRANT OPTION;
-- single routine
-- single routine