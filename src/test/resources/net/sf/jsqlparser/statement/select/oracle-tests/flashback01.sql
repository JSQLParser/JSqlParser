---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select value(p$) from "XDB"."XDB$SCHEMA"                 as of snapshot(:2) p$ where SYS_NC_OID$ = :1

--@FAILURE: Encountered unexpected token: "snapshot" <S_IDENTIFIER> recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: net.sf.jsqlparser.parser.ParseException: Encountered unexpected token: "snapshot" <S_IDENTIFIER> recorded first on 21 Dec 2021, 15:15:16