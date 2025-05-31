---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select * from dual where 1 < > 2 and 1 ! = 2 and 1 ^ /*aaa */ = 2


--@FAILURE: Encountered unexpected token: "=" "=" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: Encountered unexpected token: "^" "^" recorded first on Jul 11, 2024, 9:09:49 AM
--@FAILURE: Encountered: "^" / "^", at line 10, column 52, in lexical state DEFAULT. recorded first on 15 May 2025, 16:24:08