---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
with
	x1 as ( select * from t1 ),
	x2 as ( select * from t2 join t3 on (t2.a2 = t3.a3))
select
	*
from
	x1
join	x2 on (x1.a1 = x2.a2)
join	t4 on (x1.a1 = t4.a4)


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: WITH x1 AS(SELECT*FROM t1),x2 AS(SELECT*FROM t2 JOIN t3 ON(t2.a2=t3.a3))SELECT*FROM x1 JOIN x2 ON(x1.a1=x2.a2)JOIN t4 ON(x1.a1=t4.a4) recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: with x1 as(select*from t1),x2 as(select*from t2 join t3 on(t2.a2=t3.a3))select*from x1 join x2 on(x1.a1=x2.a2)join t4 on(x1.a1=t4.a4) recorded first on 25 Oct 2021, 18:55:27