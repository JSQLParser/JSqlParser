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
               set(collect(job))
                  as varchar2_ntt) as distinct_jobs
     from   emp
     group  by
            deptno

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT deptno,CAST(set(collect(job))AS varchar2_ntt)AS distinct_jobs FROM emp GROUP BY deptno recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select deptno,cast(set(collect(job))as varchar2_ntt)as distinct_jobs from emp group by deptno recorded first on 25 Oct 2021, 18:55:26