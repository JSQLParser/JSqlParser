---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
 select metric_id ,bsln_guid ,timegroup ,obs_value as obs_value 
 , cume_dist () over (partition by metric_id, bsln_guid, timegroup order by obs_value ) as cume_dist 
 , count(1) over (partition by metric_id, bsln_guid, timegroup ) as n 
 , row_number () over (partition by metric_id, bsln_guid, timegroup order by obs_value) as rrank 
 , percentile_disc(:b7 ) within group (order by obs_value asc) over (partition by metric_id, bsln_guid, timegroup) as mid_tail_value 
 , max(obs_value) over (partition by metric_id, bsln_guid, timegroup ) as max_val 
 , min(obs_value) over (partition by metric_id, bsln_guid, timegroup ) as min_val 
 , avg(obs_value) over (partition by metric_id, bsln_guid, timegroup ) as avg_val 
 , stddev(obs_value) over (partition by metric_id, bsln_guid, timegroup ) as sdev_val 
 , percentile_cont(0.25) within group (order by obs_value asc) over (partition by metric_id, bsln_guid, timegroup) as pctile_25 
 , percentile_cont(0.5)  within group (order by obs_value asc) over (partition by metric_id, bsln_guid, timegroup) as pctile_50 
 , percentile_cont(0.75) within group (order by obs_value asc) over (partition by metric_id, bsln_guid, timegroup) as pctile_75 
 , percentile_cont(0.90) within group (order by obs_value asc) over (partition by metric_id, bsln_guid, timegroup) as pctile_90 
 , percentile_cont(0.95) within group (order by obs_value asc) over (partition by metric_id, bsln_guid, timegroup) as pctile_95 
 , percentile_cont(0.99) within group (order by obs_value asc) over (partition by metric_id, bsln_guid, timegroup) as pctile_99
 from timegrouped_rawdata d


--@FAILURE: Encountered unexpected token: "by" "BY" recorded first on Jul 21, 2021 9:47:13 AM