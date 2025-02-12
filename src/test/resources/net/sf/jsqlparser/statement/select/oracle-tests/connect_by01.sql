---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
with o as
(
	select 'a' obj, 'b' link from dual union all
	select 'a', 'c' from dual union all
	select      'c', 'd' from dual union all
	select           'd', 'c' from dual union all
	select           'd', 'e' from dual union all
	select                'e', 'e' from dual
)
select 
  connect_by_root obj root,
  level,
  obj,link,
  sys_connect_by_path(obj||'->'||link,','),
  connect_by_iscycle,
  connect_by_isleaf
from o
connect by nocycle obj=prior link
start with obj='a'

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 14, 2021 9:00:57 PM
--@FAILURE: Encountered unexpected token: "union" "UNION" recorded first on Feb 13, 2025, 10:16:06 AM