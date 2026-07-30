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

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Jul 30, 2026, 8:19:20 AM