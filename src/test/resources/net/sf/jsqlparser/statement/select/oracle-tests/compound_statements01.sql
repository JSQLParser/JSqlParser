---
-- #%L
-- JSQLParser library
-- %%
-- Copyright (C) 2004 - 2022 JSQLParser
-- %%
-- Dual licensed under GNU LGPL 2.1 or Apache License 2.0
-- #L%
---
      DECLARE
        PK_NAME VARCHAR(200);

      BEGIN
        EXECUTE IMMEDIATE ('CREATE SEQUENCE "untitled_table3_seq"');

      SELECT
        cols.column_name INTO PK_NAME
      FROM
        all_constraints cons,
        all_cons_columns cols
      WHERE
        cons.constraint_type = 'P'
        AND cons.constraint_name = cols.constraint_name
        AND cons.owner = cols.owner
        AND cols.table_name = 'untitled_table3';

      execute immediate (
        'create or replace trigger "untitled_table3_autoinc_trg"  BEFORE INSERT on "untitled_table3"  for each row  declare  checking number := 1;  begin    if (:new."' || PK_NAME || '" is null) then      while checking >= 1 loop        select "untitled_table3_seq".nextval into :new."' || PK_NAME || '" from dual;        select count("' || PK_NAME || '") into checking from "untitled_table3"        where "' || PK_NAME || '" = :new."' || PK_NAME || '";      end loop;    end if;  end;'
      );
      END

--@FAILURE: Encountered unexpected token: "PK_NAME" <S_IDENTIFIER> recorded first on May 27, 2022, 10:27:41 PM