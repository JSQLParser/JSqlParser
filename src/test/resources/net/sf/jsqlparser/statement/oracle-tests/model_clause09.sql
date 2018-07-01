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
  ( m_1[ 1 ] = num_val[ 1     ] * 10 ,   -- literal
    m_1[ 2 ] = num_val[ 1 + 1 ] * 100 ,  -- expression
    m_1[ 3 ] = num_val[ key=3 ] * 1000   -- condition
  )
order by
  key
