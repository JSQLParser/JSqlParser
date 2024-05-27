---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select a, b, d, c, dt, p, pp, tech_pp, tech_p 
from
(
select a, b, d, c, dt, p, pp, odr from (
select spf.*, nvl(a, ddr_a) as a, b, d,
	rank() over 
	(
		partition by nvl(a, d_a), c, b, dt
		order by inp, spf.rowid asc
	) as odr
	from t_pp spf
	join t_a   sa  on (spf.a_id = sa.a_id)
	where spf.dt between to_date('2001-01-01', 'RRRR-MM.DD') and to_date('2001-01-31', 'RRRR-MM-DD')
)
	where odr = 1
	and c = '1234'
)
  model
  partition by (a, b, d, c)
  dimension by (dt)
  measures(pp, cast(null as number) tech_pp, p, cast(null as number) tech_p)
  --ignore nav
  rules sequential order(
     tech_pp[ for dt from to_date('2001-01-01','RRRR-MM-DD') to to_date('2001-01-31','RRRR-MM-DD')
                increment numtodsinterval(1,'DAY')] = nvl(pp[cv(dt)], tech_pp[cv(dt)-1])
	,
     tech_p[ for dt from to_date('2001-01-01','RRRR-MM-DD') to to_date('2001-01-31','RRRR-MM-DD')
                increment numtodsinterval(1,'DAY')] = nvl(p[cv(dt)], tech_p[cv(dt)-1])
  )


--@FAILURE: Encountered unexpected token: "partition" "PARTITION" recorded first on Aug 3, 2021, 7:20:07 AM
--@FAILURE: Encountered unexpected token: "(" "(" recorded first on May 27, 2024, 9:38:28 AM