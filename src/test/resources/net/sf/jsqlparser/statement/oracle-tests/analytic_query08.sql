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
