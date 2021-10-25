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

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:07 AM
--@FAILURE: SELECT CAST(powermultiset(varchar2_ntt('a','b','c'))AS varchar2_ntts)AS pwrmltset FROM dual recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select cast(powermultiset(varchar2_ntt('a','b','c'))as varchar2_ntts)as pwrmltset from dual recorded first on 25 Oct 2021, 18:55:26