---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2022 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
BEGIN
      SELECT
      cols.column_name INTO :variable
      FROM
      example_table;
      END

--@FAILURE: Encountered unexpected token: "BEGIN" "BEGIN" recorded first on May 27, 2022, 10:29:48 PM