---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select trim(both from con.ke)
from dual



--@FAILURE: Encountered unexpected token: "(" "(" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: Exception without message: net.sf.jsqlparser.JSQLParserException recorded first on 24 Oct 2021, 02:38:30
--@FAILURE: Encountered unexpected token: "trim" <K_STRING_FUNCTION_NAME> recorded first on 24 Oct 2021, 02:45:19