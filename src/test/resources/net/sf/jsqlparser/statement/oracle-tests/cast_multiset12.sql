---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
update customers_demo cd
  set cust_address2_ntab = 
    cast(multiset(select cust_address
                    from customers c
                    where c.customer_id =
                          cd.customer_id) as cust_address_tab_typ)

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Jul 21, 2021 9:47:13 AM