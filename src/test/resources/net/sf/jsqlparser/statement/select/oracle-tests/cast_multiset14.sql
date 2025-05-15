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
multiset intersect all cust_address2_ntab multiset_intersect
from customers_demo
order by customer_id

--@FAILURE: Encountered unexpected token: "intersect" "INTERSECT" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: Encountered unexpected token: "all" "ALL" recorded first on Mar 25, 2023, 9:18:30 AM
--@FAILURE: Encountered unexpected token: "cust_address2_ntab" <S_IDENTIFIER> recorded first on Feb 8, 2025, 6:46:21 AM
--@FAILURE: Encountered: <S_IDENTIFIER> / "cust_address2_ntab", at line 11, column 24, in lexical state DEFAULT. recorded first on 15 May 2025, 16:24:08