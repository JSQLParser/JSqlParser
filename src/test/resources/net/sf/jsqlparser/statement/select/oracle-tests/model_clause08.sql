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
  num_val ,
  m_1
from
  t
model
  dimension by ( key )
  measures     ( num_val, 0 as m_1 )
  rules        ( m_1[ num_val[1]/100 ] = 10 )
order by
  key

--@FAILURE: Encountered unexpected token: "dimension" <S_IDENTIFIER> recorded first on Aug 3, 2021, 7:20:07 AM
--@FAILURE: select key,num_val,m_1 from t model recorded first on 23 Apr 2022, 16:44:21