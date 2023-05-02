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
       grouping(fact_1_id) as f1g, 
       grouping(fact_2_id) as f2g
from   dimension_tab
group by cube (fact_1_id, fact_2_id)
having grouping(fact_1_id) = 1 or grouping(fact_2_id) = 1
order by grouping(fact_1_id), grouping(fact_2_id)

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM