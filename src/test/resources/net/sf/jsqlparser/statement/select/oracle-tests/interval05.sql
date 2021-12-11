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
--@FAILURE: net.sf.jsqlparser.parser.ParseException: Encountered unexpected token: "overlaps" <S_IDENTIFIER> recorded first on 10 Dec 2021, 23:47:00