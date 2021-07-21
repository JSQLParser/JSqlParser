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

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Jul 21, 2021 9:47:13 AM