---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select channels.channel_desc, countries.country_iso_code,
  to_char(sum(amount_sold), '9,999,999,999') sales$
from sales, customers, times, channels, countries
where sales.time_id=times.time_id and sales.cust_id=customers.cust_id and
  sales.channel_id= channels.channel_id and channels.channel_desc in
  ('direct sales', 'internet') and times.calendar_month_desc='2000-09'
  and customers.country_id=countries.country_id
  and countries.country_iso_code in ('us','fr')
group by cube(channels.channel_desc, countries.country_iso_code)

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Jul 21, 2021 9:47:13 AM