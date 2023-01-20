###########################
Java SQL Parser Library
###########################

.. toctree::
   :maxdepth: 2
   :hidden:

   usage
   contribution
   syntax
   keywords
   changelog

.. image:: https://maven-badges.herokuapp.com/maven-central/com.github.jsqlparser/jsqlparser/badge.svg
    :alt: Maven Repo

.. image:: https://github.com/JSQLParser/JSqlParser/actions/workflows/maven.yml/badge.svg
    :alt: Maven Build Status

.. image:: https://coveralls.io/repos/JSQLParser/JSqlParser/badge.svg?branch=master
    :alt: Coverage Status

.. image:: https://app.codacy.com/project/badge/Grade/6f9a2d7eb98f45969749e101322634a1
    :alt: Codacy Status

.. image:: https://www.javadoc.io/badge/com.github.jsqlparser/jsqlparser.svg
    :alt: Java Docs

**JSQLParser** is a SQL statement parser built from JavaCC. It translates SQLs in a traversable hierarchy of Java classes.

Latest stable release: |JSQLPARSER_STABLE_VERSION_LINK|

Development version: |JSQLPARSER_SNAPSHOT_VERSION_LINK|

.. sidebar:: Java SQL Object Hierarchy

	.. image:: _images/JavaAST.png


.. code-block:: SQL
    :caption: Sample SQL Statement

    SELECT /*+ PARALLEL */
        cfe.id_collateral_ref.nextval
        , id_collateral
    FROM (  SELECT DISTINCT
                a.id_collateral
            FROM cfe.collateral a
                LEFT JOIN cfe.collateral_ref b
                    ON a.id_collateral = b.id_collateral
            WHERE b.id_collateral_ref IS NULL )
    ;


******************************
SQL Dialects
******************************

**JSqlParser** is RDBMS agnostic and provides support for many dialects such as:

    * Oracle Database
    * MS SqlServer
    * MySQL and MariaDB
    * PostgreSQL
    * H2

*******************************
Features
*******************************

    * Comprehensive support for statements:
        - QUERY: ``SELECT ...``
        - DML: ``INSERT ... INTO ...`` ``UPDATE ...`` ``MERGE ... INTO ...`` ``DELETE ... FROM ...``
        - DDL: ``CREATE ...`` ``ALTER ...`` ``DROP ...``

    * Nested Expressions (e.g. Sub-Selects)
    * ``WITH`` clauses
    * Old Oracle ``JOIN (+)``
    * PostgreSQL implicit ``CAST ::``
    * SQL Parameters (e.g. ``?`` or ``:parameter``)
    * Arrays vs. T-SQL Squared Bracket Quotes
    * Fluent API to create SQL Statements from java Code
    * Statement De-Parser to write SQL from Java Objects






