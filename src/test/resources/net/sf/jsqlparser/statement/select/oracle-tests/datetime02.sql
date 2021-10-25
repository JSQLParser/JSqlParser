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
	dbin.db_name,
	dbin.instance_name,
	dbin.version,
	case when s1.startup_time = s2.startup_time             then 0 else 1 end as bounce,
        cast(s1.end_interval_time as date) as begin_time,
	cast(s2.end_interval_time as date) as end_time,
	round((cast( (case when s2.end_interval_time > s1.end_interval_time then s2.end_interval_time else s1.end_interval_time end) as date)
		- cast(s1.end_interval_time as date)) * 86400) as int_secs,
	case when (s1.status <> 0 or s2.status <> 0) then 1 else 0 end as err_detect,
        round( greatest( (extract(day from s2.flush_elapsed) * 86400)
	       + (extract(hour from s2.flush_elapsed) * 3600)
	       + (extract(minute from s2.flush_elapsed) * 60)
	       + extract(second from s2.flush_elapsed),
	       (extract(day from s1.flush_elapsed) * 86400)
	       + (extract(hour from s1.flush_elapsed) * 3600)
	       + (extract(minute from s1.flush_elapsed) * 60)
	       + extract(second from s1.flush_elapsed),0 )
	) as max_flush_secs
from wrm$_snapshot s1 ,  wrm$_database_instance dbin ,  wrm$_snapshot s2
where s1.dbid = :dbid
and s2.dbid = :dbid
and s1.instance_number = :inst_num
and s2.instance_number = :inst_num
and s1.snap_id  = :bid
and s2.snap_id = :eid
and dbin.dbid = s1.dbid
and dbin.instance_number = s1.instance_number
and dbin.startup_time = s1.startup_time


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT dbin.db_name,dbin.instance_name,dbin.version,CASE WHEN s1.startup_time=s2.startup_time THEN 0 ELSE 1 END AS bounce,CAST(s1.end_interval_time AS date)AS begin_time,CAST(s2.end_interval_time AS date)AS end_time,round((CAST((CASE WHEN s2.end_interval_time>s1.end_interval_time THEN s2.end_interval_time ELSE s1.end_interval_time END)AS date)-CAST(s1.end_interval_time AS date))*86400)AS int_secs,CASE WHEN(s1.status<>0 OR s2.status<>0)THEN 1 ELSE 0 END AS err_detect,round(greatest((EXTRACT(day FROM s2.flush_elapsed)*86400)+(EXTRACT(hour FROM s2.flush_elapsed)*3600)+(EXTRACT(minute FROM s2.flush_elapsed)*60)+EXTRACT(second FROM s2.flush_elapsed),(EXTRACT(day FROM s1.flush_elapsed)*86400)+(EXTRACT(hour FROM s1.flush_elapsed)*3600)+(EXTRACT(minute FROM s1.flush_elapsed)*60)+EXTRACT(second FROM s1.flush_elapsed),0))AS max_flush_secs FROM wrm$_snapshot s1,wrm$_database_instance dbin,wrm$_snapshot s2 WHERE s1.dbid=:dbid AND s2.dbid=:dbid AND s1.instance_number=:inst_num AND s2.instance_number=:inst_num AND s1.snap_id=:bid AND s2.snap_id=:eid AND dbin.dbid=s1.dbid AND dbin.instance_number=s1.instance_number AND dbin.startup_time=s1.startup_time recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select dbin.db_name,dbin.instance_name,dbin.version,case when s1.startup_time=s2.startup_time then 0 else 1 end as bounce,cast(s1.end_interval_time as date)as begin_time,cast(s2.end_interval_time as date)as end_time,round((cast((case when s2.end_interval_time>s1.end_interval_time then s2.end_interval_time else s1.end_interval_time end)as date)-cast(s1.end_interval_time as date))*86400)as int_secs,case when(s1.status<>0 or s2.status<>0)then 1 else 0 end as err_detect,round(greatest((extract(day from s2.flush_elapsed)*86400)+(extract(hour from s2.flush_elapsed)*3600)+(extract(minute from s2.flush_elapsed)*60)+extract(second from s2.flush_elapsed),(extract(day from s1.flush_elapsed)*86400)+(extract(hour from s1.flush_elapsed)*3600)+(extract(minute from s1.flush_elapsed)*60)+extract(second from s1.flush_elapsed),0))as max_flush_secs from wrm$_snapshot s1,wrm$_database_instance dbin,wrm$_snapshot s2 where s1.dbid=:dbid and s2.dbid=:dbid and s1.instance_number=:inst_num and s2.instance_number=:inst_num and s1.snap_id=:bid and s2.snap_id=:eid and dbin.dbid=s1.dbid and dbin.instance_number=s1.instance_number and dbin.startup_time=s1.startup_time recorded first on 25 Oct 2021, 18:55:27