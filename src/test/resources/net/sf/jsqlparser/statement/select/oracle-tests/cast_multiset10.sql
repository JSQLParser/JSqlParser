---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select cast(powermultiset(cust_address_ntab)
as cust_address_tab_tab_typ)
from customers_demo

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT CAST(powermultiset(cust_address_ntab)AS cust_address_tab_tab_typ)FROM customers_demo recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select cast(powermultiset(cust_address_ntab)as cust_address_tab_tab_typ)from customers_demo recorded first on 25 Oct 2021, 18:55:26