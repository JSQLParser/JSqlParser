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
timestamp '2009-10-29 01:30:00' at time zone 'us/pacific'from dual


--@FAILURE: select timestamp '2009-10-29 01:30:00' at time zone 'us/pacific' from dual recorded first on Jul 21, 2021 9:47:13 AM