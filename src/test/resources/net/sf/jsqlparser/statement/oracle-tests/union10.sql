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
(                                                                                                                                                              
 (                                                                                                                                                             
  select 'y' from dual                                                                                                                                         
  where exists ( select 1 from dual where 1 = 0 )                                                                                                              
 )                                                                                                                                                             
 union                                                                                                                                                         
 (                                                                                                                                                             
  select 'n' from dual                                                                                                                                         
  where not exists ( select 1 from dual where 1 = 0 )                                                                                                          
 )                                                                                                                                                             
)                                                                                                                                                              
as yes_no                                                                                                                                                      
from dual

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Jul 21, 2021 9:47:13 AM