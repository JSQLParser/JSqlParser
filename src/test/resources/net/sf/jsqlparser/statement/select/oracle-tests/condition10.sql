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


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT department_id,last_name,salary FROM employees x WHERE 1=1 AND((HI)>(.1*T.ROWCNT)) recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select department_id,last_name,salary from employees x where 1=1 and((hi)>(.1*t.rowcnt)) recorded first on 25 Oct 2021, 18:55:26