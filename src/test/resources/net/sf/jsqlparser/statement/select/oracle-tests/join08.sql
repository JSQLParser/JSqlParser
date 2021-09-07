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
join (select * from dual) tt2 using(dummy)
join (select * from dual) using(dummy)
join (select * from dual) d on(d.dummy=tt3.dummy)
inner join (select * from dual) tt2 using(dummy)
inner join (select * from dual) using(dummy)
inner join (select * from dual) d on(d.dummy=t1.dummy)


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: select*from dual t1 join select*from dual tt2 using(dummy)join select*from dual using(dummy)join select*from dual d on(d.dummy=tt3.dummy)inner join select*from dual tt2 using(dummy)inner join select*from dual using(dummy)inner join select*from dual d on(d.dummy=t1.dummy) recorded first on Sep 7, 2021 7:58:09 AM