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
--@FAILURE: WITH days AS(SELECT(SELECT trunc(sysdate,'MONTH')FROM dual)+rownum-1 AS d FROM dual CONNECT BY rownum<31)SELECT d FROM days WHERE(trunc(d)-trunc(d,'IW')+1)NOT IN(6,7)AND d<=last_day(sysdate) recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: with days as(select(select trunc(sysdate,'month')from dual)+rownum-1 as d from dual connect by rownum<31)select d from days where(trunc(d)-trunc(d,'iw')+1)not in(6,7)and d<=last_day(sysdate) recorded first on 25 Oct 2021, 18:55:27