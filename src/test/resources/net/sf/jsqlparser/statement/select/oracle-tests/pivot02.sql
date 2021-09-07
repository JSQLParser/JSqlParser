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
 select times_purchased as "puchase frequency", state_code
 from customers t
 )
 pivot xml
 (
 count(state_code)
 for state_code in (select state_code from preferred_states)
 )
 order by 1

	    

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: select*from select times_purchased as "puchase frequency",state_code from customers t pivot xml(count(state_code)for state_code in(select state_code from preferred_states))order by 1 recorded first on Sep 7, 2021 7:58:10 AM