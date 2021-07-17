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
