---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
delete from table_name
where current of cursor_name

--@FAILURE: Encountered unexpected token: "of" "OF" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: Encountered: <K_OF> / "of", at line 11, column 15, in lexical state DEFAULT. recorded first on 15 May 2025, 16:24:08