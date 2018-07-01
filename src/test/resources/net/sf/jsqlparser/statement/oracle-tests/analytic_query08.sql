select manager_id, last_name, hire_date, 
   count(*) over (partition by manager_id order by hire_date 
   range numtodsinterval(100, 'day') preceding) as t_count 
   from employees