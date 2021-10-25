---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select * from (
 select times_purchased as "purchase frequency", state_code
 from customers t
 )
 pivot xml
 (
 count(state_code)
 for state_code in (any)
 )
 order by 1

	    

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT*FROM(SELECT times_purchased AS "purchase frequency",state_code FROM customers t)PIVOT XML(count(state_code)FOR state_code IN(ANY))ORDER BY 1 recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select*from(select times_purchased as "purchase frequency",state_code from customers t)pivot xml(count(state_code)for state_code in(any))order by 1 recorded first on 25 Oct 2021, 18:55:26