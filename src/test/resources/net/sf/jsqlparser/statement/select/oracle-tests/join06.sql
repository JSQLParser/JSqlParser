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
--@FAILURE: java.lang.Error: Missing return statement in function recorded first on 26 Nov 2022, 17:20:59