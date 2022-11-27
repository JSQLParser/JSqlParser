---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
with days as (select (select trunc(sysdate, 'MONTH') from dual) + rownum -1 as d from dual connect by rownum < 31)
select d from days where (trunc(d) - trunc(d,'IW') +1 ) not in (6,7) and d <= last_day(sysdate)

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:07 AM
--@FAILURE: java.lang.Error: Missing return statement in function recorded first on 26 Nov 2022, 17:21:00
--@FAILURE: Encountered unexpected token: "not" "NOT" recorded first on 27 Nov 2022, 11:47:11