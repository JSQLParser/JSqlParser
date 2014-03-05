-- insert all
insert all
into ap_cust values (customer_id, program_id, delivered_date)
into ap_orders values (order_date, program_id)
select program_id, delivered_date, customer_id, order_date
from airplanes
