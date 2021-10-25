---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select :b3 as l_snap_id , :b2 as p_dbid , :b1 as p_instance_number , nvl(pid, -9) pid , nvl(serial#, -9) serial# , decode(pid, null, null, max(spid)) spid ,
 decode(pid, null, null, max(program)) program , decode(pid, null, null, max(background)) background , sum(pga_used_mem) pga_used_mem ,
 sum(pga_alloc_mem) pga_alloc_mem , sum(pga_freeable_mem) pga_freeable_mem , max(pga_alloc_mem) max_pga_alloc_mem , max(pga_max_mem) max_pga_max_mem ,
 decode(pid, null, avg(pga_alloc_mem), null) avg_pga_alloc_mem , decode(pid, null, stddev(pga_alloc_mem), null) stddev_pga_alloc_mem ,
 decode(pid, null, count(pid), null) num_processes
 from v$process
 where program != 'pseudo'
 group by grouping sets ( (pid, serial#), () )


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT :b3 AS l_snap_id,:b2 AS p_dbid,:b1 AS p_instance_number,nvl(pid,-9)pid,nvl(serial#,-9)serial#,decode(pid,NULL,NULL,max(spid))spid,decode(pid,NULL,NULL,max(program))program,decode(pid,NULL,NULL,max(background))background,sum(pga_used_mem)pga_used_mem,sum(pga_alloc_mem)pga_alloc_mem,sum(pga_freeable_mem)pga_freeable_mem,max(pga_alloc_mem)max_pga_alloc_mem,max(pga_max_mem)max_pga_max_mem,decode(pid,NULL,avg(pga_alloc_mem),NULL)avg_pga_alloc_mem,decode(pid,NULL,stddev(pga_alloc_mem),NULL)stddev_pga_alloc_mem,decode(pid,NULL,count(pid),NULL)num_processes FROM v$process WHERE program!='pseudo' GROUP BY GROUPING SETS((pid,serial#),()) recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select :b3 as l_snap_id,:b2 as p_dbid,:b1 as p_instance_number,nvl(pid,-9)pid,nvl(serial#,-9)serial#,decode(pid,null,null,max(spid))spid,decode(pid,null,null,max(program))program,decode(pid,null,null,max(background))background,sum(pga_used_mem)pga_used_mem,sum(pga_alloc_mem)pga_alloc_mem,sum(pga_freeable_mem)pga_freeable_mem,max(pga_alloc_mem)max_pga_alloc_mem,max(pga_max_mem)max_pga_max_mem,decode(pid,null,avg(pga_alloc_mem),null)avg_pga_alloc_mem,decode(pid,null,stddev(pga_alloc_mem),null)stddev_pga_alloc_mem,decode(pid,null,count(pid),null)num_processes from v$process where program!='pseudo' group by grouping sets((pid,serial#),()) recorded first on 25 Oct 2021, 18:55:26