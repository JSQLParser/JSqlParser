---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select  count(*), max(scn)
from
(
	(select sp.bo#, sp.pmoptype, sp.scn, sp.flags
	from sumpartlog$ sp, sumdep$ sd
	where sd.sumobj# = :1 and sd.p_obj# = sp.bo#
	group by sp.bo#, sp.pmoptype, sp.scn, sp.flags)
	minus
	(select sp.bo#, sp.pmoptype, sp.scn, sp.flags
	from sumpartlog$ sp
	where sp.bo# not in
	(
		select sk.detailobj# from sumkey$ sk where sk.sumobj# = :1 and sk.detailcolfunction in (2,3)
	)
	and bitand(sp.flags, 2) != 2 and sp.pmoptype in (2,3,5,7))
	group by sp.bo#, sp.pmoptype, sp.scn, sp.flags
)
where scn > :2

--@FAILURE: Encountered unexpected token: "group" "GROUP" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: Encountered unexpected token: "minus" "MINUS" recorded first on Mar 25, 2023, 9:30:55 AM