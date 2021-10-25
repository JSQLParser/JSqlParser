---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select * from dual t1
left outer join (select * from dual) tt2 using(dummy)
left outer join (select * from dual) using(dummy)
left outer join (select * from dual) d on(d.dummy=tt3.dummy)
inner join (select * from dual) tt2 using(dummy)
inner join (select * from dual) using(dummy)
inner join (select * from dual) d on(d.dummy=t1.dummy)


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT*FROM dual t1 LEFT OUTER JOIN(SELECT*FROM dual)tt2 USING(dummy)LEFT OUTER JOIN(SELECT*FROM dual)USING(dummy)LEFT OUTER JOIN(SELECT*FROM dual)d ON(d.dummy=tt3.dummy)INNER JOIN(SELECT*FROM dual)tt2 USING(dummy)INNER JOIN(SELECT*FROM dual)USING(dummy)INNER JOIN(SELECT*FROM dual)d ON(d.dummy=t1.dummy) recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select*from dual t1 left outer join(select*from dual)tt2 using(dummy)left outer join(select*from dual)using(dummy)left outer join(select*from dual)d on(d.dummy=tt3.dummy)inner join(select*from dual)tt2 using(dummy)inner join(select*from dual)using(dummy)inner join(select*from dual)d on(d.dummy=t1.dummy) recorded first on 25 Oct 2021, 18:55:26