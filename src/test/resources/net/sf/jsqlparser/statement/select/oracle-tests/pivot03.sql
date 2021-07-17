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

	    
