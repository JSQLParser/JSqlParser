---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
-- http://www.adp-gmbh.ch/ora/sql/model_clause/ex_generate_dates.html
select dt from (select trunc(sysdate) dt from dual)
model 
  dimension by (0 d)
  measures     (dt)
  rules iterate(9) (
    dt[ iteration_number+1 ] = dt[ iteration_number ]+1
  )

--@FAILURE: Encountered unexpected token: "dimension" <S_IDENTIFIER> recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: Encountered: <S_IDENTIFIER> / "dimension", at line 13, column 3, in lexical state DEFAULT. recorded first on 15 May 2025, 16:24:08