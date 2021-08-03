---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select deptno
	, ename
	, hiredate
	, listagg(ename, ',') within group (order by hiredate) over (partition by deptno) as employees
from emp

--@FAILURE: Encountered unexpected token: "by" "BY" recorded first on Aug 3, 2021, 7:20:08 AM