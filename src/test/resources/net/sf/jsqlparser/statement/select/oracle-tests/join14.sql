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

			
			

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT*FROM dual t1,(((dual t2 JOIN dual t3 USING(dummy))LEFT OUTER JOIN dual t4 USING(dummy))LEFT OUTER JOIN dual t5 USING(dummy)) recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select*from dual t1,(((dual t2 join dual t3 using(dummy))left outer join dual t4 using(dummy))left outer join dual t5 using(dummy)) recorded first on 25 Oct 2021, 18:55:26