with emp_count (eid, emp_last, mgr_id, mgrlevel, salary, cnt_employees) as (
  select employee_id, last_name, manager_id, 0 mgrlevel, salary, 0 cnt_employees
  from employees
  union all
  select e.employee_id, e.last_name, e.manager_id, r.mgrlevel+1 mgrlevel, e.salary, 1
  cnt_employees
  from emp_count r, employees e
  where e.employee_id = r.mgr_id)
search depth first by emp_last set order1
select emp_last, eid, mgr_id, salary, sum(cnt_employees), max(mgrlevel) mgrlevel
from emp_count
group by emp_last, eid, mgr_id, salary
having max(mgrlevel) > 0
order by mgr_id nulls first, emp_last
