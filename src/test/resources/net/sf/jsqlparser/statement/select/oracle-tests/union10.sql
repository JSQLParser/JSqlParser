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

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: Encountered unexpected token: "union" "UNION" recorded first on Feb 13, 2025, 10:16:06 AM