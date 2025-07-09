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
--@FAILURE: Encountered: "(" / "(", at line 12, column 20, in lexical state DEFAULT. recorded first on 15 May 2025, 16:24:08
--@FAILURE: Encountered: <OPENING_BRACKET> / "(", at line 12, column 20, in lexical state DEFAULT. recorded first on 9 Jul 2025, 17:09:17