---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
with col_generator as (
select t1.batch_id, decode(t1.action, 'sent', t1.actdate) sent,
decode(t2.action,'recv', t2.actdate) received
from test t1, test t2
where t2.batch_id(+) = t1.batch_id)
select batch_id, max(sent) sent, max(received) received
from col_generator
group by batch_id
order by 1

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: WITH col_generator AS(SELECT t1.batch_id,decode(t1.action,'sent',t1.actdate)sent,decode(t2.action,'recv',t2.actdate)received FROM test t1,test t2 WHERE t2.batch_id(+)=t1.batch_id)SELECT batch_id,max(sent)sent,max(received)received FROM col_generator GROUP BY batch_id ORDER BY 1 recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: with col_generator as(select t1.batch_id,decode(t1.action,'sent',t1.actdate)sent,decode(t2.action,'recv',t2.actdate)received from test t1,test t2 where t2.batch_id(+)=t1.batch_id)select batch_id,max(sent)sent,max(received)received from col_generator group by batch_id order by 1 recorded first on 25 Oct 2021, 18:55:27