---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
with rn as (
  select rownum rn
  from dual
  connect by level <= (select max(cases) from t1))
select pname
from t1, rn
where rn <= cases
order by pname

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: WITH rn AS(SELECT rownum rn FROM dual CONNECT BY level<=(SELECT max(cases)FROM t1))SELECT pname FROM t1,rn WHERE rn<=cases ORDER BY pname recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: with rn as(select rownum rn from dual connect by level<=(select max(cases)from t1))select pname from t1,rn where rn<=cases order by pname recorded first on 25 Oct 2021, 18:55:27