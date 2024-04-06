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
--@FAILURE: select*from pivot_tableunpivot(yearly_total for order_mode in(store as 'direct',internet as 'online'))order by year,order_mode recorded first on Jul 12, 2023, 12:58:42 PM
--@FAILURE: Encountered unexpected token: "\'direct\'" <S_CHAR_LITERAL> recorded first on Apr 6, 2024, 7:50:18 AM