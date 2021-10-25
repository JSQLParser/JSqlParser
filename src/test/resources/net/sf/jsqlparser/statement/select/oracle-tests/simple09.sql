---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select a||last_name,
        employee_id
    from employees
    start with job_id = 'ad_vp' 
    connect by prior employee_id = manager_id



--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT a||last_name,employee_id FROM employees START WITH job_id='ad_vp' CONNECT BY PRIOR employee_id=manager_id recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select a||last_name,employee_id from employees start with job_id='ad_vp' connect by prior employee_id=manager_id recorded first on 25 Oct 2021, 18:55:27