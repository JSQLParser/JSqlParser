---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select (systimestamp - order_date) day(9) to second from orders
where order_id = 2458

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on 2026年8月10日 上午11:23:23