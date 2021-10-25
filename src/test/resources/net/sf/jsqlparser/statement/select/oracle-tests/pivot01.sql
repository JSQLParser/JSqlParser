---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select * from pivot_table
  unpivot (yearly_total for order_mode in (store as 'direct',
           internet as 'online'))
  order by year, order_mode



--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT*FROM pivot_table UNPIVOT(yearly_total FOR order_mode IN(store AS 'direct',internet AS 'online'))ORDER BY year,order_mode recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select*from pivot_table unpivot(yearly_total for order_mode in(store as 'direct',internet as 'online'))order by year,order_mode recorded first on 25 Oct 2021, 18:55:27