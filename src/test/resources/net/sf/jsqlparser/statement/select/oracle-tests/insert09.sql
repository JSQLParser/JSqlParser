---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
insert into (
select deptno, dname, loc
from dept
where deptno < 30 with check option)
values (99, 'travel', 'seattle')

--@FAILURE: Encountered unexpected token: "(" "(" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: net.sf.jsqlparser.parser.ParseException: Encountered unexpected token: "(" "(" recorded first on 21 Dec 2021, 15:15:16