---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select tbl$or$idx$part$num("sys"."wrh$_seg_stat",0,4,0,"rowid") as c1 from t1

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT tbl$or$idx$part$num("sys"."wrh$_seg_stat",0,4,0,"rowid")AS c1 FROM t1 recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select tbl$or$idx$part$num("sys"."wrh$_seg_stat",0,4,0,"rowid")as c1 from t1 recorded first on 25 Oct 2021, 18:55:26