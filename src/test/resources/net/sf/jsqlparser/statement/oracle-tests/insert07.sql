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
