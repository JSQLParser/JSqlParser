---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select d1.*, d2.* from dual cross join dual 

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT d1.*,d2.*FROM dual CROSS JOIN dual recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select d1.*,d2.*from dual cross join dual recorded first on 25 Oct 2021, 18:55:26