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


