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
 1
 , cursor(select 1 from dual) c1
 , cursor(select 2, 3 from dual) as c2
 from
 table(select 1 from dual)


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT 1,cursor(SELECT 1 FROM dual)c1,cursor(SELECT 2,3 FROM dual)AS c2 FROM table(SELECT 1 FROM dual) recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select 1,cursor(select 1 from dual)c1,cursor(select 2,3 from dual)as c2 from table(select 1 from dual) recorded first on 25 Oct 2021, 18:55:26