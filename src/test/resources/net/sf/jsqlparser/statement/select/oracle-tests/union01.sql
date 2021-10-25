---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
(select 'a' obj, 'b' link from dual) union all
(select 'a', 'c' from dual) union all
(select      'c', 'd' from dual) union all
(select           'd', 'c' from dual) union all
(select           'd', 'e' from dual) union all
(select                'e', 'e' from dual)


--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: (SELECT 'a' obj,'b' link FROM dual)UNION ALL(SELECT 'a','c' FROM dual)UNION ALL(SELECT 'c','d' FROM dual)UNION ALL(SELECT 'd','c' FROM dual)UNION ALL(SELECT 'd','e' FROM dual)UNION ALL(SELECT 'e','e' FROM dual) recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: (select 'a' obj,'b' link from dual)union all(select 'a','c' from dual)union all(select 'c','d' from dual)union all(select 'd','c' from dual)union all(select 'd','e' from dual)union all(select 'e','e' from dual) recorded first on 25 Oct 2021, 18:55:27