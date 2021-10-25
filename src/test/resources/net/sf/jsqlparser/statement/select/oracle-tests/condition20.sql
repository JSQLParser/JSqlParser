---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select * from employees
  where (salary, salary) >=
  some ( select 1, 2 from dual )
  order by employee_id

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT*FROM employees WHERE(salary,salary)>=SOME(SELECT 1,2 FROM dual)ORDER BY employee_id recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select*from employees where(salary,salary)>=some(select 1,2 from dual)order by employee_id recorded first on 25 Oct 2021, 18:55:26