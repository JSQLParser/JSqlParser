---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select e1.last_name from employees e1
 where f( cursor(select e2.hire_date from employees e2 where e1.employee_id = e2.manager_id), e1.hire_date) = 1
order by last_name

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM