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
--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Nov 14, 2022, 11:44:23 AM
--@FAILURE: Encountered unexpected token: "group" "GROUP" recorded first on May 27, 2024, 9:38:25 AM