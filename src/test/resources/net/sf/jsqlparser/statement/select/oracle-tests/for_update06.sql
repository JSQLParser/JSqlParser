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
   for update of employee_id skip locked


--@FAILURE: Encountered unexpected token: "skip" "SKIP" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: select employee_id from(select employee_id+1 as employee_id from employees)for update of employee_id recorded first on 23 Apr 2022, 16:44:21