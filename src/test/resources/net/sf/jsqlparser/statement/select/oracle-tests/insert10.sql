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

--@FAILURE: Encountered unexpected token: "(" "(" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: Encountered unexpected token: "insert" "INSERT" recorded first on Mar 26, 2023, 6:59:20 PM