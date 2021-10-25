---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select staleness
, osize, obj#
, type#
, case when row_number() over (partition by bo# order by staleness, osize, obj#) = 1 then 64 else 0 end
  +
  case when row_number() over (partition by (select tcp0.bo# from tabcompart$ tcp0 where tcp0.obj#=st0.bo#) order by staleness, osize, obj#) = 1 then 32
  else 0 end aflags
, 0 status
, :b3 sid
, :b2 serial#
, part#, bo#
from st0

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT staleness,osize,obj#,type#,CASE WHEN row_number()OVER(PARTITION BY bo# ORDER BY staleness,osize,obj#)=1 THEN 64 ELSE 0 END+CASE WHEN row_number()OVER(PARTITION BY(SELECT tcp0.bo# FROM tabcompart$ tcp0 WHERE tcp0.obj#=st0.bo#)ORDER BY staleness,osize,obj#)=1 THEN 32 ELSE 0 END aflags,0 status,:b3 sid,:b2 serial#,part#,bo# FROM st0 recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select staleness,osize,obj#,type#,case when row_number()over(partition by bo# order by staleness,osize,obj#)=1 then 64 else 0 end+case when row_number()over(partition by(select tcp0.bo# from tabcompart$ tcp0 where tcp0.obj#=st0.bo#)order by staleness,osize,obj#)=1 then 32 else 0 end aflags,0 status,:b3 sid,:b2 serial#,part#,bo# from st0 recorded first on 25 Oct 2021, 18:55:26