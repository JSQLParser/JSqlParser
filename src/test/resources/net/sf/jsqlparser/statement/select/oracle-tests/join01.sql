---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select d.department_id as d_dept_id, e.department_id as e_dept_id, e.last_name
   from departments d full outer join employees e
   on d.department_id = e.department_id
   order by d.department_id, e.last_name


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM