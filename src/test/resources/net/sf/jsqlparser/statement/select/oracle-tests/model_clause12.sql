---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select organisation,
level1, level2, level3, level4 from
( select 'org_name' as organisation, level as org_level, ename
from emp connect by prior empno = mgr
start with ename = 'king')
model
return updated rows
partition by
(organisation)
dimension by (rownum rn)
measures (lpad(' ',10) level1,
lpad(' ',10) level2, lpad(' ',10) level3,
lpad(' ',10) level4, org_level, ename)
rules update
( level1[any] = case when org_level[cv()] = 1 then ename [cv()] end,
level2[any] = case when org_level[cv()] = 2 then ename [cv()] end,
level3[any] = case when org_level[cv()] = 3 then ename [cv()] end,
level4[any] = case when org_level[cv()] = 4 then ename [cv()] end )

--@FAILURE: Encountered unexpected token: "return" <S_IDENTIFIER> recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: Encountered unexpected token: "by" "BY" recorded first on 18 Oct 2021, 21:15:57