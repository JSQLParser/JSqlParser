---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
insert first
when customer_id < 'i' then
  into cust_ah
  values (customer_id, program_id, delivered_date)
when customer_id < 'q' then
  into cust_ip
  values (customer_id, program_id, delivered_date)
when customer_id > 'pzzz' then
  into cust_qz
  values (customer_id, program_id, delivered_date)
select program_id, delivered_date, customer_id, order_date
from airplanes

--@FAILURE: Encountered unexpected token: "when" "WHEN" recorded first on Aug 3, 2021, 7:20:08 AM