---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
merge /*+ dynamic_sampling(mm 4) dynamic_sampling_est_cdn(mm)
              dynamic_sampling(m 4) dynamic_sampling_est_cdn(m) */

into sys.mon_mods_all$ mm
using
(
	select decode(grouping_id(tp.bo#,tsp.pobj#,m.obj#),3,tp.bo#,1,tsp.pobj#,m.obj#) obj#, sum(m.inserts) inserts, sum(m.updates) updates, sum(m.deletes) deletes,
	decode(sum(bitand(m.flags,1)),0,0,1) +decode(sum(bitand(m.flags,2)),0,0,2) +decode(sum(bitand(m.flags,4)),0,0,4) flags, sum(m.drop_segments) drop_segments
	from sys.mon_mods$ m, sys.tabcompart$ tp, sys.tabsubpart$ tsp
	where m.obj# = tsp.obj# and tp.obj# = tsp.pobj#
	group by rollup(tp.bo#,tsp.pobj#,m.obj#) having grouping_id(tp.bo#,tsp.pobj#,m.obj#) < 7
        order by 1, 2, 3
) v on
(mm.obj# = v.obj#)
when matched then
update set mm.inserts = mm.inserts + v.inserts, mm.updates = mm.updates + v.updates, mm.deletes = mm.deletes + v.deletes,
mm.flags = mm.flags + v.flags - bitand(mm.flags,v.flags) , mm.drop_segments = mm.drop_segments + v.drop_segments
when not matched then insert values (v.obj#, v.inserts, v.updates, v.deletes, sysdate, v.flags, v.drop_segments)

--@FAILURE: merge into sys.mon_mods_all$ mm using(select decode(grouping_id(tp.bo#,tsp.pobj#,m.obj#),3,tp.bo#,1,tsp.pobj#,m.obj#)obj#,sum(m.inserts)inserts,sum(m.updates)updates,sum(m.deletes)deletes,decode(sum(bitand(m.flags,1)),0,0,1)+decode(sum(bitand(m.flags,2)),0,0,2)+decode(sum(bitand(m.flags,4)),0,0,4)flags,sum(m.drop_segments)drop_segments from sys.mon_mods$ m,sys.tabcompart$ tp,sys.tabsubpart$ tsp where m.obj#=tsp.obj# and tp.obj#=tsp.pobj# group by rollup(tp.bo#,tsp.pobj#,m.obj#)having grouping_id(tp.bo#,tsp.pobj#,m.obj#)<7 order by 1,2,3)v on(mm.obj#=v.obj#)when matched then update set mm.inserts=mm.inserts+v.inserts,mm.updates=mm.updates+v.updates,mm.deletes=mm.deletes+v.deletes,mm.flags=mm.flags+v.flags-bitand(mm.flags,v.flags),mm.drop_segments=mm.drop_segments+v.drop_segments when not matched then insert values(v.obj#,v.inserts,v.updates,v.deletes,sysdate,v.flags,v.drop_segments) recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: MERGE INTO sys.mon_mods_all$ mm USING(SELECT decode(grouping_id(tp.bo#,tsp.pobj#,m.obj#),3,tp.bo#,1,tsp.pobj#,m.obj#)obj#,sum(m.inserts)inserts,sum(m.updates)updates,sum(m.deletes)deletes,decode(sum(bitand(m.flags,1)),0,0,1)+decode(sum(bitand(m.flags,2)),0,0,2)+decode(sum(bitand(m.flags,4)),0,0,4)flags,sum(m.drop_segments)drop_segments FROM sys.mon_mods$ m,sys.tabcompart$ tp,sys.tabsubpart$ tsp WHERE m.obj#=tsp.obj# AND tp.obj#=tsp.pobj# GROUP BY rollup(tp.bo#,tsp.pobj#,m.obj#)HAVING grouping_id(tp.bo#,tsp.pobj#,m.obj#)<7 ORDER BY 1,2,3)v ON(mm.obj#=v.obj#)WHEN MATCHED THEN UPDATE SET mm.inserts=mm.inserts+v.inserts,mm.updates=mm.updates+v.updates,mm.deletes=mm.deletes+v.deletes,mm.flags=mm.flags+v.flags-bitand(mm.flags,v.flags),mm.drop_segments=mm.drop_segments+v.drop_segments WHEN NOT MATCHED THEN INSERT VALUES(v.obj#,v.inserts,v.updates,v.deletes,sysdate,v.flags,v.drop_segments) recorded first on 25 Oct 2021, 18:46:42