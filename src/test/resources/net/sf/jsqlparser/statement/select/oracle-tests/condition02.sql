---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select *
 from employees x 
 where salary > (select avg(salary) from x)
 and 1 = 1
 and hiredate = sysdate
 and to_yminterval('01-00') < sysdate
 and to_yminterval('01-00') + x < sysdate


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT*FROM employees x WHERE salary>(SELECT avg(salary)FROM x)AND 1=1 AND hiredate=sysdate AND to_yminterval('01-00')<sysdate AND to_yminterval('01-00')+x<sysdate recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select*from employees x where salary>(select avg(salary)from x)and 1=1 and hiredate=sysdate and to_yminterval('01-00')<sysdate and to_yminterval('01-00')+x<sysdate recorded first on 25 Oct 2021, 18:55:26