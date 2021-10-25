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
 or  a=b
 and d=e


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM