---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select * from sys.dual natural join sys.dual 


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT*FROM sys.dual NATURAL JOIN sys.dual recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select*from sys.dual natural join sys.dual recorded first on 25 Oct 2021, 18:55:26