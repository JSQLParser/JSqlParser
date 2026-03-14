---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select *
     from   table( varchar2_ntt('a','b','c')
                      multiset union distinct
                         varchar2_ntt('b','c','d') )

--@FAILURE: Encountered unexpected token: "(" "(" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: Encountered: "(" / "(", at line 11, column 18, in lexical state DEFAULT. recorded first on 15 May 2025, 16:24:08
--@FAILURE: Encountered: <OPENING_BRACKET> / "(", at line 11, column 18, in lexical state DEFAULT. recorded first on 9 Jul 2025, 17:09:17
--@FAILURE: Encountered: <K_UNION> / "union", at line 12, column 32, in lexical state DEFAULT. recorded first on 14 Mar 2026, 22:33:07