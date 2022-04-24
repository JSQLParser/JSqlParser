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

--@FAILURE: Encountered unexpected token: "BEGIN" "BEGIN" recorded first on Aug 3, 2021, 7:20:07 AM
--@FAILURE: begin<<outer_loop>>for i in 1. .3 loop<<inner_loop>>for i in 1. .3 loop if outer_loop . i=2 then dbms_output . put_line('outer:'||to_char(outer_loop . i)||' inner:'||to_char(inner_loop . i)); end if ; end loop inner_loop ; end loop outer_loop ; end ; recorded first on 23 Apr 2022, 15:05:41
--@FAILURE: <<outer_loop>>for i in 1. .3 loop<<inner_loop>>for i in 1. .3 loop if outer_loop . i=2 then dbms_output . put_line('outer:'||to_char(outer_loop . i)||' inner:'||to_char(inner_loop . i)); end if ; end loop inner_loop ; end loop outer_loop ; end ; recorded first on 23 Apr 2022, 15:48:28
--@FAILURE: Encountered unexpected token: "END" "END" recorded first on 23 Apr 2022, 16:09:20
--@FAILURE: Encountered unexpected token: ";" ";" recorded first on 23 Apr 2022, 23:14:35