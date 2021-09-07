---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select *
from   (select customer_id, product_code, quantity
        from   pivot_test)
pivot xml (sum(quantity) as sum_quantity for product_code in (select distinct product_code
                                                                from   pivot_test))

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: select*from select customer_id,product_code,quantity from pivot_test pivot xml(sum(quantity)as sum_quantity for product_code in(select distinct product_code from pivot_test)) recorded first on Sep 7, 2021 7:58:09 AM