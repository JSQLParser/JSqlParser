---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select customer_id, cust_address_ntab
multiset union cust_address2_ntab multiset_union
from customers_demo
order by customer_id

--@FAILURE: Encountered unexpected token: "union" "UNION" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: net.sf.jsqlparser.parser.ParseException: Encountered unexpected token: "union" "UNION" recorded first on 21 Dec 2021, 15:15:16