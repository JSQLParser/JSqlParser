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
