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
--@FAILURE: Encountered: <S_IDENTIFIER> / "snapshot", at line 10, column 64, in lexical state DEFAULT. recorded first on 15 May 2025, 16:24:08