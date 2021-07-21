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
     ,      cast(collect(job) as varchar2_ntt) as jobs
     from   emp
     group  by
            deptno

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Jul 21, 2021 9:47:13 AM