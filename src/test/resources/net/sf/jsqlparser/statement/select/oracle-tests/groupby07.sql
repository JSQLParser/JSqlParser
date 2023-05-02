---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select decode((tt || tc), '56', count(distinct cn), '57',  sum(nu)) as q
from t
where tt='500'
  and tc in ('6','7')
  and to_char(c,'mm') = '03'
  having sum(nu) > 0
group by tn, ui, (tt || tc)
order by 1

--@FAILURE: Encountered unexpected token: "group" "GROUP" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: select decode((tt||tc),'56',count(distinct cn),'57',sum(nu))as q from t where tt='500' and tc in('6','7')and to_char(c,'mm')='03' group by tn,ui,(tt||tc)having sum(nu)>0 order by 1 recorded first on 29 Apr 2023, 20:32:34
--@FAILURE: Encountered unexpected token: "(" "(" recorded first on 1 May 2023, 23:35:02