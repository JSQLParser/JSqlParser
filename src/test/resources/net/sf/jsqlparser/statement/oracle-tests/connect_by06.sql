select last_name "Employee", connect_by_root last_name "Manager",
   level-1 "Pathlen", sys_connect_by_path(last_name, '/') "Path"
   from employees
   where level > 1 and department_id = 110
   connect by prior employee_id = manager_id
   order by "Employee", "Manager", "Pathlen", "Path"
   