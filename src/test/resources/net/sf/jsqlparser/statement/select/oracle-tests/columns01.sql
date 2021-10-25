---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select a, b,
a d,
ddd as ddd,
ddd as "dfdf",
x as x
from dual


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT a,b,a d,ddd AS ddd,ddd AS "dfdf",x AS x FROM dual recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select a,b,a d,ddd as ddd,ddd as "dfdf",x as x from dual recorded first on 25 Oct 2021, 18:55:26