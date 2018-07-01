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
