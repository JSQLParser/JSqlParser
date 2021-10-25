---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select trim(both ' ' from '  a  ') from dual where trim(:a) is not null

--@SUCCESSFULLY_PARSED_AND_DEPARSED first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: SELECT trim(both ' ' from ' a ')FROM dual WHERE trim(:a)IS NOT NULL recorded first on 25 Oct 2021, 18:46:41
--@FAILURE: select trim(both ' ' from ' a ')from dual where trim(:a)is not null recorded first on 25 Oct 2021, 18:55:26