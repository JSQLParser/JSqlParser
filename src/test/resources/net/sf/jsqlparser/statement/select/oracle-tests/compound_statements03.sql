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
--@FAILURE: Encountered unexpected token: ":" ":" recorded first on 9 Dec 2022, 14:03:29
--@FAILURE: Encountered unexpected token: "INTO" "INTO" recorded first on 4 May 2023, 18:47:18
--@FAILURE: Encountered: <K_INTO> / "INTO", at line 12, column 24, in lexical state DEFAULT. recorded first on 15 May 2025, 16:24:08