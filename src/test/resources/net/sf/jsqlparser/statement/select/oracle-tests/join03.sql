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



--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT d.department_id,e.last_name FROM m.departments d RIGHT OUTER JOIN n.employees e ON d.department_id=e.department_id ORDER BY d.department_id,e.last_name recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select d.department_id,e.last_name from m.departments d right outer join n.employees e on d.department_id=e.department_id order by d.department_id,e.last_name recorded first on 25 Oct 2021, 18:55:26