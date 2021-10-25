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
--@FAILURE: MERGE INTO bonuses d USING(SELECT employee_id.*FROM employees)s ON(employee_id=a)WHEN NOT MATCHED THEN INSERT(d.employee_id,d.bonus)VALUES(s.employee_id,s.salary)WHERE(s.salary<=8000)WHEN MATCHED THEN UPDATE SET d.bonus=bonus DELETE WHERE(salary>8000) recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: merge into bonuses d using(select employee_id.*from employees)s on(employee_id=a)when not matched then insert(d.employee_id,d.bonus)values(s.employee_id,s.salary)where(s.salary<=8000)when matched then update set d.bonus=bonus delete where(salary>8000) recorded first on 25 Oct 2021, 18:55:26