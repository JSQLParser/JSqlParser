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
--@FAILURE: SELECT listagg(column_value,',')WITHIN GROUP(ORDER BY column_value)FROM table(CAST(multiset(SELECT 'one' FROM dual UNION ALL SELECT 'two' FROM dual)AS t_str)) recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select listagg(column_value,',')within group(order by column_value)from table(cast(multiset(select 'one' from dual union all select 'two' from dual)as t_str)) recorded first on 25 Oct 2021, 18:55:26