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
