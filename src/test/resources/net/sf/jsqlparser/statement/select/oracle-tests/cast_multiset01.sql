---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select t1.department_id, t2.* 
   from hr_info t1, 
   table
   (
    cast
    (
        multiset
        (
            select t3.last_name, t3.department_id, t3.salary 
            from people t3
            where t3.department_id = t1.department_id
        )
        as people_tab_typ
    )
) t2


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT t1.department_id,t2.*FROM hr_info t1,table(CAST(multiset(SELECT t3.last_name,t3.department_id,t3.salary FROM people t3 WHERE t3.department_id=t1.department_id)AS people_tab_typ))t2 recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select t1.department_id,t2.*from hr_info t1,table(cast(multiset(select t3.last_name,t3.department_id,t3.salary from people t3 where t3.department_id=t1.department_id)as people_tab_typ))t2 recorded first on 25 Oct 2021, 18:55:26