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
  rules
  ( m_1[ 1 ] = num_val[ 1 ] * 10 ,
    m_1[ 2 ] = to_number( to_char( sysdate, 'YYYYMMDD' ) ),
    m_1[ 3 ] = dbms_utility.get_time ,
    m_1[ 4 ] = :bind_var
  )
order by
  key

--@FAILURE: Encountered unexpected token: "dimension" <S_IDENTIFIER> recorded first on Aug 3, 2021, 7:20:08 AM