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
   from m.departments d right outer join n.employees e
   on d.department_id = e.department_id
   order by d.department_id, e.last_name


