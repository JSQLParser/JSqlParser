---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select *
     from   table(
               powermultiset_by_cardinality(
                  varchar2_ntt('a','b','c','d','d'), 3))

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT*FROM table(powermultiset_by_cardinality(varchar2_ntt('a','b','c','d','d'),3)) recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select*from table(powermultiset_by_cardinality(varchar2_ntt('a','b','c','d','d'),3)) recorded first on 25 Oct 2021, 18:55:26