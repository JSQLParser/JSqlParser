---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select nt.column_value as distinct_element
     from   table(set(varchar2_ntt('a','b','c','c'))) nt

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT nt.column_value AS distinct_element FROM table(set(varchar2_ntt('a','b','c','c')))nt recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select nt.column_value as distinct_element from table(set(varchar2_ntt('a','b','c','c')))nt recorded first on 25 Oct 2021, 18:55:27