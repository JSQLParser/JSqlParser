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
--@FAILURE: delete from table_name where current recorded first on 23 Apr 2022, 16:44:21