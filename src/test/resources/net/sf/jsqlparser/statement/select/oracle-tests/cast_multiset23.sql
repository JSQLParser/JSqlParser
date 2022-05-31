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
               collect(all job)
                  as varchar2_ntt) as distinct_jobs
     from   emp
     group  by
            deptno

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: java.lang.NullPointerException: Cannot invoke "net.sf.jsqlparser.expression.operators.relational.ExpressionList.getExpressions()" because "this.parameters" is null recorded first on 31 May 2022, 17:49:11