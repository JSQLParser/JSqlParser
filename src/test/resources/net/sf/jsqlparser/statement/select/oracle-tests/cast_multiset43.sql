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
,      avg(sal) avg_sal
,      cast
       ( collect(ename)
         as ename_type
       ) enames
from   emp
group
by     deptno

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT deptno,avg(sal)avg_sal,CAST(collect(ename)AS ename_type)enames FROM emp GROUP BY deptno recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select deptno,avg(sal)avg_sal,cast(collect(ename)as ename_type)enames from emp group by deptno recorded first on 25 Oct 2021, 18:55:26