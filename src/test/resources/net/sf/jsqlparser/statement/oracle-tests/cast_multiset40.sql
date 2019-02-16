---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select cast(
               powermultiset(
                  varchar2_ntt('a','b','c')) as varchar2_ntts) as pwrmltset
     from   dual
