---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
-- insert all
insert all
into ap_cust values (customer_id, program_id, delivered_date)
into ap_orders values (order_date, program_id)
select program_id, delivered_date, customer_id, order_date
from airplanes

--@FAILURE: Encountered unexpected token: "into" "INTO" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: Encountered unexpected token: "ap_cust" <S_IDENTIFIER> recorded first on 24 Oct 2021, 16:56:39
--@FAILURE: Encountered unexpected token: "insert" "INSERT" recorded first on Mar 26, 2023, 6:59:20 PM
--@FAILURE: Encountered: <K_INSERT> / "insert", at line 11, column 1, in lexical state DEFAULT. recorded first on 15 May 2025, 16:24:08