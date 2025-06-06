---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select
  group_2 ,
  num_val ,
  m_1
from
  t
model unique single reference
  dimension by ( group_2 )
  measures     ( num_val, 0 as m_1 )
  rules        ( m_1[any] = 10 )
order by
  group_2

--@FAILURE: Encountered unexpected token: "unique" "UNIQUE" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: Encountered: <K_UNIQUE> / "unique", at line 16, column 7, in lexical state DEFAULT. recorded first on 15 May 2025, 16:24:08