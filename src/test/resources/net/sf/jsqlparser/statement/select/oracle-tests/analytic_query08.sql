---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select manager_id, last_name, hire_date, 
   count(*) over (partition by manager_id order by hire_date 
   range numtodsinterval(100, 'day') preceding) as t_count 
   from employees

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT manager_id,last_name,hire_date,count(*)OVER(PARTITION BY manager_id ORDER BY hire_date RANGE numtodsinterval(100,'day')PRECEDING)AS t_count FROM employees recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select manager_id,last_name,hire_date,count(*)over(partition by manager_id order by hire_date range numtodsinterval(100,'day')preceding)as t_count from employees recorded first on 25 Oct 2021, 18:55:26