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
--@FAILURE: Encountered unexpected token: "set" "SET" recorded first on 2023年12月23日 下午1:38:33
--@FAILURE: Encountered unexpected token: "plan" "PLAN" recorded first on 23 Aug 2024, 21:35:20
--@FAILURE: Encountered: <K_SET> / "set", at line 11, column 5, in lexical state DEFAULT. recorded first on 15 May 2025, 16:24:08