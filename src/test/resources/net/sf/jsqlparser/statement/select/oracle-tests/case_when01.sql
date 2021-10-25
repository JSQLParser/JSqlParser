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
ROOT,LEV,OBJ,LinK,PaTH,cycle,
    case
    when (LEV - LEaD(LEV) over (order by orD)) < 0 then 0
    else 1
    end is_LEaF
from T



--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT ROOT,LEV,OBJ,LinK,PaTH,cycle,CASE WHEN(LEV-LEaD(LEV)OVER(ORDER BY orD))<0 THEN 0 ELSE 1 END is_LEaF FROM T recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select root,lev,obj,link,path,cycle,case when(lev-lead(lev)over(order by ord))<0 then 0 else 1 end is_leaf from t recorded first on 25 Oct 2021, 18:55:26