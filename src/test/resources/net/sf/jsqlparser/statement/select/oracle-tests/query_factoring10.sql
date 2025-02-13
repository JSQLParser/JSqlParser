---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
with o(obj,link) as
(
select 'a', 'b' from dual union all
select 'a', 'c' from dual union all
select      'c', 'd' from dual union all
select           'd', 'c' from dual union all
select           'd', 'e' from dual union all
select                'e', 'e' from dual
),
t(root,lev,obj,link,path) as (
select obj,1,obj,link,cast(obj||'->'||link 
as varchar2(4000))
from o 
where obj='a'  -- start with
union all
select 
  t.root,t.lev+1,o.obj,o.link,
  t.path||', '||o.obj||
    '->'
    ||o.link
from t, o 
where t.link=o.obj
)
search depth first by obj set ord
cycle obj set cycle to 1 default 0
select root,lev,obj,link,path,cycle,
    case
    when (lev - lead(lev) over (order by ord)) < 0
    then 0
    else 1
    end is_leaf
 from t


--@FAILURE: Encountered unexpected token: "search" <S_IDENTIFIER> recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: Encountered unexpected token: "union" "UNION" recorded first on Feb 13, 2025, 10:16:06 AM