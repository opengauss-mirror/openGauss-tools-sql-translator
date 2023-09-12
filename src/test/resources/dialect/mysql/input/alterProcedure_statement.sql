alter procedure f1 comment 'procedure comment';
alter procedure f1 contains sql;
alter procedure f1 sql security definer;
alter procedure f1 sql security invoker;
alter procedure f1 language sql;
-- 以下三种druid不支持
-- alter procedure f1 no sql;
-- alter procedure f1 reads sql data;
-- alter procedure f1 modifies sql data;
