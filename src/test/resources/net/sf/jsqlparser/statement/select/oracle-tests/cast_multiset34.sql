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
               collect(job)
                  as varchar2_ntt
               ) multiset union distinct varchar2_ntt() as distinct_jobs
     from   emp
     group  by
            deptno

--@FAILURE: Encountered unexpected token: "varchar2_ntt" <S_IDENTIFIER> recorded first on Aug 3, 2021, 7:20:08 AM