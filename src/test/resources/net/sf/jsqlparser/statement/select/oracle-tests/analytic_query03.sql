---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select times.time_id, product, quantity from inventory 
   partition by  (product) 
   right outer join times on (times.time_id = inventory.time_id) 
   where times.time_id between to_date('01/04/01', 'dd/mm/yy') 
      and to_date('06/04/01', 'dd/mm/yy') 
   order by  2,1



--@FAILURE: Encountered unexpected token: "by" "BY" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: Encountered: <K_BY> / "by", at line 11, column 14, in lexical state DEFAULT. recorded first on 23 May 2025, 22:04:10