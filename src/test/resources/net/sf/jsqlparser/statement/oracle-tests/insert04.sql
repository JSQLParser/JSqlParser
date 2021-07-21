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

--@FAILURE: Encountered unexpected token: "into" "INTO" recorded first on Jul 21, 2021 9:47:13 AM