---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select
timestamp '2009-10-29 01:30:00'
from dual


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT TIMESTAMP '2009-10-29 01:30:00' FROM dual recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select timestamp '2009-10-29 01:30:00' from dual recorded first on 25 Oct 2021, 18:55:26