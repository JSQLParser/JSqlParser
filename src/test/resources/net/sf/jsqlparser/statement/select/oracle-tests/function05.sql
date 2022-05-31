---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select count(*)
  from employees
  where lnnvl(commission_pct >= .2)

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: select count(all*)from employees where lnnvl(commission_pct>=.2) recorded first on 31 May 2022, 17:49:12