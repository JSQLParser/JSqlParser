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
--@FAILURE: SELECT*FROM v.e WHERE cid<>rid AND rid NOT IN((SELECT DISTINCT rid FROM v.s)UNION(SELECT DISTINCT rid FROM v.p))AND "timestamp"<=1298505600000 recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select*from v.e where cid<>rid and rid not in((select distinct rid from v.s)union(select distinct rid from v.p))and "timestamp"<=1298505600000 recorded first on 25 Oct 2021, 18:55:26