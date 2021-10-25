---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select department_id as d_e_dept_id, e.last_name
   from departments d full outer join employees e
   using (department_id)
   order by department_id, e.last_name



--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT department_id AS d_e_dept_id,e.last_name FROM departments d FULL OUTER JOIN employees e USING(department_id)ORDER BY department_id,e.last_name recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select department_id as d_e_dept_id,e.last_name from departments d full outer join employees e using(department_id)order by department_id,e.last_name recorded first on 25 Oct 2021, 18:55:26