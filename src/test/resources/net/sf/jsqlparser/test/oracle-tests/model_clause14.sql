-- http://www.adp-gmbh.ch/ora/sql/model_clause/ex_generate_dates.html
select dt from (select trunc(sysdate) dt from dual)
model 
  dimension by (0 d)
  measures     (dt)
  rules iterate(9) (
    dt[ iteration_number+1 ] = dt[ iteration_number ]+1
  )
