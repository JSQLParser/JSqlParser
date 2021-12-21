---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select varchar2_ntt('a','b','c')
               multiset except
                  varchar2_ntt('b','c','d') as multiset_except
     from   dual

--@FAILURE: Encountered unexpected token: "except" "EXCEPT" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: net.sf.jsqlparser.parser.ParseException: Encountered unexpected token: "except" "EXCEPT" recorded first on 21 Dec 2021, 15:15:16