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
 from (s join d using(c))
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

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT*FROM(s JOIN d USING(c))PIVOT(max(c_c_p)AS max_ccp,max(d_c_p)max_dcp,max(d_x_p)dxp,count(1)cnt FOR(i,p)IN((1,1)AS one_one,(1,2)AS one_two,(1,3)AS one_three,(2,1)AS two_one,(2,2)AS two_two,(2,3)AS two_three))WHERE d_t='p' recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select*from(s join d using(c))pivot(max(c_c_p)as max_ccp,max(d_c_p)max_dcp,max(d_x_p)dxp,count(1)cnt for(i,p)in((1,1)as one_one,(1,2)as one_two,(1,3)as one_three,(2,1)as two_one,(2,2)as two_two,(2,3)as two_three))where d_t='p' recorded first on 25 Oct 2021, 18:55:26