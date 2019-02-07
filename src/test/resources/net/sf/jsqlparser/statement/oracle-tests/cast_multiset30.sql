---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select cast(
               collect(
                  distinct empsal_ot(ename, sal)
                  ) as empsal_ntt) as empsals
     from   emp
	    
