---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select * from persons p
       where value(p) is of type(only employee_t)
       

--@FAILURE: Encountered unexpected token: "is" "IS" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: select*from persons p where value(p) recorded first on 23 Apr 2022, 16:44:21