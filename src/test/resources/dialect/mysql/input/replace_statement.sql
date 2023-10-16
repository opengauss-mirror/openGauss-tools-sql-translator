drop table replace_test;
CREATE TABLE replace_test(
    id int NOT NULL,
    name varchar(20) DEFAULT NULL,
	key1 varchar(20) DEFAULT NULL,
    PRIMARY KEY(id)
);
replace into replace_test values(1, 'aaaaa', 'aaaaa');
replace into replace_test values(6, 'bbbbb', 'bbbbb');
replace into replace_test values(15, 'ccccc', 'ccccc');
replace into replace_test set id=1, name='aaaaa', key1='bbbbb';
replace into replace_test values(3, default, 'ccccc'),(4, 'ddddd', 'ddddd'),(5, 'eeeee', 'eeeee');
replace into replace_test select * from replace_test;