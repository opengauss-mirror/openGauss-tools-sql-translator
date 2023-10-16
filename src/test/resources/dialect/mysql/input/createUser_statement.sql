create user if not exists test1 identified by 'Test@123';
create user 'test2'@'localhost' identified by 'Test@123';
create user 'test3'@'127.0.0.1' identified by 'Test@123';
create user 'test4'@'127.0.0.%' identified by 'Test@123';