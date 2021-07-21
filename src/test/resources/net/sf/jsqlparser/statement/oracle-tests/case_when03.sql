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
case (STaTUS)
when 'N' then 1
when 'B' then 2
when 'a' then 3
end as STaTE
from VaLUE
where KID=:B2 and RID=:B1


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Jul 21, 2021 9:47:12 AM