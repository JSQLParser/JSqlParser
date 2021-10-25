---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select lpad(' ',2*(level-1)) || last_name org_chart, 
        employee_id, manager_id, job_id
    from employees
    start with job_id = 'ad_vp' 
    connect by prior employee_id = manager_id



--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT lpad(' ',2*(level-1))||last_name org_chart,employee_id,manager_id,job_id FROM employees START WITH job_id='ad_vp' CONNECT BY PRIOR employee_id=manager_id recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select lpad(' ',2*(level-1))||last_name org_chart,employee_id,manager_id,job_id from employees start with job_id='ad_vp' connect by prior employee_id=manager_id recorded first on 25 Oct 2021, 18:55:26