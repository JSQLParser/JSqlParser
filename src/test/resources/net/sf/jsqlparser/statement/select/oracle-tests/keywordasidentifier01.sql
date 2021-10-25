---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select timestamp, avg, cume_dist from nulls

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT timestamp,avg,cume_dist FROM nulls recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select timestamp,avg,cume_dist from nulls recorded first on 25 Oct 2021, 18:55:27