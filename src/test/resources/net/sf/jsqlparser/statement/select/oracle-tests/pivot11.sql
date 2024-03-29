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
 from s
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
 join d using(c)
 where d_t = 'p'

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: select*from spivot(max(c_c_p)as max_ccp,max(d_c_p)max_dcp,max(d_x_p)dxp,count(1)cnt for(i,p)in((1,1)as one_one,(1,2)as one_two,(1,3)as one_three,(2,1)as two_one,(2,2)as two_two,(2,3)as two_three))join d using(c)where d_t='p' recorded first on Jul 12, 2023, 12:58:42 PM