---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select * from table (cast (f_int_date_varchar2() as table_int_date_varchar2))

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Jul 21, 2021 9:47:13 AM