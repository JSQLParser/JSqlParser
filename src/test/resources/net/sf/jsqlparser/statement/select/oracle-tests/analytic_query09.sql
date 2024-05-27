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
   

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: Encountered unexpected token: "group" "GROUP" recorded first on May 27, 2024, 9:38:28 AM