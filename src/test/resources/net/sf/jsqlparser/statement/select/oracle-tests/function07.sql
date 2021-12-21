---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select cust_gender, count(*) as cnt, round(avg(age)) as avg_age
   from mining_data_apply_v
   where prediction(dt_sh_clas_sample cost model
      using cust_marital_status, education, household_size) = 1
   group by cust_gender
   order by cust_gender
	    

--@FAILURE: Encountered unexpected token: "(" "(" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: net.sf.jsqlparser.parser.ParseException: Encountered unexpected token: "(" "(" recorded first on 21 Dec 2021, 15:15:16