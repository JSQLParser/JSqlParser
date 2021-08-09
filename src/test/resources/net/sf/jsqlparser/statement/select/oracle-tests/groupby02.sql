---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select
	decode(pid, null, null, max(program)) program , decode(pid, null, null, max(background)) background , sum(pga_used_mem) pga_used_mem ,
	sum(pga_alloc_mem) pga_alloc_mem , sum(pga_freeable_mem) pga_freeable_mem , max(pga_alloc_mem) max_pga_alloc_mem , max(pga_max_mem) max_pga_max_mem ,
	decode(pid, null, avg(pga_alloc_mem), null) avg_pga_alloc_mem , decode(pid, null, stddev(pga_alloc_mem), null) stddev_pga_alloc_mem ,
	decode(pid, null, count(pid), null) num_processes
from v$process
where program != 'pseudo'
group by grouping sets ( (), ((pid+1), serial#) )


														

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM