---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select * from 
(
select 1 as c1 from "sys"."obj$" sample block (14.285714 , 1) seed (1) "o"
) samplesub

--@FAILURE: Encountered unexpected token: "block" <S_IDENTIFIER> recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: Encountered unexpected token: "block" "BLOCK" recorded first on Jul 12, 2023, 12:58:42 PM
--@FAILURE: Encountered unexpected token: "," "," recorded first on Jul 12, 2023, 1:30:58 PM
--@FAILURE: Encountered: <K_COMMA> / ",", at line 12, column 58, in lexical state DEFAULT. recorded first on 15 May 2025, 16:24:09