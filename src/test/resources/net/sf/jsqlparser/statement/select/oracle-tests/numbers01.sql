---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select 25
, +6.34
, 0.5
, 25e-03
, -1 -- Here are some valid floating-point number literals:
, 25f
, +6.34F
, 0.5d
, -1D
, 1.
, .5
, (sysdate -1d)   -- here we substract "one" in decimal format
, sysdate -1m   -- here we substract "one" and "m" is column's alias
, sysdate -1dm
, 1.-+.5
, 1.+.5
, 1.+.5D
, 1.+.5DM
, 1.D
, 1.M
, .5M
, .5DM
from dual


--@FAILURE: Encountered unexpected token: "d" <S_IDENTIFIER> recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: net.sf.jsqlparser.parser.ParseException: Encountered unexpected token: "d" <S_IDENTIFIER> recorded first on 21 Dec 2021, 15:15:17