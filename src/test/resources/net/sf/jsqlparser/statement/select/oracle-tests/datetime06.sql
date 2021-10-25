---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select * from dual where sysdate > date '2013-04-10'

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT*FROM dual WHERE sysdate>DATE '2013-04-10' recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select*from dual where sysdate>date '2013-04-10' recorded first on 25 Oct 2021, 18:55:26