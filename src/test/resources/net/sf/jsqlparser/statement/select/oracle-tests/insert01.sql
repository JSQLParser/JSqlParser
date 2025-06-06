---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
insert
  when mod( object_id, 2 ) = 1 then
    into t1 ( x, y ) values ( s.nextval, object_id )
  when mod( object_id, 2 ) = 0 then
    into t2 ( x, y ) values ( s.nextval, created )
select object_id, created from all_objects

--@FAILURE: Encountered unexpected token: "when" "WHEN" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: Encountered unexpected token: "insert" "INSERT" recorded first on 11 Jan 2023, 21:07:10
--@FAILURE: Encountered: <K_INSERT> / "insert", at line 10, column 1, in lexical state DEFAULT. recorded first on 15 May 2025, 16:24:08