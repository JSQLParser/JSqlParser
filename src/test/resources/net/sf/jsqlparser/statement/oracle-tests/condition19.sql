select * from employees
  where (salary, salary) >=
  some ( 1400, 3000)
  order by employee_id
  