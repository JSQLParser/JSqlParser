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
--@FAILURE: Encountered: "(" / "(", at line 12, column 10, in lexical state DEFAULT. recorded first on 15 May 2025, 16:24:09
--@FAILURE: Encountered: <OPENING_BRACKET> / "(", at line 12, column 10, in lexical state DEFAULT. recorded first on 9 Jul 2025, 17:09:18