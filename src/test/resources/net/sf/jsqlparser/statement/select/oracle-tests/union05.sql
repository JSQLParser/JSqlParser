---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
(select * from dual)
union all
(
	select * from dual
)
union all
(
	select * from dual
)
union all
(
	select * from dual
)
union all
(
	select * from dual
)
union all
(
	select * from dual
)
union all
(
	select * from dual
)
union all
(
	select * from dual
)
order by 1 asc, 2 asc

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: Encountered unexpected token: "union" "UNION" recorded first on Feb 13, 2025, 10:16:06 AM