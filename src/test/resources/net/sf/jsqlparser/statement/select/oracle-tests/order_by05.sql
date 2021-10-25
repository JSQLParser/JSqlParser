---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select * from dual order siblings by a nulls first, b nulls last, c nulls last, d nulls last, e nulls last

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT*FROM dual ORDER SIBLINGS BY a NULLS FIRST,b NULLS LAST,c NULLS LAST,d NULLS LAST,e NULLS LAST recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select*from dual order siblings by a nulls first,b nulls last,c nulls last,d nulls last,e nulls last recorded first on 25 Oct 2021, 18:55:26