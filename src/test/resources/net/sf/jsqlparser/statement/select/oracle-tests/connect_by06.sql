---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select last_name "Employee", connect_by_root last_name "Manager",
   level-1 "Pathlen", sys_connect_by_path(last_name, '/') "Path"
   from employees
   where level > 1 and department_id = 110
   connect by prior employee_id = manager_id
   order by "Employee", "Manager", "Pathlen", "Path"
   

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 14, 2021 9:00:57 PM
--@FAILURE: SELECT last_name "Employee",CONNECT_BY_ROOT last_name "Manager",level-1 "Pathlen",sys_connect_by_path(last_name,'/')"Path" FROM employees WHERE level>1 AND department_id=110 CONNECT BY PRIOR employee_id=manager_id ORDER BY "Employee","Manager","Pathlen","Path" recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select last_name "employee",connect_by_root last_name "manager",level-1 "pathlen",sys_connect_by_path(last_name,'/')"path" from employees where level>1 and department_id=110 connect by prior employee_id=manager_id order by "employee","manager","pathlen","path" recorded first on 25 Oct 2021, 18:55:26