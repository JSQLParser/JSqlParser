---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
update emp
set ename = lower(ename)
where job = :jobs(i)
returning empno
bulk collect into :empnos

--@FAILURE: Encountered unexpected token: "(" "(" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: update emp set ename=lower(ename)where job=:jobs recorded first on 23 Apr 2022, 16:44:21