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
--@FAILURE: SELECT((SELECT 'y' FROM dual WHERE EXISTS(SELECT 1 FROM dual WHERE 1=0))UNION(SELECT 'n' FROM dual WHERE NOT EXISTS(SELECT 1 FROM dual WHERE 1=0)))AS yes_no FROM dual recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select((select 'y' from dual where exists(select 1 from dual where 1=0))union(select 'n' from dual where not exists(select 1 from dual where 1=0)))as yes_no from dual recorded first on 25 Oct 2021, 18:55:27