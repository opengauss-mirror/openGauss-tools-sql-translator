create table test1(
    `id` int(10) auto_increment primary key ,
    name char(2) not null default  'GA' unique comment 'char' column_format fixed storage disk
);
create table testIndexKey (
    id int,
    name char(2),
    INDEX idIndex USING BTREE (id),
    KEY  nameIndex USING HASH (name)
);
create table testIndexKey (
    id int,
    INDEX idIndex USING BTREE (id),
    name char(2),
    KEY  nameIndex USING HASH (name)
);
create table testPrimaryUniqueKey(
    id int,
    name char(2),
    parent_id int,
    constraint  pk_id primary key pk_index using btree (id),
    col3 int,
    constraint  unique_constraint  unique index   (name) ,
    col4 int,
    constraint  fk_pid foreign key  (parent_id) references parent(id) match simple  on delete  cascade,
    col5 int,
    constraint  ck_con check(id>0) Not enforced
);
create table testIndexKey(
    id int,
    name char(2),
    t text,
    index id_index using btree (id),
    key name_key using hash(name),
    col4 int,
    fulltext test_index (t)
);


