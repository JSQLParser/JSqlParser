---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
explain plan 
    set statement_id = 'raise in tokyo' 
    into plan_table 
    for update employees 
        set salary = salary * 1.10 
        where department_id =  
           (select department_id from departments
               where location_id = 1700)

--@FAILURE: Encountered unexpected token: "plan" <S_IDENTIFIER> recorded first on Aug 3, 2021, 7:20:08 AM