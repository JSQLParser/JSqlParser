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

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: UPDATE customers_demo cd SET cust_address2_ntab=CAST(multiset(SELECT cust_address FROM customers c WHERE c.customer_id=cd.customer_id)AS cust_address_tab_typ) recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: update customers_demo cd set cust_address2_ntab=cast(multiset(select cust_address from customers c where c.customer_id=cd.customer_id)as cust_address_tab_typ) recorded first on 25 Oct 2021, 18:55:26