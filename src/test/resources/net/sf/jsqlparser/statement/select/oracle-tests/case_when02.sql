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
	STaLENESS, OSIZE, OBJ#, TYPE#,
	case
	when STaLENESS > .5 then 128
	when STaLENESS > .1 then 256
	else 0
	end + aFLaGS aFLaGS,
	STaTUS,
	SID,
	SERIaL#,
	PaRT#,
	BO#
	,
	case
	when is_FULL_EVENTS_HisTorY = 1 then SRC.Bor_LasT_STaTUS_TIME
	else 
		case GREaTEST (NVL (WP.Bor_LasT_STaT_TIME, date '1900-01-01'), NVL (SRC.Bor_LasT_STaTUS_TIME, date '1900-01-01'))
		when date '1900-01-01' then null
		when WP.Bor_LasT_STaT_TIME then WP.Bor_LasT_STaT_TIME
		when SRC.Bor_LasT_STaTUS_TIME then SRC.Bor_LasT_STaTUS_TIME
		else null
		end
	end
	,
	case GREaTEST (NVL (WP.Bor_LasT_STaT_TIME, date '1900-01-01'), NVL (SRC.Bor_LasT_STaTUS_TIME, date '1900-01-01'))
	when date '1900-01-01' then null
	when WP.Bor_LasT_STaT_TIME then WP.Bor_LasT_STaT_TIME
	when SRC.Bor_LasT_STaTUS_TIME then SRC.Bor_LasT_STaTUS_TIME
	else null
	end	
from X


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT STaLENESS,OSIZE,OBJ#,TYPE#,CASE WHEN STaLENESS>.5 THEN 128 WHEN STaLENESS>.1 THEN 256 ELSE 0 END+aFLaGS aFLaGS,STaTUS,SID,SERIaL#,PaRT#,BO#,CASE WHEN is_FULL_EVENTS_HisTorY=1 THEN SRC.Bor_LasT_STaTUS_TIME ELSE CASE GREaTEST(NVL(WP.Bor_LasT_STaT_TIME,DATE '1900-01-01'),NVL(SRC.Bor_LasT_STaTUS_TIME,DATE '1900-01-01'))WHEN DATE '1900-01-01' THEN NULL WHEN WP.Bor_LasT_STaT_TIME THEN WP.Bor_LasT_STaT_TIME WHEN SRC.Bor_LasT_STaTUS_TIME THEN SRC.Bor_LasT_STaTUS_TIME ELSE NULL END END,CASE GREaTEST(NVL(WP.Bor_LasT_STaT_TIME,DATE '1900-01-01'),NVL(SRC.Bor_LasT_STaTUS_TIME,DATE '1900-01-01'))WHEN DATE '1900-01-01' THEN NULL WHEN WP.Bor_LasT_STaT_TIME THEN WP.Bor_LasT_STaT_TIME WHEN SRC.Bor_LasT_STaTUS_TIME THEN SRC.Bor_LasT_STaTUS_TIME ELSE NULL END FROM X recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select staleness,osize,obj#,type#,case when staleness>.5 then 128 when staleness>.1 then 256 else 0 end+aflags aflags,status,sid,serial#,part#,bo#,case when is_full_events_history=1 then src.bor_last_status_time else case greatest(nvl(wp.bor_last_stat_time,date '1900-01-01'),nvl(src.bor_last_status_time,date '1900-01-01'))when date '1900-01-01' then null when wp.bor_last_stat_time then wp.bor_last_stat_time when src.bor_last_status_time then src.bor_last_status_time else null end end,case greatest(nvl(wp.bor_last_stat_time,date '1900-01-01'),nvl(src.bor_last_status_time,date '1900-01-01'))when date '1900-01-01' then null when wp.bor_last_stat_time then wp.bor_last_stat_time when src.bor_last_status_time then src.bor_last_status_time else null end from x recorded first on 25 Oct 2021, 18:55:26