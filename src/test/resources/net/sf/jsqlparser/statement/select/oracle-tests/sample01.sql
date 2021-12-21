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
--@FAILURE: net.sf.jsqlparser.parser.ParseException: Encountered unexpected token: "block" <S_IDENTIFIER> recorded first on 21 Dec 2021, 15:15:17