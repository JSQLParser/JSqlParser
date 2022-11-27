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
   when not matched then insert (d.employee_id, d.bonus) 
     values (s.employee_id, s.salary)
     where (s.salary <= 8000)
   when matched then update set d.bonus = bonus 
     delete where (salary > 8000)


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: java.lang.Error: Missing return statement in function recorded first on 26 Nov 2022, 17:20:59