---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select varchar2_ntt('a','b','c')
               multiset except
                  varchar2_ntt('b','c','d') as multiset_except
     from   dual

--@FAILURE: Encountered unexpected token: "except" "EXCEPT" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: Encountered unexpected token: "varchar2_ntt" <S_IDENTIFIER> recorded first on Mar 25, 2023, 9:18:30 AM
--@FAILURE: Encountered: <K_EXCEPT> / "except", at line 11, column 25, in lexical state DEFAULT. recorded first on 15 May 2025, 16:24:08