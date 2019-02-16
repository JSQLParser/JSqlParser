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
               collect(ename order by ename)
                  as varchar2_ntt) as ordered_emps
     from   emp
     group  by
            deptno
