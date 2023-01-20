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
--@FAILURE: select 25,+6.34,0.5,25e-03,-1,25f,+6.34 f,0.5 d,-1d,1.,.5,(sysdate-1d),sysdate-1m,sysdate-1dm,1.-+.5,1.+.5,1.+.5 d,1.+.5 dm,1. d,1. m,.5 m,.5 dm from dual recorded first on 11 Jan 2023, 21:07:10