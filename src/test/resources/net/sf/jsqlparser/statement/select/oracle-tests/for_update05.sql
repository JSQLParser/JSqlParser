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
   for update of employee_id wait 10


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT employee_id FROM(SELECT employee_id+1 AS employee_id FROM employees)FOR UPDATE OF employee_id WAIT 10 recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select employee_id from(select employee_id+1 as employee_id from employees)for update of employee_id wait 10 recorded first on 25 Oct 2021, 18:55:26