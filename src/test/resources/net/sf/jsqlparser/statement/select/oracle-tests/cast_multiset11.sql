---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
update table_name
set row = array_of_records(i)
where 1=1
      

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: UPDATE table_name SET row=array_of_records(i)WHERE 1=1 recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: update table_name set row=array_of_records(i)where 1=1 recorded first on 25 Oct 2021, 18:55:26