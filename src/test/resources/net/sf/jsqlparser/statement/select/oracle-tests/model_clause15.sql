---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
-- http://asktom.oracle.com/pls/asktom/f?p=100:11:0::::P11_QUESTION_ID:8912311513313
select
  name, to_char(dt,'DD-MM-YYYY') dt, amt, cum_amt
  from (
     select name, trunc(dt,'MM') dt, sum(amt) amt
     from c
     group by name, trunc(dt,'MM')
  )
  model
  partition by (name)
  dimension by (dt)
  measures(amt, cast(null as number) cum_amt)
  ignore nav
  rules sequential order(
      amt[for dt from to_date('01-01-2002','DD-MM-YYYY')
                   to to_date('01-12-2002','DD-MM-YYYY')
                 increment numtoyminterval(1,'MONTH')    ] = amt[cv(dt)],
      cum_amt[any] = sum(amt)[dt <= cv(dt)]
   )
   order by name, dt


--@FAILURE: Encountered unexpected token: "partition" "PARTITION" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: net.sf.jsqlparser.parser.ParseException: Encountered unexpected token: "partition" "PARTITION" recorded first on 21 Dec 2021, 15:15:16