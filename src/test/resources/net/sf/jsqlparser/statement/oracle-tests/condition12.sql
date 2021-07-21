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


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Jul 21, 2021 9:47:13 AM