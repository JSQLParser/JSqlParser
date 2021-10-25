---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select t.*, connect_by_root id
from test t
start with t.id = 1
connect by prior t.id = t.parent_id
order siblings by t.some_text

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT t.*,CONNECT_BY_ROOT id FROM test t START WITH t.id=1 CONNECT BY PRIOR t.id=t.parent_id ORDER SIBLINGS BY t.some_text recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select t.*,connect_by_root id from test t start with t.id=1 connect by prior t.id=t.parent_id order siblings by t.some_text recorded first on 25 Oct 2021, 18:55:26