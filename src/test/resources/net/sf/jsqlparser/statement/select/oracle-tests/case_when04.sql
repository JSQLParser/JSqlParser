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

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT CASE WHEN row_number()OVER(PARTITION BY bo# ORDER BY staleness,osize,obj#)=1 THEN 32 ELSE 0 END+64 aflags FROM f recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select case when row_number()over(partition by bo# order by staleness,osize,obj#)=1 then 32 else 0 end+64 aflags from f recorded first on 25 Oct 2021, 18:55:26