---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select value
from
(
 (select customer_id, product_code, quantity
        from   pivot_test)
 pivot  (sum(quantity) as sum_quantity for product_code in ('a' as a, 'b' as b, 'c' as c))
 )
--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Jul 21, 2021 10:33:10 AM