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
  key ,
  dummy
from
  t
where
  key = 1
model
  dimension by ( key )
  measures     ( ( select dummy from dual ) as dummy )
  rules        ( )

--@FAILURE: Encountered unexpected token: "model" <S_IDENTIFIER> recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: select key,dummy from t where key=1 recorded first on 23 Apr 2022, 16:44:21