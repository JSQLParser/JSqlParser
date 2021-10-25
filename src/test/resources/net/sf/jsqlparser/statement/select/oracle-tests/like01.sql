---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select last_name
from employees
where last_name
like '%a\_b%' escape '\'
order by last_name

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT last_name FROM employees WHERE last_name LIKE '%a\_b%' ESCAPE '\' ORDER BY last_name recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select last_name from employees where last_name like '%a\_b%' escape '\' order by last_name recorded first on 25 Oct 2021, 18:55:26