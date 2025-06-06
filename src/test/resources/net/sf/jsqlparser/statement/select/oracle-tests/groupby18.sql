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
       fact_3_id,
       fact_4_id,
       sum(sales_value) as sales_value,
       grouping_id(fact_1_id, fact_2_id, fact_3_id, fact_4_id) as grouping_id
from   dimension_tab
group by grouping sets(fact_1_id, fact_2_id), grouping sets(fact_3_id, fact_4_id)
order by fact_1_id, fact_2_id, fact_3_id, fact_4_id

--@FAILURE: Encountered unexpected token: "," "," recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: Encountered: <K_COMMA> / ",", at line 17, column 45, in lexical state DEFAULT. recorded first on 15 May 2025, 16:24:08