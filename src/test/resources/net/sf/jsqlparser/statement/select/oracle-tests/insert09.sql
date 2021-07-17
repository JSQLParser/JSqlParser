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
