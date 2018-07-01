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