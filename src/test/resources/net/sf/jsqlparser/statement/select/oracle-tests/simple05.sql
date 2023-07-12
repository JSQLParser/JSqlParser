---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select * from
(
select * from a 
	unpivot
	(
		value for value_type in (dummy)
	)
)


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: select*from(select*from aunpivot(value for value_type in(dummy))) recorded first on Jul 12, 2023, 12:58:42 PM