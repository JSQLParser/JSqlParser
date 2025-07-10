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
--@FAILURE: Encountered unexpected token: "union" "UNION" recorded first on Feb 13, 2025, 10:16:06 AM
--@FAILURE: Encountered: <S_IDENTIFIER> / "varchar2_ntt", at line 14, column 42, in lexical state DEFAULT. recorded first on 15 May 2025, 16:24:08