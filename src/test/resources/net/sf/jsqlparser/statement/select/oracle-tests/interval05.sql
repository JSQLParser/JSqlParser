---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
-- see metalink note 1056382.1
select 'yes' from dual where (sysdate-5,sysdate) overlaps (sysdate-2,sysdate-1)

--@FAILURE: Encountered unexpected token: "overlaps" <S_IDENTIFIER> recorded first on Aug 3, 2021, 7:20:08 AM
--@SUCCESSFULLY_PARSED_AND_DEPARSED first on 31.08.2022 20:18:36
--@FAILURE: Encountered unexpected token: "," "," recorded first on 26 Nov 2022, 17:20:59