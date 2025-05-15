---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select time_id, product
   , last_value(quantity ignore nulls) over (partition by product order by time_id) quantity
   , last_value(quantity respect nulls) over (partition by product order by time_id) quantity
   from ( select times.time_id, product, quantity 
             from inventory partition by  (product) 
                right outer join times on (times.time_id = inventory.time_id) 
   where times.time_id between to_date('01/04/01', 'dd/mm/yy') 
      and to_date('06/04/01', 'dd/mm/yy')) 
   order by  2,1



--@FAILURE: Encountered unexpected token: "(" "(" recorded first on Aug 3, 2021, 7:20:07 AM
--@FAILURE: Encountered unexpected token: "by" "BY" recorded first on Apr 6, 2024, 7:38:53 AM
--@FAILURE: Encountered: <K_BY> / "by", at line 14, column 39, in lexical state DEFAULT. recorded first on 15 May 2025, 16:24:08