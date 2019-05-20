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
