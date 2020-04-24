---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
insert into (
  select empno, ename, job, sal, deptno
  from emp)
  values
  (1, 'morgan', 'dba', '1', 40)
