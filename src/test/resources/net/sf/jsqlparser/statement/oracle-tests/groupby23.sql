---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select channel_desc, calendar_month_desc, country_iso_code, to_char(
sum(amount_sold), '9,999,999,999') sales$, grouping(channel_desc) ch, grouping
  (calendar_month_desc)  mo, grouping(country_iso_code) co
from sales, customers, times, channels, countries
where sales.time_id=times.time_id and sales.cust_id=customers.cust_id 
  and customers.country_id = countries.country_id 
  and sales.channel_id= channels.channel_id 
  and channels.channel_desc in ('direct sales', 'internet') 
  and times.calendar_month_desc in ('2000-09', '2000-10') 
  and country_iso_code in ('gb', 'us')
group by cube(channel_desc, calendar_month_desc, country_iso_code)
having (grouping(channel_desc)=1 and grouping(calendar_month_desc)= 1 
  and grouping(country_iso_code)=1) or (grouping(channel_desc)=1 
  and grouping (calendar_month_desc)= 1) or (grouping(country_iso_code)=1
  and grouping(calendar_month_desc)= 1)

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Jul 21, 2021 9:47:13 AM