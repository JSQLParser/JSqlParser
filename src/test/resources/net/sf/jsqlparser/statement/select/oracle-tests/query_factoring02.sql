---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
with
  reports_to_101 (eid, emp_last, mgr_id, reportlevel, mgr_list) 
  as
  (
     (select employee_id, last_name, manager_id, 0 reportlevel
     , cast(manager_id as varchar2(2000))
     from employees
     where employee_id = 101)
  union all
     (select e.employee_id, e.last_name, e.manager_id, reportlevel+1
     , cast(mgr_list || ',' || manager_id as varchar2(2000))
     from reports_to_101 r, employees e
     where r.eid = e.manager_id)
  )
select eid, emp_last, mgr_id, reportlevel, mgr_list
from reports_to_101
order by reportlevel, eid


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: WITH reports_to_101(eid,emp_last,mgr_id,reportlevel,mgr_list)AS((SELECT employee_id,last_name,manager_id,0 reportlevel,CAST(manager_id AS varchar2(2000))FROM employees WHERE employee_id=101)UNION ALL(SELECT e.employee_id,e.last_name,e.manager_id,reportlevel+1,CAST(mgr_list||','||manager_id AS varchar2(2000))FROM reports_to_101 r,employees e WHERE r.eid=e.manager_id))SELECT eid,emp_last,mgr_id,reportlevel,mgr_list FROM reports_to_101 ORDER BY reportlevel,eid recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: with reports_to_101(eid,emp_last,mgr_id,reportlevel,mgr_list)as((select employee_id,last_name,manager_id,0 reportlevel,cast(manager_id as varchar2(2000))from employees where employee_id=101)union all(select e.employee_id,e.last_name,e.manager_id,reportlevel+1,cast(mgr_list||','||manager_id as varchar2(2000))from reports_to_101 r,employees e where r.eid=e.manager_id))select eid,emp_last,mgr_id,reportlevel,mgr_list from reports_to_101 order by reportlevel,eid recorded first on 25 Oct 2021, 18:55:26