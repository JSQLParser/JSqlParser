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

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT*FROM(SELECT product_code,quantity FROM pivot_test)PIVOT XML(sum(quantity)AS sum_quantity FOR product_code IN(SELECT DISTINCT product_code FROM pivot_test WHERE id<10)) recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select*from(select product_code,quantity from pivot_test)pivot xml(sum(quantity)as sum_quantity for product_code in(select distinct product_code from pivot_test where id<10)) recorded first on 25 Oct 2021, 18:55:26