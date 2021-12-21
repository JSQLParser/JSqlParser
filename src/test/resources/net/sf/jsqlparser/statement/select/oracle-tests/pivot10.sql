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
 from s join d using(c)
 pivot
 (
 max(c_c_p) as max_ccp
 , max(d_c_p) max_dcp
 , max(d_x_p) dxp
 , count(1) cnt
 for (i, p) in
 (
 (1,1) as one_one,
 (1,2) as one_two,
 (1,3) as one_three,
 (2,1) as two_one,
 (2,2) as two_two,
 (2,3) as two_three
 )
 )
 where d_t = 'p'

--@FAILURE: Encountered unexpected token: "pivot" "PIVOT" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: net.sf.jsqlparser.parser.ParseException: Encountered unexpected token: "pivot" "PIVOT" recorded first on 21 Dec 2021, 15:15:17