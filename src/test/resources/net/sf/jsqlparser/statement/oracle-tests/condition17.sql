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

--@FAILURE: Encountered unexpected token: "of" "OF" recorded first on Jul 21, 2021 9:47:13 AM