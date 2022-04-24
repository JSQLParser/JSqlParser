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
--@FAILURE: select value(p$)from "xdb"."xdb$schema" as of recorded first on 23 Apr 2022, 16:44:21