select fact_1_id,
       fact_2_id,
       sum(sales_value) as sales_value,
       grouping(fact_1_id) as f1g, 
       grouping(fact_2_id) as f2g
from   dimension_tab
group by cube (fact_1_id, fact_2_id)
having grouping(fact_1_id) = 1 or grouping(fact_2_id) = 1
order by grouping(fact_1_id), grouping(fact_2_id)
