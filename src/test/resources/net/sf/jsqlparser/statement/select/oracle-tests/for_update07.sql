---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select employee_id from (select employee_id+1 as employee_id from employees)
   for update of a, b.c, d skip locked


--@FAILURE: Encountered unexpected token: "," "," recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: Encountered: <K_COMMA> / ",", at line 11, column 19, in lexical state DEFAULT. recorded first on 15 May 2025, 16:24:08