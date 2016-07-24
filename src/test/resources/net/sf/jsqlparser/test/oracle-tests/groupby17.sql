select fact_3_id,
       fact_4_id,
       sum(sales_value) as sales_value,
       grouping_id(fact_3_id, fact_4_id) as grouping_id
from   dimension_tab
group by grouping sets(fact_3_id, fact_4_id)
order by fact_3_id, fact_4_id