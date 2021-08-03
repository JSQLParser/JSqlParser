---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select * from dual t1,
(
	(
		(
			dual t2 join dual t3 using(dummy) )
			left outer join dual t4 using(dummy) )
			left outer join dual t5 using(dummy) )

			
			
