update customers_demo cd
  set cust_address2_ntab = 
    cast(multiset(select cust_address
                    from customers c
                    where c.customer_id =
                          cd.customer_id) as cust_address_tab_typ)
