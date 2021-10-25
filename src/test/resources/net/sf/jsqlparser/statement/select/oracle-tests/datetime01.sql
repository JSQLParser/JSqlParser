---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select from_tz(cast(to_date('1999-12-01 11:00:00','yyyy-mm-dd hh:mi:ss') as timestamp), 'america/new_york') at time zone 'america/los_angeles' "west coast time" from dual

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT from_tz(CAST(to_date('1999-12-01 11:00:00','yyyy-mm-dd hh:mi:ss')AS timestamp),'america/new_york')AT TIME ZONE 'america/los_angeles' "west coast time" FROM dual recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select from_tz(cast(to_date('1999-12-01 11:00:00','yyyy-mm-dd hh:mi:ss')as timestamp),'america/new_york')at time zone 'america/los_angeles' "west coast time" from dual recorded first on 25 Oct 2021, 18:55:26