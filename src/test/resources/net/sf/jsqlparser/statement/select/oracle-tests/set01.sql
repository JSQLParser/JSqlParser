---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select nt
     ,      set(nt) as nt_set
     from (
           select varchar2_ntt('a','b','c','c') as nt
           from dual
          )

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:07 AM
--@FAILURE: SELECT nt,set(nt)AS nt_set FROM(SELECT varchar2_ntt('a','b','c','c')AS nt FROM dual) recorded first on 25 Oct 2021, 18:46:42
--@FAILURE: select nt,set(nt)as nt_set from(select varchar2_ntt('a','b','c','c')as nt from dual) recorded first on 25 Oct 2021, 18:55:27