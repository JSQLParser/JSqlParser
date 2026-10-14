.. meta::
   :description: Java Software Library for parsing SQL Statements into Abstract Syntax Trees (AST) and manipulation of SQL Statements
   :keywords: java sql statement parser abstract syntax tree

###########################
Java SQL Parser Library
###########################

.. toctree::
   :maxdepth: 2
   :hidden:

   usage
   contribution
   migration47
   migration50
   SQL Grammar Stable <syntax_stable>
   SQL Grammar Snapshot <syntax_snapshot>
   Unsupported Grammar <unsupported>
   Java API Stable <javadoc_stable>
   Java API Snapshot <javadoc_snapshot>
   keywords
   changelog

.. image:: https://github.com/JSQLParser/JSqlParser/actions/workflows/ci.yml/badge.svg
    :alt: CI Status
    :target: https://github.com/JSQLParser/JSqlParser/actions/workflows/ci.yml

.. image:: https://coveralls.io/repos/JSQLParser/JSqlParser/badge.svg?branch=master
    :alt: Coverage Status
    :target: https://coveralls.io/r/JSQLParser/JSqlParser?branch=master

.. image:: https://app.codacy.com/project/badge/Grade/6f9a2d7eb98f45969749e101322634a1
    :alt: Codacy Status
    :target: https://www.codacy.com/gh/JSQLParser/JSqlParser/dashboard

.. image:: https://img.shields.io/maven-central/v/com.manticore-projects.jsqlformatter/jsqlparser.svg?label=manticore%20build&color=ff420e
    :alt: Manticore Build
    :target: https://central.sonatype.com/artifact/com.manticore-projects.jsqlformatter/jsqlparser

.. image:: https://img.shields.io/maven-central/v/com.github.jsqlparser/jsqlparser.svg?label=upstream%20release
    :alt: Maven Central
    :target: https://central.sonatype.com/artifact/com.github.jsqlparser/jsqlparser

.. image:: https://www.javadoc.io/badge/com.github.jsqlparser/jsqlparser.svg
    :alt: Java Docs
    :target: https://javadoc.io/doc/com.github.jsqlparser/jsqlparser/latest/index.html

.. image:: https://img.shields.io/github/issues/JSQLParser/JSqlParser
    :alt: GitHub Issues Badge
    :target: https://github.com/JSQLParser/JSqlParser/issues

.. image:: https://img.shields.io/github/stars/JSQLParser/JSqlParser?style=flat&label=stars
    :alt: GitHub Stars
    :target: https://github.com/JSQLParser/JSqlParser/stargazers

.. sidebar:: Java API Website

	.. image:: _images/JavaAST.png

**Turn any SQL statement into a traversable tree of Java objects — and back again.**

**JSQLParser** is an RDBMS-agnostic SQL parser for the JVM, built with JavaCC: one grammar, all major dialects, no native extensions. Give it SQL, get an AST you can walk, rewrite and print back out.

Latest stable release: |JSQLPARSER_STABLE_VERSION_LINK|

Development version: |JSQLPARSER_SNAPSHOT_VERSION_LINK|

.. note::

    Since the 5.0 release JSQLParser requires Java 11 and has introduced new Visitors. Please see the :ref:`Migration to 5.0` guide.


******************************
What it does
******************************

.. code-block:: sql

    SELECT 1 FROM dual WHERE a = b

.. raw:: html

    <div class="highlight">
    <pre>
    SQL Text
     └─Statements: statement.select.PlainSelect
        ├─selectItems: statement.select.SelectItem
        │  └─LongValue: 1
        ├─Table: dual
        └─where: expression.operators.relational.EqualsTo
           ├─Column: a
           └─Column: b
    </pre>
    </div>

.. code-block:: java

    PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse("select 1 from dual where a=b");

    Table table = (Table) select.getFromItem();
    Assertions.assertEquals("dual", table.getName());

The tree is traversable with the Visitor pattern, and the same object model works in reverse: build statements from Java with a fluent API and render them as SQL text. See :doc:`usage`.


******************************
Install
******************************

Use the **Manticore builds**. They are immutable, versioned releases cut continuously from the current development line, and carry the grammar and performance work described below. The upstream ``com.github.jsqlparser`` release is considerably older.

.. code-block:: xml

    <dependency>
        <groupId>com.manticore-projects.jsqlformatter</groupId>
        <artifactId>jsqlparser</artifactId>
        <version>[5.3.218,)</version>
    </dependency>

Upstream coordinates, snapshots and Gradle are on the :ref:`Add JSQLParser to your Project` page.


******************************
Performance
******************************

**11× faster than 5.3**, and the fastest parser on real-world SQL of any of the parsers tested, in any language — 19× ahead of ``sqlglot[c]`` on JSQLParser's own ``SELECT`` test suite.

.. code-block:: text

    Benchmark                               (version)  Mode  Cnt   Score   Error  Units
    JSQLParserBenchmark.parseSQLStatements     latest  avgt   15   7.602 ± 0.135  ms/op
    JSQLParserBenchmark.parseSQLStatements        5.3  avgt   15  84.687 ± 3.321  ms/op

Methodology and the full cross-parser comparison against SQLGlot, ``sqlglot[c]`` and polyglot-sql: `jsqlparser-bench <https://github.com/manticore-projects/jsqlparser-bench>`_.


******************************
What it parses
******************************

One grammar covers the SQL standard plus all major RDBMS. Missing syntax gets added on demand — `open an issue <https://github.com/JSQLParser/JSqlParser/issues>`_.

``BigQuery`` · ``Snowflake`` · ``DuckDB`` · ``Redshift`` · ``Oracle`` · ``MS SQL Server`` · ``Sybase`` · ``PostgreSQL`` · ``MySQL`` · ``MariaDB`` · ``DB2`` · ``H2`` · ``HSQLDB`` · ``Derby`` · ``SQLite``

.. list-table::
    :header-rows: 1
    :widths: 25 75

    * -
      - Statements
    * - **Queries**
      - ``SELECT`` · ``WITH …`` · Piped SQL
    * - **DML**
      - ``INSERT`` · ``UPDATE`` · ``UPSERT`` · ``MERGE`` · ``DELETE`` · ``TRUNCATE TABLE``
    * - **DDL**
      - ``CREATE …`` · ``ALTER …`` · ``DROP …``
    * - **MySQL table maintenance**
      - ``ANALYZE`` · ``CHECK`` · ``OPTIMIZE`` · ``REPAIR TABLE``
    * - **MySQL account management**
      - ``CREATE``/``ALTER``/``RENAME USER`` · ``CREATE ROLE`` · ``SET ROLE`` · ``SET DEFAULT ROLE`` · ``SET PASSWORD``
    * - **PostgreSQL RLS**
      - ``CREATE POLICY`` · ``ALTER TABLE … ENABLE``/``DISABLE``/``FORCE``/``NO FORCE ROW LEVEL SECURITY``
    * - **Salesforce SOQL**
      - ``INCLUDES`` · ``EXCLUDES``

Beyond statement shapes: nested sub-selects, bind parameters (``?``, ``:name``), window and analytic functions, Oracle hints, the old Oracle ``JOIN (+)``, PostgreSQL implicit ``CAST ::``, and the T-SQL square-bracket versus array-literal ambiguity. The complete reference is on the :doc:`syntax_stable` page; the gaps are on :doc:`unsupported`.


******************************
Statement classification
******************************

Any parsed statement can say what it actually does — no second parse, no visitor to write:

.. code-block:: java

    StatementFeatures features = CCJSqlParserUtil.parse(sqlStr).getFeatures();

    // safeguard a read-only client before anything reaches the database
    if (connection.isReadOnly() && features.mayModifyData()) {
        throw new SQLException("rejected: " + features.getUnresolvedReferences());
    }

    // dispatch correctly
    if (features.returnsResultSet()) { statement.executeQuery(sqlStr);  }
    else                             { statement.executeUpdate(sqlStr); }

This is not ``sqlStr.startsWith("SELECT")`` with extra steps: ``RETURNING`` turns DML into a row source, a data-modifying CTE hides a ``DELETE`` inside a ``SELECT``, and ``INSERT INTO x SELECT ..`` contains a query but returns nothing. See :ref:`Classify a Statement`.


******************************
Piped SQL
******************************

Support is progressing for Piped SQL, which writes queries in the order they actually execute rather than the order SQL historically demanded.

.. code-block:: sql

    FROM Produce
    |> WHERE
        item != 'bananas'
        AND category IN ('fruit', 'nut')
    |> AGGREGATE COUNT(*) AS num_items, SUM(sales) AS total_sales
       GROUP BY item
    |> ORDER BY item DESC;

Background reading: the `Google research paper <https://storage.googleapis.com/gweb-research2023-media/pubtools/1004848.pdf>`_, `BigQuery pipe syntax <https://cloud.google.com/bigquery/docs/reference/standard-sql/pipe-syntax>`_ and `DuckDB FROM-first syntax <https://duckdb.org/docs/sql/query_syntax/from.html#from-first-syntax>`_.


******************************
Java version
******************************

.. list-table::
    :header-rows: 1
    :widths: 25 20 55

    * - JSQLParser
      - Runtime
      - Notes
    * - 4.9
      - JDK 8
      - last JDK 8 compatible release
    * - 5.0 and later
      - JDK 11
      - breaking changes to the AST Visitors, see :ref:`Migration to 5.0`
    * - 5.1 and later
      - JDK 11
      - building requires a **JDK 17 toolchain** (plugin requirement)
    * - 5.4 and later
      - JDK 11
      - parser generated with **JavaCC 8**


******************************
Sister projects
******************************

- `JSQLFormatter <https://manticore-projects.com/JSQLFormatter/index.html>`_ — pretty-printing and formatting of SQL text
- `JSQLTranspiler <https://manticore-projects.com/JSQLTranspiler/index.html>`_ — dialect-specific rewriting, column resolution and lineage, by `Starlake.ai <https://starlake.ai/>`_


******************************
Sponsor
******************************

A huge thank you to our sponsor, `Starlake.ai <https://starlake.ai/>`_, who simplify data ingestion, transformation and orchestration, enabling faster delivery of high-quality data. Starlake has been instrumental in providing Piped SQL support and numerous test cases for BigQuery, Redshift, Databricks and DuckDB. Show your support for ongoing development by visiting Starlake.ai and giving them a star.


******************************
License
******************************

Dual licensed under **LGPL 2.1** or the **Apache License, Version 2.0**. Take your pick.