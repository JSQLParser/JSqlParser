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
     ,      cast(
               collect(
                  empsal_ot(ename, sal) order by sal
                  ) as empsal_ntt) as empsals
     from   emp
     group  by
            deptno

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT deptno,CAST(collect(empsal_ot(ename,sal)ORDER BY sal)AS empsal_ntt)AS empsals FROM emp GROUP BY deptno recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select deptno,cast(collect(empsal_ot(ename,sal)order by sal)as empsal_ntt)as empsals from emp group by deptno recorded first on 25 Oct 2021, 18:55:26