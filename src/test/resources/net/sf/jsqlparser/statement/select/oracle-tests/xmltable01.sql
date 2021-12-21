---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
select warehouse_name warehouse, warehouse2."water", warehouse2."rail"
from warehouses,
 xmltable('/warehouse'
  passing warehouses.warehouse_spec
  columns
   "water" varchar2(6) path '/warehouse/wateraccess',
   "rail" varchar2(6) path '/warehouse/railaccess')
warehouse2

--@FAILURE: Encountered unexpected token: "(" "(" recorded first on Aug 3, 2021, 7:20:08 AM
--@FAILURE: net.sf.jsqlparser.parser.ParseException: Encountered unexpected token: "(" "(" recorded first on 21 Dec 2021, 15:15:17