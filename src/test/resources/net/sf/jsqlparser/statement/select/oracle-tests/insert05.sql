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
--@FAILURE: Encountered unexpected token: "t" <S_IDENTIFIER> recorded first on 24 Oct 2021, 16:56:39
--@FAILURE: Encountered unexpected token: "insert" "INSERT" recorded first on Mar 26, 2023, 6:59:20 PM
--@FAILURE: Encountered: <K_INSERT> / "insert", at line 11, column 1, in lexical state DEFAULT. recorded first on 15 May 2025, 16:24:08