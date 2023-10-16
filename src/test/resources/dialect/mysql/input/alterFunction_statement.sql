alter function f1 comment 'function comment';
alter function f1 contains sql;
alter function f1 sql security definer;
alter function f1 sql security invoker;
alter function f1 language sql;
-- 以下三种 druid不支持
-- alter function f1 no sql;
-- alter function f1 reads sql data;
-- alter function f1 modifies sql data;