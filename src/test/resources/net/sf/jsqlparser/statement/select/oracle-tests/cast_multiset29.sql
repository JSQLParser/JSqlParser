---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select cast(
               collect(
                  distinct empsal_ot(ename, sal)
                  ) as empsal_ntt) as empsals
     from   emp

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT CAST(collect(DISTINCT empsal_ot(ename,sal))AS empsal_ntt)AS empsals FROM emp recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select cast(collect(distinct empsal_ot(ename,sal))as empsal_ntt)as empsals from emp recorded first on 25 Oct 2021, 18:55:26