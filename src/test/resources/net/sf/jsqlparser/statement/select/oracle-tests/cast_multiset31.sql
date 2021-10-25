---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select e.deptno
     ,      cast(
               multiset(
                  select e2.ename
                  from   emp e2
                  where  e2.deptno = e.deptno
                  order  by
                         e2.hiredate
                  ) as varchar2_ntt) as ordered_emps
     from   emp e
     group  by
            e.deptno
     order  by
            e.deptno

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT e.deptno,CAST(multiset(SELECT e2.ename FROM emp e2 WHERE e2.deptno=e.deptno ORDER BY e2.hiredate)AS varchar2_ntt)AS ordered_emps FROM emp e GROUP BY e.deptno ORDER BY e.deptno recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select e.deptno,cast(multiset(select e2.ename from emp e2 where e2.deptno=e.deptno order by e2.hiredate)as varchar2_ntt)as ordered_emps from emp e group by e.deptno order by e.deptno recorded first on 25 Oct 2021, 18:55:26