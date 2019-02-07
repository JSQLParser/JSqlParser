---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select decode(decode(decode( (select count(1) from dual), a, 1, 0), 0, 1), 1, 0) from dual
