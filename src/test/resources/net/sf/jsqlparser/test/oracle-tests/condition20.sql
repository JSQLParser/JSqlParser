select * from employees
  where (salary, salary) >=
  some ( select 1, 2 from dual )
  order by employee_id
