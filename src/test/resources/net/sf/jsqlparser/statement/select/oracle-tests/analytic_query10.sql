---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
SELECT STALENESS, 
       OSIZE, OBJ#, 
       TYPE#, 
       ROW_NUMBER() OVER (PARTITION BY BO# ORDER BY STALENESS, OSIZE, OBJ#),
       CASE WHEN ROW_NUMBER() OVER (PARTITION BY BO# ORDER BY STALENESS, OSIZE, OBJ#) = 1 THEN 64 ELSE 0 END + 
       CASE WHEN ROW_NUMBER() OVER (PARTITION BY (SELECT TCP0.BO# FROM TABCOMPART$ TCP0 WHERE TCP0.OBJ#=ST0.BO#) ORDER BY STALENESS, OSIZE, OBJ#) = 1 THEN 32 
       ELSE 0 END AFLAGS, 
       0 STATUS, 
       :B5 SID, 
       :B4 SERIAL#, PART#, BO#, LOC_STALE_PCT 
FROM 
A

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:07 AM
--@FAILURE: select staleness,osize,obj#,type#,row_number()over(partition by bo# order by staleness,osize,obj#),case when row_number()over(partition by bo# order by staleness,osize,obj#)=1 then 64 else 0 end+case when row_number()over(partition by select tcp0.bo# from tabcompart$ tcp0 where tcp0.obj#=st0.bo# order by staleness,osize,obj#)=1 then 32 else 0 end aflags,0 status,:b5 sid,:b4 serial#,part#,bo#,loc_stale_pct from a recorded first on Sep 7, 2021 7:58:10 AM