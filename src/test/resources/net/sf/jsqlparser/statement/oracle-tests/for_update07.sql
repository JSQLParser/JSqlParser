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


--@FAILURE: Encountered unexpected token: "," "," recorded first on Jul 21, 2021 9:47:12 AM