---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select * from dual order by m.year, m.title, f(a)

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:07 AM
--@FAILURE: SELECT*FROM dual ORDER BY m.year,m.title,f(a) recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select*from dual order by m.year,m.title,f(a) recorded first on 25 Oct 2021, 18:55:27