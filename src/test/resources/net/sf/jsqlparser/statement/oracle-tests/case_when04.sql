---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select
case when row_number() over (partition by bo# order by staleness, osize, obj#) = 1 then 32 else 0 end + 64 aflags
from f
