---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
with dup_hiredate (eid, emp_last, mgr_id, reportlevel, hire_date, job_id) as (
  select employee_id, last_name, manager_id, 0 reportlevel, hire_date, job_id
  from employees
  where manager_id is null
  union all
  select e.employee_id, e.last_name, e.manager_id,
  r.reportlevel+1 reportlevel, e.hire_date, e.job_id
  from dup_hiredate r, employees e
  where r.eid = e.manager_id)
search depth first by hire_date set order1
cycle hire_date set is_cycle to 'y' default 'n'
select lpad(' ',2*reportlevel)||emp_last emp_name, eid, mgr_id, hire_date, job_id, is_cycle
from dup_hiredate
order by order1

--@FAILURE: Encountered unexpected token: "search" <S_IDENTIFIER> recorded first on Jul 21, 2021 9:47:13 AM