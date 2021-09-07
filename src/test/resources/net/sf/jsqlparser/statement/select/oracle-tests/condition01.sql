---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select department_id, last_name, salary 
 from employees x 
 where salary > (select avg(salary) 
 from employees 
 where x.department_id = department_id) 
 order by department_id


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: select department_id,last_name,salary from employees x where salary>select avg(salary)from employees where x.department_id=department_id order by department_id recorded first on Sep 7, 2021 7:58:09 AM