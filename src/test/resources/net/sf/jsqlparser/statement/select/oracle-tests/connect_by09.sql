---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select t.*, prior t.id parent_id
from test t
start with t.id = 1
connect by prior t.id = t.parent_id
order siblings by t.some_text
--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Oct 2, 2024, 8:14:31 PM