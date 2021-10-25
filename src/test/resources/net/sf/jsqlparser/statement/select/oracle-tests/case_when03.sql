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


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT CASE(STaTUS)WHEN 'N' THEN 1 WHEN 'B' THEN 2 WHEN 'a' THEN 3 END AS STaTE FROM VaLUE WHERE KID=:B2 AND RID=:B1 recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select case(status)when 'n' then 1 when 'b' then 2 when 'a' then 3 end as state from value where kid=:b2 and rid=:b1 recorded first on 25 Oct 2021, 18:55:26