---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select fact_1_id,
       fact_2_id,
       sum(sales_value) as sales_value,
       grouping_id(fact_1_id, fact_2_id) as grouping_id,
       group_id() as group_id
from   dimension_tab
group by grouping sets(fact_1_id, cube (fact_1_id, fact_2_id))
order by fact_1_id, fact_2_id
