---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select fact_3_id,
       fact_4_id,
       sum(sales_value) as sales_value,
       grouping_id(fact_3_id, fact_4_id) as grouping_id
from   dimension_tab
group by grouping sets(fact_3_id, fact_4_id)
order by fact_3_id, fact_4_id

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM