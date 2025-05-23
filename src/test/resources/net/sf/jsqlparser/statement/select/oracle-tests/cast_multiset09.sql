---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
update customers_demo
set cust_address_ntab = cust_address_ntab multiset union cust_address_ntab

--@FAILURE: Encountered unexpected token: "multiset" <S_IDENTIFIER> recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: Encountered: <S_IDENTIFIER> / "multiset", at line 11, column 43, in lexical state DEFAULT. recorded first on 23 May 2025, 22:04:10