---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
-- insert all
insert all
into t (pid, fname, lname)
values (1, 'dan', 'morgan')
into t (pid, fname, lname)
values (2, 'jeremiah', 'wilton')
into t (pid, fname, lname)
values (3, 'helen', 'lofstrom')
select * from dual

--@FAILURE: Encountered unexpected token: "into" "INTO" recorded first on Aug 3, 2021, 7:20:08 AM