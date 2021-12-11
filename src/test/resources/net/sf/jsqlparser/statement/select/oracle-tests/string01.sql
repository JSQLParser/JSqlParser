---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select
  'hello'
, 'oracle.dbs'
, 'jackie''s raincoat'
, '09-mar-98'
, ''
, ''''
, q'!name like '%dbms_%%'!'
, q'<'so,' she said, 'it's finished.'>'
, q'{select * from employees where last_name = 'smith'}'
, q'"name like '['"'
, n'nchar literal'
from dual


--@FAILURE: Encountered unexpected token: "%" "%" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: net.sf.jsqlparser.parser.ParseException: Encountered unexpected token: "%" "%" recorded first on 10 Dec 2021, 23:47:01