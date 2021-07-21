---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select department_id, last_name, salary 
 from employees x 
 where
 1 = 1
 and
 (
 	(
	HI
	)
	>
	(
	.1 * T.ROWCNT
	)
 )


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Jul 21, 2021 9:47:12 AM