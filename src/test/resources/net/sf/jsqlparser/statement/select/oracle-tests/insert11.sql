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
