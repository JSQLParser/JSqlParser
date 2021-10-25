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

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT fact_1_id,fact_2_id,sum(sales_value)AS sales_value,grouping_id(fact_1_id,fact_2_id)AS grouping_id,group_id()AS group_id FROM dimension_tab GROUP BY GROUPING SETS(fact_1_id,cube(fact_1_id,fact_2_id))ORDER BY fact_1_id,fact_2_id recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select fact_1_id,fact_2_id,sum(sales_value)as sales_value,grouping_id(fact_1_id,fact_2_id)as grouping_id,group_id()as group_id from dimension_tab group by grouping sets(fact_1_id,cube(fact_1_id,fact_2_id))order by fact_1_id,fact_2_id recorded first on 25 Oct 2021, 18:55:26