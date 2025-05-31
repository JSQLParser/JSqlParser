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
          n_emp_id EMPLOYEES.EMPLOYEE_ID%TYPE := &emp_id1;
        BEGIN
          DECLARE
            n_emp_id employees.employee_id%TYPE := &emp_id2;
            v_name   employees.first_name%TYPE;
          BEGIN
            SELECT first_name, CASE foo WHEN 'a' THEN 1 ELSE 2 END CASE as other
            INTO v_name
            FROM employees
            WHERE employee_id = n_emp_id;

            DBMS_OUTPUT.PUT_LINE('First name of employee ' || n_emp_id ||
                                              ' is ' || v_name);
            EXCEPTION
              WHEN no_data_found THEN
                DBMS_OUTPUT.PUT_LINE('Employee ' || n_emp_id || ' not found');
          END;
        END

--@FAILURE: Encountered unexpected token: "n_emp_id" <S_IDENTIFIER> recorded first on May 27, 2022, 10:29:48 PM
--@FAILURE: Encountered: <S_IDENTIFIER> / "n_emp_id", at line 11, column 11, in lexical state DEFAULT. recorded first on 15 May 2025, 16:24:08