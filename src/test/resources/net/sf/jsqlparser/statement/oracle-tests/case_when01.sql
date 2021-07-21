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



--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Jul 21, 2021 9:47:13 AM