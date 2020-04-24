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
from   (select product_code, quantity
        from   pivot_test)
pivot xml (sum(quantity) as sum_quantity for product_code in (select distinct product_code
                                                                from   pivot_test
                                                                where  id < 10))
