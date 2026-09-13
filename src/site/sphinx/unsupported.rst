***************************************
Unsupported Grammar of various RDBMS
***************************************

*JSQLParser* is a RDBMS agnostic parser with a certain focus on SQL:2016 Standard compliant Queries and the "Big Four" (Oracle, MS SQL Server, Postgres, MySQL/MariaDB).
We would like to recommend writing portable, standard compliant SQL in general.

Missing syntax is added on demand — please `open an issue <https://github.com/JSQLParser/JSqlParser/issues>`_ with a minimal example.


Procedural SQL
=======================================

Procedural-language support is partial. Accepting a block or routine definition does not necessarily mean that its body is parsed into a statement tree. Select the relevant dialect as described in :ref:`Choose a Dialect`.

.. code-block:: sql
    :caption: Oracle anonymous block — supported with Dialect.ORACLE

    DECLARE
        num NUMBER;
    BEGIN
        num := 10;
        dbms_output.put_line('The number is ' || num);
    END;

.. code-block:: postgresql
    :caption: PostgreSQL DO — supported with Dialect.POSTGRESQL; body remains opaque

    DO $$
    BEGIN
        RAISE NOTICE 'hello';
    END
    $$;

The PostgreSQL example produces a ``DoStatement`` whose ``getCode()`` is a ``StringValue``. The body, quotes and dollar tag are preserved, and statements following the block are parsed separately. PL/pgSQL declarations, conditions and statements inside that literal are not exposed as child AST nodes. Table discovery therefore rejects the opaque body rather than reporting an incomplete table list.

Full procedural-language coverage, including cursors, loops and ``ELSIF``, remains outside the supported subset.

What *is* supported:

- ``BEGIN .. END`` blocks and ``IF .. ELSE`` around ordinary statements (``Block``, ``IfElseStatement``)
- ``DECLARE @variable`` in the T-SQL sense (``DeclareStatement``)
- PostgreSQL ``DO`` wrappers with the body preserved as a string literal (``DoStatement``)
- Selected Oracle anonymous blocks, including variable declarations, assignments and exception handlers (``OracleBlock``); see :ref:`Oracle anonymous blocks`

Routine and trigger definitions
---------------------------------------

``CREATE FUNCTION`` and ``CREATE PROCEDURE`` **are** accepted, but the body is captured as an opaque token list rather than parsed into a tree. You get a ``CreateFunction`` / ``CreateProcedure`` that round-trips to the original text — you cannot traverse or rewrite what is inside it.

``CREATE TRIGGER`` follows the MySQL shape (``FOR EACH ROW`` with a single body statement).

.. note::

    Both are reported as ``OPAQUE`` by :ref:`Classify a Statement` — the analysis will not claim a routine body is side-effect free, because nothing about it is knowable.


Query features not implemented
=======================================

.. list-table::
    :header-rows: 1
    :widths: 30 70

    * - Feature
      - Notes
    * - ``MATCH_RECOGNIZE``
      - SQL:2016 row pattern recognition (Oracle, Snowflake, Flink, Trino)
    * - Oracle ``MODEL`` clause
      - spreadsheet-style inter-row calculation



Dialect-specific syntax
=======================================

The remaining gaps are narrow: one product's spelling of one construct. The `open issues <https://github.com/JSQLParser/JSqlParser/issues>`_ are the accurate list at any moment — recurring themes are Informix and Teradata extensions, T-SQL ``FOR XML PATH`` and table variables, and assorted vendor-specific DDL options.

If you hit one, you do not have to abandon the parse:

.. code-block:: java

    Statements statements = CCJSqlParserUtil.parseStatements(
            sqlStr, parser -> parser.withUnsupportedStatements() );

The offending statement comes back as an ``UnsupportedStatement`` holding its original text, and the rest of the script parses normally. See :ref:`Handle Parse Errors`.

Before concluding something is unsupported, check the parser features: square brackets, backslash escapes, double-quoted strings and hash comments are all **off by default** and are the most common cause of a "not supported" report that is really a dialect setting. See :ref:`Choose a Dialect`.
