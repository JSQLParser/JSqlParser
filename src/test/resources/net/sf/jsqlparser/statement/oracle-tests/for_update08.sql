---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select su.ttype ,su.cid ,su.s_id ,sessiontimezone
from sku su
where (nvl(su.up,'n')='n' and su.ttype=:b0)
for update of su.up
order by su.d
