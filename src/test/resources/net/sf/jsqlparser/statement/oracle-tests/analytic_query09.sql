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
   