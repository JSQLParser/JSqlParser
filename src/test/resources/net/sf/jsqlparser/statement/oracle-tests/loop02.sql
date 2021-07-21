---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2019 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
BEGIN
  <<outer_loop>>
  FOR i IN 1..3 LOOP
      <<inner_loop>>
      FOR i IN 1..3 LOOP
        IF outer_loop.i = 2 THEN
          DBMS_OUTPUT.PUT_LINE
            ( 'outer: ' || TO_CHAR(outer_loop.i) || ' inner: '
              || TO_CHAR(inner_loop.i));
        END IF;
      END LOOP inner_loop;
  END LOOP outer_loop;
END;

--@FAILURE: Encountered unexpected token: "BEGIN" "BEGIN" recorded first on Jul 21, 2021 9:47:13 AM