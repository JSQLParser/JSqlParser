---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select d.department_id, e.last_name
   from departments d, employees e
   where d.department_id = e.department_id(+)
   order by d.department_id, e.last_name



--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: Encountered: "+" / "+", at line 12, column 44, in lexical state DEFAULT. recorded first on 13 Feb 2026, 12:40:36