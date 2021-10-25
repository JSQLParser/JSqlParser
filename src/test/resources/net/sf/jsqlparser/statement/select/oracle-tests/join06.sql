---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select * from dual d1
join dual d2 on (d1.dummy = d2.dummy)
join dual d3 on(d1.dummy = d3.dummy)
join dual on(d1.dummy = dual.dummy)


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT*FROM dual d1 JOIN dual d2 ON(d1.dummy=d2.dummy)JOIN dual d3 ON(d1.dummy=d3.dummy)JOIN dual ON(d1.dummy=dual.dummy) recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select*from dual d1 join dual d2 on(d1.dummy=d2.dummy)join dual d3 on(d1.dummy=d3.dummy)join dual on(d1.dummy=dual.dummy) recorded first on 25 Oct 2021, 18:55:26