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
   migration
   SQL Grammar Stable <syntax_stable>
   SQL Grammar Snapshot <syntax_snapshot>
   Unsupported Grammar <unsupported>
   Java API Stable <javadoc_stable>
   Java API Snapshot <javadoc_snapshot>
   keywords
   changelog

.. image:: https://img.shields.io/github/release/JSQLParser/JSqlParser?include_prereleases=&sort=semver&color=blue
    :alt: GitGub Release Badge
    :target:  https://github.com/JSQLParser/JSqlParser/releases

.. image:: https://img.shields.io/github/issues/JSQLParser/JSqlParser
    :alt: GitGub Issues Badge
    :target: https://github.com/JSQLParser/JSqlParser/issues

.. image:: https://badgen.net/maven/v/maven-central/com.github.jsqlparser/jsqlparser
    :alt: Maven Badge
    :target: https://mvnrepository.com/artifact/com.github.jsqlparser/jsqlparser

.. image:: https://github.com/JSQLParser/JSqlParser/actions/workflows/maven.yml/badge.svg
    :alt: Maven Build Status
    :target: https://github.com/JSQLParser/JSqlParser/actions/workflows/maven.yml

.. image:: https://coveralls.io/repos/JSQLParser/JSqlParser/badge.svg?branch=master
    :alt: Coverage Status
    :target: https://coveralls.io/github/com.github.jsqlparser/jsqlparser?branch=master

.. image:: https://app.codacy.com/project/badge/Grade/6f9a2d7eb98f45969749e101322634a1
    :alt: Codacy Status
    :target: https://app.codacy.com/gh/com.github.jsqlparser/jsqlparser/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade

.. image:: https://www.javadoc.io/badge/com.github.jsqlparser/jsqlparser.svg
    :alt: Java Docs
    :target: https://javadoc.io/doc/com.github.jsqlparser/jsqlparser/latest/index.html


**JSQLParser** is a SQL statement parser built from JavaCC. It translates SQLs in a traversable hierarchy of Java classes.

Latest stable release: |JSQLPARSER_STABLE_VERSION_LINK|

Development version: |JSQLPARSER_SNAPSHOT_VERSION_LINK|

.. sidebar:: Java API Website

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






