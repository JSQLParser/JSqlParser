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
--@FAILURE: Encountered: <K_IS> / "is", at line 11, column 23, in lexical state DEFAULT. recorded first on 15 May 2025, 16:24:08