---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select * from v.e
where
	cid <> rid
	and  rid  not in
	(
		(select distinct  rid  from  v.s )
		union
		(select distinct  rid  from v.p )
	)
	and  "timestamp"  <= 1298505600000


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: Encountered unexpected token: "not" "NOT" recorded first on Feb 13, 2025, 10:16:06 AM