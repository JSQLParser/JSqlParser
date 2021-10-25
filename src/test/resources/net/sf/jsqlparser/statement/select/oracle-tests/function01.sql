---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select decode(decode(decode( (select count(1) from dual), a, 1, 0), 0, 1), 1, 0) from dual

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT decode(decode(decode((SELECT count(1)FROM dual),a,1,0),0,1),1,0)FROM dual recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select decode(decode(decode((select count(1)from dual),a,1,0),0,1),1,0)from dual recorded first on 25 Oct 2021, 18:55:26