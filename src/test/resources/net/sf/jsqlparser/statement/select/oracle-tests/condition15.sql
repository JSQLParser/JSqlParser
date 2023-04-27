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
	"a3"."r_id" "r_id"
from
	"pe" "a3",
	"me" "a2"
where
	 "a3"."m_id"="a2"."m_id"
	 and "a2"."mi_t" =
	 any
	 (((
		select "a4"."sys$"."id"
		from t "a4"
	)))

--@FAILURE: select "a3"."r_id" "r_id" from "pe" "a3","me" "a2" where "a3"."m_id"="a2"."m_id" and "a2"."mi_t"=any((select "a4"."sys$"."id" from t "a4")) recorded first on Aug 3, 2021, 7:20:08 AM
--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Mar 25, 2023, 9:18:30 AM