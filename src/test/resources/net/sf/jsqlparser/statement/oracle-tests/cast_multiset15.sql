select customer_id, cust_address_ntab
multiset union cust_address2_ntab multiset_union
from customers_demo
order by customer_id
