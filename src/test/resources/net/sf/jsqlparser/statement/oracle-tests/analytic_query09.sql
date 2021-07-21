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
   listagg(column_value, ',') within group (order by column_value) 
from 
   table(
      cast(
         multiset(
            select 'one' from dual
            union all
            select 'two' from dual
         ) as t_str
      )
   )
   

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Jul 21, 2021 9:47:13 AM