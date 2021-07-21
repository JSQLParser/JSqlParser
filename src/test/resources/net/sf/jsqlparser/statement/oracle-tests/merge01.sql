---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
merge into bonuses d 
   using (select employee_id.* from employees) s 
   on (employee_id = a) 
   when matched then update set d.bonus = bonus 
     delete where (salary > 8000)
   when not matched then insert (d.employee_id, d.bonus) 
     values (s.employee_id, s.salary)
     where (s.salary <= 8000)


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Jul 21, 2021 9:47:13 AM