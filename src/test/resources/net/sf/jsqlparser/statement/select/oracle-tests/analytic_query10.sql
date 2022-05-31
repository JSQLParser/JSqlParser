---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
SELECT STALENESS, 
       OSIZE, OBJ#, 
       TYPE#, 
       ROW_NUMBER() OVER (PARTITION BY BO# ORDER BY STALENESS, OSIZE, OBJ#),
       CASE WHEN ROW_NUMBER() OVER (PARTITION BY BO# ORDER BY STALENESS, OSIZE, OBJ#) = 1 THEN 64 ELSE 0 END + 
       CASE WHEN ROW_NUMBER() OVER (PARTITION BY (SELECT TCP0.BO# FROM TABCOMPART$ TCP0 WHERE TCP0.OBJ#=ST0.BO#) ORDER BY STALENESS, OSIZE, OBJ#) = 1 THEN 32 
       ELSE 0 END AFLAGS, 
       0 STATUS, 
       :B5 SID, 
       :B4 SERIAL#, PART#, BO#, LOC_STALE_PCT 
FROM 
A

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:07 AM
--@FAILURE: java.lang.NullPointerException: Cannot invoke "net.sf.jsqlparser.expression.operators.relational.ExpressionList.getExpressions()" because "this.parameters" is null recorded first on 31 May 2022, 17:49:11