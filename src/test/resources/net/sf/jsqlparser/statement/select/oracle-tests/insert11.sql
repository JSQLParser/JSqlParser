---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
  insert into emp
  (empno, ename)
  values
  (seq_emp.nextval, 'morgan')
  returning empno
  into x

--@FAILURE: Encountered unexpected token: "into" "INTO" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: Encountered unexpected token: "x" <S_IDENTIFIER> recorded first on 24 Oct 2021, 16:56:39