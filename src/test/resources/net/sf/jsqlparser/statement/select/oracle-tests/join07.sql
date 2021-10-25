---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select * from t1 
join t2 tt2 using(c)
join t3 tt3 using(d)
join t3 using(d)


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT*FROM t1 JOIN t2 tt2 USING(c)JOIN t3 tt3 USING(d)JOIN t3 USING(d) recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select*from t1 join t2 tt2 using(c)join t3 tt3 using(d)join t3 using(d) recorded first on 25 Oct 2021, 18:55:26