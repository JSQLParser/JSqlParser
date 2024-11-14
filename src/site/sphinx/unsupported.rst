***************************************
Unsupported Grammar of various RDBMS
***************************************

*JSQLParser* is a RDBMS agnostic parser with a certain focus on SQL:2016 Standard compliant Queries and the "Big Four" (Oracle, MS SQL Server, Postgres, MySQL/MariaDB).
We would like to recommend writing portable, standard compliant  SQL in general.

- Oracle PL/SQL blocks are not support.
    
    .. code-block:: sql

        DECLARE
            num NUMBER;
        BEGIN
            num := 10;
            dbms_output.put_line('The number is ' || num);
        END;



- Oracle `INSERT ALL ...` is not supported
    
    .. code-block:: sql

        INSERT ALL
          INTO mytable (column1, column2, column_n) VALUES (expr1, expr2, expr_n)
          INTO mytable (column1, column2, column_n) VALUES (expr1, expr2, expr_n)
          INTO mytable (column1, column2, column_n) VALUES (expr1, expr2, expr_n)
        SELECT * FROM dual;

- DDL statements

    While *JSQLParser* provides a lot of generic support for DDL statements, it is possible that certain RDBMS specific syntax (especially about indices, encodings, compression) won't be supported.

- Interval Operators

    Anything like `DAY HOUR MINUTE SECOND [TO HOUR MINUTE SECOND]` is not supported.:

    .. code-block:: sql

        values cast ((time '12:03:34' - time '11:57:23') minute to second as varchar(8));




