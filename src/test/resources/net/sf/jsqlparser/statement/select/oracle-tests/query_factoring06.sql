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
   dept_costs as (
      select department_name, sum(salary) dept_total
         from employees e, departments d
         where e.department_id = d.department_id
      group by department_name),
   avg_cost as (
      select sum(dept_total)/count(*) avg
      from dept_costs)
select * from dept_costs
   where dept_total >
      (select avvg from avg_cost)
      order by department_name



--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: WITH dept_costs AS(SELECT department_name,sum(salary)dept_total FROM employees e,departments d WHERE e.department_id=d.department_id GROUP BY department_name),avg_cost AS(SELECT sum(dept_total)/count(*)avg FROM dept_costs)SELECT*FROM dept_costs WHERE dept_total>(SELECT avvg FROM avg_cost)ORDER BY department_name recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: with dept_costs as(select department_name,sum(salary)dept_total from employees e,departments d where e.department_id=d.department_id group by department_name),avg_cost as(select sum(dept_total)/count(*)avg from dept_costs)select*from dept_costs where dept_total>(select avvg from avg_cost)order by department_name recorded first on 25 Oct 2021, 18:55:26