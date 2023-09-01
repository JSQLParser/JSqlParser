
************************
Changelog
************************


Latest Changes since |JSQLPARSER_VERSION|
=============================================================


  * **feat: BigQuery Except(..) Replace(..) syntax**
    
    Andreas Reichel, 2023-09-01
  * **test: add test for Postgres `NOTNULL` expression**
    
    Andreas Reichel, 2023-08-31
  * **build: Disable Signing with in Memory keys temporarily**
    
    Andreas Reichel, 2023-08-29
  * **fix: ExpressionList of Expressions in `Values`**
    
    Andreas Reichel, 2023-08-26
  * **style: add license headers**
    
    Andreas Reichel, 2023-08-23
  * **feat: implement a few missing expressions**
    
    Andreas Reichel, 2023-08-23
  * **fix: check for NULL before iterating**
    
    Andreas Reichel, 2023-08-23
  * **doc: explain the TablesNamesFinder, fix the example**
    
    Andreas Reichel, 2023-08-20
  * **doc: explain the TablesNamesFinder**
    
    Andreas Reichel, 2023-08-20
  * **Fixing a problem with an OP_CONCAT in WhenExpression (#1837)**
    
    amigalev, 2023-08-20
  * **refactor: TablesNamesFinder**
    
    Andreas Reichel, 2023-08-20
  * **build: disable Gradle publish**
    
    Andreas Reichel, 2023-08-20
  * **refactor: TableFunction extends Function, supports `LATERAL` prefix**
    
    Andreas Reichel, 2023-08-20
  * **Update Gradle JavaCC parser to latest version (3.0.0) (#1843)**
    
    Zbynek Konecny, 2023-08-05
  * **Update sql-parser-error.md**
    
    manticore-projects, 2023-07-26
  * **Update sql-parser-error.md**
    
    manticore-projects, 2023-07-26
  * **Update sql-parser-error.md**
    
    manticore-projects, 2023-07-25
  * **feat: SQL:2016 TABLESAMPLE clause**
    
    Andreas Reichel, 2023-07-12
  * **feat: add a method checking balanced brackets**
    
    Andreas Reichel, 2023-07-12
  * **build: publish Snapshots on commit**
    
    Andreas Reichel, 2023-07-12
  * **build: publish Snapshots on commit**
    
    Andreas Reichel, 2023-07-03
  * **build: publish Snapshots on commit**
    
    Andreas Reichel, 2023-07-02
  * **build: publish Snapshots on commit**
    
    Andreas Reichel, 2023-07-02
  * **build: publish Snapshots on commit**
    
    Andreas Reichel, 2023-07-02
  * **build: publish Snapshots on commit**
    
    Andreas Reichel, 2023-07-02
  * **feat: add support for INTERPRET function parsing (#1816)**
    
    Matteo Sist, 2023-06-29
  * **style: Licenses from Maven plugin**
    
    Andreas Reichel, 2023-06-27
  * **fix: Backslash escaped single quote `'\''`**
    
    Andreas Reichel, 2023-06-27
  * **Assorted Fixes #8 (#1807)**
    
    manticore-projects, 2023-06-15
  * **fix: `INSERT` must use simple Column Names only**
    
    Andreas Reichel, 2023-06-15
  * **feat: MySQL `NOT RLIKE`, `NOT REGEXP` expressions**
    
    Andreas Reichel, 2023-06-15
  * **feat: Postgres `NOTNULL` support**
    
    manticore-projects, 2023-06-15
  * **feat: `QUALIFY` clause**
    
    Andreas Reichel, 2023-06-15
  * **docs: write migration guide**
    
    Andreas Reichel, 2023-06-11
  * **fix: SPHINX modules and themes**
    
    Andreas Reichel, 2023-06-02
  * **feat: T-SQL `FOR ...` clause**
    
    Andreas Reichel, 2023-06-02
  * **doc: migration guide**
    
    Andreas Reichel, 2023-06-02
  * **fix: expose IntervalExpression attributes and use DeParser**
    
    Andreas Reichel, 2023-06-01
  * **Update sphinx.yml**
    
    manticore-projects, 2023-06-01
  * **JSQLParser 5.0 (#1778)**
    
    manticore-projects, 2023-06-01
  * **doc: write migration guide**
    
    Andreas Reichel, 2023-05-29
  * **fix: throw the specific exception**
    
    Andreas Reichel, 2023-05-29
  * **doc: Website, fix tabs**
    
    Andreas Reichel, 2023-05-24
  * **doc: Website improvements**
    
    Andreas Reichel, 2023-05-22
  * **build: improve Upload task**
    
    Andreas Reichel, 2023-05-19
  * **feat: Quoted Identifiers can contain double-quotes (PostgreSQL)**
    
    Andreas Reichel, 2023-05-18
  * **Create gradle.yml**
    
    manticore-projects, 2023-05-18
  * **feat: functions blocks, parenthesed JSON Expressions**
    
    Andreas Reichel, 2023-05-18
  * **feat: functions blocks, parenthesed JSON Expressions**
    
    Andreas Reichel, 2023-05-18
  * **feat: parse CREATE TRIGGER as UnsupportedStatement**
    
    Andreas Reichel, 2023-05-17
  * **build: try to work around the Maven/JDK8 issue on GitHub**
    
    Andreas Reichel, 2023-05-17
  * **refact: Statements extends List<Statement>**
    
    Andreas Reichel, 2023-05-17
  * **style: remove unused imports**
    
    Andreas Reichel, 2023-05-17
  * **feat: chaining JSON Expressions**
    
    Andreas Reichel, 2023-05-17
  * **style: Cosmetic improvements**
    
    Andreas Reichel, 2023-05-17
  * **style: Quieten the logger**
    
    Andreas Reichel, 2023-05-17
  * **fix: Complex Parsing Approach**
    
    Andreas Reichel, 2023-05-17
  * **refactor: CREATE and ALTER productions**
    
    Andreas Reichel, 2023-05-16
  * **refactor: RETURNING clause**
    
    Andreas Reichel, 2023-05-16
  * **refactor: SHOW statement, supporting any RDBMS specific implementation**
    
    Andreas Reichel, 2023-05-16
  * **refactor: simplify production `CreateParameter()`**
    
    Andreas Reichel, 2023-05-16
  * **fix: issue #1789**
    
    Andreas Reichel, 2023-05-16
  * **fix: issue #1789**
    
    Andreas Reichel, 2023-05-16
  * **fix: issue #1791**
    
    Andreas Reichel, 2023-05-15
  * **build: improve the GIT Snapshot detection**
    
    Andreas Reichel, 2023-05-15
  * **build: Sphinx build fixes**
    
    Andreas Reichel, 2023-05-14
  * **build: Sphinx build fixes**
    
    Andreas Reichel, 2023-05-14
  * **build: Sphinx build fixes**
    
    Andreas Reichel, 2023-05-14
  * **Update sphinx.yml**
    
    manticore-projects, 2023-05-14
  * **feat: Write API documentation to the WebSite via XMLDoclet**
    
    Andreas Reichel, 2023-05-14
  * **test: add unit test for issue #1778**
    
    Andreas Reichel, 2023-05-11
  * **style: appease PMD/Codacy**
    
    Andreas Reichel, 2023-05-11
  * **style: appease PMD/Codacy**
    
    Andreas Reichel, 2023-05-11
  * **feat: `MEMBER OF` condition as shown at https://dev.mysql.com/doc/refman/8.0/en/json-search-functions.html#operator_member-of**
    
    Andreas Reichel, 2023-05-11
  * **feat: access Elements of Array Columns**
    
    Andreas Reichel, 2023-05-11
  * **feat: JdbcNamedParameter allows "&" (instead of ":")**
    
    Andreas Reichel, 2023-05-11
  * **fix: Java Version 8**
    
    Andreas Reichel, 2023-05-09
  * **refactor: generify `SelectItem` and remove `FunctionItem` and `ExpressionListItem`**
    
    Andreas Reichel, 2023-05-09
  * **style: replace all List<Expression> with ExpressionList<> and enforce policy via Acceptance Test**
    
    Andreas Reichel, 2023-05-09
  * **fix: find the correct position when field belongs to an internal class**
    
    Andreas Reichel, 2023-05-09
  * **style: Appease PMD**
    
    Andreas Reichel, 2023-05-07
  * **style: Appease Checkstyle**
    
    Andreas Reichel, 2023-05-07
  * **test: Disable API Sanitation for the moment**
    
    Andreas Reichel, 2023-05-07
  * **refactor: `Insert` uses `ExpressionList` and `UpdateSet`**
    
    Andreas Reichel, 2023-05-07
  * **build: improve Gradle Build**
    
    Andreas Reichel, 2023-05-07
  * **refactor: remove SimpleFunction**
    
    Andreas Reichel, 2023-05-06
  * **doc: RR chart colors cater for Dark Mode**
    
    Andreas Reichel, 2023-05-06
  * **doc: Better Sphinx Tabs**
    
    Andreas Reichel, 2023-05-06
  * **style: Rework all the ENUMs**
    
    Andreas Reichel, 2023-05-05
  * **style: Appease Codacy**
    
    Andreas Reichel, 2023-05-04
  * **refactor: Remove `ItemsList`, `MultiExpressionList`, `Replace`**
    
    Andreas Reichel, 2023-05-04
  * **style: Checkstyle**
    
    Andreas Reichel, 2023-05-03
  * **style: Appease Codacy**
    
    Andreas Reichel, 2023-05-03
  * **build: Increase TimeOut for the GitHub CI**
    
    Andreas Reichel, 2023-05-03
  * **refactor: UpdateSets for `Update` and `InsertConflictTarget`**
    
    Andreas Reichel, 2023-05-03
  * **fix: Remove tests for `()`, since `ParenthesedExpressionList` will catch those too**
    
    Andreas Reichel, 2023-05-03
  * **feat: Consolidate the `ExpressionList`, removing many redundant List alike Classes and Productions**
    
    Andreas Reichel, 2023-05-03
  * **Revert "fix: assign Enum case insensitive"**
    
    Andreas Reichel, 2023-05-02
  * **fix: assign Enum case insensitive**
    
    Andreas Reichel, 2023-05-02
  * **doc: Update the README.md**
    
    Andreas Reichel, 2023-05-01
  * **build: Add missing import**
    
    Andreas Reichel, 2023-04-30
  * **doc: Update examples**
    
    Andreas Reichel, 2023-04-30
  * **refactor: remove `SelectExpressionItem` in favor of `SelectItem`**
    
    Andreas Reichel, 2023-04-30
  * **test: add specific tests for closed issues**
    
    Andreas Reichel, 2023-04-30
  * **test: add specific tests for closed issues**
    
    Andreas Reichel, 2023-04-30
  * **feat: ClickHouse `LIMIT ... BY ...` clause**
    
    Andreas Reichel, 2023-04-30
  * **feat: implement SQL:2016 Convert() and Trim()**
    
    Andreas Reichel, 2023-04-30
  * **feat: Switch off contradicting `JOIN` qualifiers, when setting a qualifier**
    
    Andreas Reichel, 2023-04-30
  * **feat: Test if a JOIN is an INNER JOIN according to the SQL:2016**
    
    Andreas Reichel, 2023-04-30
  * **feat: ClickHouse `Select...` ``FINAL` modifier**
    
    Andreas Reichel, 2023-04-29
  * **feat: Multi-Part Names for Variables and Parameters**
    
    Andreas Reichel, 2023-04-29
  * **feat: Oracle `HAVING` before `GROUP BY`**
    
    Andreas Reichel, 2023-04-29
  * **feat: Lateral View**
    
    Andreas Reichel, 2023-04-29
  * **Fix #1758: Use long for Feature.timeOut (#1759)**
    
    Tomasz Zarna, 2023-04-27
  * **Ignoring unnecessarily generated jacoco report (#1762)**
    
    optimizing-ci-builds, 2023-04-27
  * **Ignoring unnecessarily generated by pmd plugin (#1763)**
    
    optimizing-ci-builds, 2023-04-27
  * **Refactor Parenthesed SelectBody and FromItem (#1754)**
    
    manticore-projects, 2023-04-27
  * **Assorted Fixes #7 (#1745)**
    
    manticore-projects, 2023-03-21
  * **disable xml report (#1748)**
    
    optimizing-ci-builds, 2023-03-21
  * **Assorted Fixes #6 (#1740)**
    
    manticore-projects, 2023-03-09
  * **test: commit missing test**
    
    Andreas Reichel, 2023-03-07
  * **style: apply Spotless**
    
    Andreas Reichel, 2023-03-07
  * **feat: FETCH uses EXPRESSION**
    
    Andreas Reichel, 2023-03-07
  * **version 4.7-SNAPSHOT**
    
    Tobias Warneke, 2023-02-23
  * **[maven-release-plugin] prepare for next development iteration**
    
    Tobias Warneke, 2023-02-23
  * **feat: Support more Statement Separators**
    
    Andreas Reichel, 2023-02-02
  * **Update issue templates**
    
    manticore-projects, 2023-02-01
  * **Update issue templates**
    
    manticore-projects, 2023-02-01
  * **doc: fix the issue template**
    
    Andreas Reichel, 2023-02-01
  * **feat: CREATE VIEW ... REFRESH AUTO...**
    
    Andreas Reichel, 2023-01-30
  * **style: Appease PMD/Codacy**
    
    Andreas Reichel, 2023-01-29
  * **feat: Oracle Alternative Quoting**
    
    Andreas Reichel, 2023-01-29
  * **doc: Better integration of the RR diagrams**
    
    Andreas Reichel, 2023-01-21
  * **feat: make important Classes Serializable**
    
    Andreas Reichel, 2023-01-21
  * **chore: Make Serializable**
    
    Andreas Reichel, 2023-01-21
  * **doc: request for `Conventional Commit` messages**
    
    Andreas Reichel, 2023-01-21
  * **Sphinx Documentation**
    
    Andreas Reichel, 2023-01-21
  * **Define Reserved Keywords explicitly**
    
    Andreas Reichel, 2023-01-21
  * **Adjust Gradle to JUnit 5**
    
    Andreas Reichel, 2023-01-21
  * **Enhanced Keywords**
    
    Andreas Reichel, 2023-01-21
  * **Remove unused imports**
    
    Andreas Reichel, 2023-01-21
  * **Fix test resources**
    
    Andreas Reichel, 2023-01-21
  * **Do not mark SpeedTest for concurrent execution**
    
    Andreas Reichel, 2023-01-21
  * **Fix incorrect tests**
    
    Andreas Reichel, 2023-01-21
  * **Remove unused imports**
    
    Andreas Reichel, 2023-01-21
  * **Adjust Gradle to JUnit 5**
    
    Andreas Reichel, 2023-01-21
  * **Do not mark SpeedTest for concurrent execution**
    
    Andreas Reichel, 2023-01-21
  * **Reduce cyclomatic complexity in CreateView.toString**
    
    zaza, 2023-01-08
  * **Fixes #1684: Support CREATE MATERIALIZED VIEW with AUTO REFRESH**
    
    zaza, 2022-12-11

Version jsqlparser-4.6
=============================================================


  * **[maven-release-plugin] prepare release jsqlparser-4.6**
    
    Tobias Warneke, 2023-02-23
  * **actualized release plugin**
    
    Tobias Warneke, 2023-02-23
  * **actualized release plugin**
    
    Tobias Warneke, 2023-02-23
  * **Update build.gradle**
    
    Tobias, 2023-02-17
  * **Update README.md**
    
    Tobias, 2023-02-17
  * **Oracle Alternative Quoting (#1722)**
    
    manticore-projects, 2023-02-07
  * **Issue1673 case within brackets (#1675)**
    
    manticore-projects, 2023-01-31
  * **Added support for SHOW INDEX from table (#1704)**
    
    Jayant Kumar Yadav, 2023-01-31
  * **Sphinx Website (#1624)**
    
    manticore-projects, 2023-01-20
  * **Assorted Fixes #5 (#1715)**
    
    manticore-projects, 2023-01-20
  * **Support DROP MATERIALIZED VIEW statements (#1711)**
    
    Tomasz Zarna, 2023-01-12
  * **corrected readme**
    
    Tobias Warneke, 2023-01-04
  * **Update README.md**
    
    Tobias, 2022-12-27
  * **Fix #1686: add support for creating views with "IF NOT EXISTS" clause (#1690)**
    
    Tomasz Zarna, 2022-12-22
  * **Assorted Fixes #4 (#1676)**
    
    manticore-projects, 2022-12-22
  * **Fixed download war script in the renderRR task (#1659)**
    
    haha1903, 2022-12-10
  * **Assorted fixes (#1666)**
    
    manticore-projects, 2022-11-20
  * **Fix parsing statements with multidimensional array PR2 (#1665)**
    
    manticore-projects, 2022-11-20
  * **removed disabled from Keyword tests and imports**
    
    Tobias Warneke, 2022-11-02
  * **removed disabled from Keyword tests**
    
    Tobias Warneke, 2022-11-02
  * **Keywords2: Update whitelisted Keywords (#1653)**
    
    manticore-projects, 2022-11-02
  * **Enhanced Keywords (#1382)**
    
    manticore-projects, 2022-10-25
  * **#1610 Support for SKIP LOCKED tokens on SELECT statements (#1649)**
    
    Lucas Dillmann, 2022-10-25
  * **Assorted fixes (#1646)**
    
    manticore-projects, 2022-10-16
  * **actualized multiple dependencies**
    
    Tobias Warneke, 2022-09-28
  * **Bump h2 from 1.4.200 to 2.1.210 (#1639)**
    
    dependabot[bot], 2022-09-28
  * **Support BigQuery SAFE_CAST (#1622) (#1634)**
    
    dequn, 2022-09-20
  * **fix: add missing public Getter (#1632)**
    
    manticore-projects, 2022-09-20
  * **Support timestamptz dateliteral (#1621)**
    
    Todd Pollak, 2022-08-31
  * **fixes #1617**
    
    Tobias Warneke, 2022-08-31
  * **fixes #419**
    
    Tobias Warneke, 2022-08-31
  * **Closes #1604, added simple OVERLAPS support (#1611)**
    
    Rob Audenaerde, 2022-08-16
  * **Fixes  PR #1524 support hive alter sql (#1609)**
    
    manticore-projects, 2022-08-14
  * **#1524  support hive alter sql : ALTER TABLE name ADD COLUMNS (col_spec[, col_spec ...]) (#1605)**
    
    Zhumin-lv-wn, 2022-08-03
  * **fixes #1581**
    
    Tobias Warneke, 2022-07-25
  * **Using own Feature - constant for "delete with returning" #1597 (#1598)**
    
    gitmotte, 2022-07-25
  * **[maven-release-plugin] prepare for next development iteration**
    
    Tobias Warneke, 2022-07-22

Version jsqlparser-4.5
=============================================================


  * **[maven-release-plugin] prepare release jsqlparser-4.5**
    
    Tobias Warneke, 2022-07-22
  * **introduced changelog generator**
    
    Tobias Warneke, 2022-07-22
  * **fixes #1596**
    
    Tobias Warneke, 2022-07-22
  * **integrated test for #1595**
    
    Tobias Warneke, 2022-07-19
  * **reduced time to parse exception to minimize impact on building time**
    
    Tobias Warneke, 2022-07-19
  * **add support for drop column if exists (#1594)**
    
    rrrship, 2022-07-19
  * **PostgreSQL INSERT ... ON CONFLICT Issue #1551 (#1552)**
    
    manticore-projects, 2022-07-19
  * **Configurable Parser Timeout via Feature (#1592)**
    
    manticore-projects, 2022-07-19
  * **fixes #1590**
    
    Tobias Warneke, 2022-07-19
  * **fixes #1590**
    
    Tobias Warneke, 2022-07-19
  * **extended support Postgres' `Extract( field FROM source)` where `field` is a String instead of a Keyword (#1591)**
    
    manticore-projects, 2022-07-19
  * **Closes #1579. Added ANALYZE <table> support. (#1587)**
    
    Rob Audenaerde, 2022-07-14
  * **Closes #1583:: Implement Postgresql optional TABLE in TRUNCATE (#1585)**
    
    Rob Audenaerde, 2022-07-14
  * **Support table option character set and index options (#1586)**
    
    luofei, 2022-07-14
  * **corrected a last minute bug**
    
    Tobias Warneke, 2022-07-09
  * **corrected a last minute bug**
    
    Tobias Warneke, 2022-07-09
  * **corrected a last minute bug**
    
    Tobias Warneke, 2022-07-09
  * **fixes #1576**
    
    Tobias Warneke, 2022-07-09
  * **added simple test for #1580**
    
    Tobias Warneke, 2022-07-07
  * **disabled test for large cnf expansion and stack overflow problem**
    
    Tobias Warneke, 2022-07-07
  * **Add test for LikeExpression.setEscape and LikeExpression.getStringExpression (#1568)**
    
    Caro, 2022-07-07
  * **add support for postgres drop function statement (#1557)**
    
    rrrship, 2022-07-06
  * **Add support for Hive dialect GROUPING SETS. (#1539)**
    
    chenwl, 2022-07-06
  * **fixes #1566**
    
    Tobias Warneke, 2022-06-28
  * **Postgres NATURAL LEFT/RIGHT joins (#1560)**
    
    manticore-projects, 2022-06-28
  * **compound statement tests (#1545)**
    
    Matthew Rathbone, 2022-06-08
  * **Allow isolation keywords as column name and aliases (#1534)**
    
    Tomer Shay (Shimshi), 2022-05-19
  * **added github action badge**
    
    Tobias, 2022-05-16
  * **Create maven.yml**
    
    Tobias, 2022-05-16
  * **introduced deparser and toString correction for insert output clause**
    
    Tobias Warneke, 2022-05-15
  * **revived compilable status after merge**
    
    Tobias Warneke, 2022-05-15
  * **INSERT with SetOperations (#1531)**
    
    manticore-projects, 2022-05-15
  * **#1516 rename without column keyword (#1533)**
    
    manticore-projects, 2022-05-11
  * **Add support for `... ALTER COLUMN ... DROP DEFAULT` (#1532)**
    
    manticore-projects, 2022-05-11
  * **#1527 DELETE ... RETURNING ... (#1528)**
    
    manticore-projects, 2022-05-11
  * **fixs #1520 (#1521)**
    
    chiangcho, 2022-05-11
  * **Unsupported statement (#1519)**
    
    manticore-projects, 2022-05-11
  * **fixes #1518**
    
    Tobias Warneke, 2022-04-26
  * **Update bug_report.md (#1512)**
    
    manticore-projects, 2022-04-22
  * **changed to allow #1481**
    
    Tobias Warneke, 2022-04-22
  * **Performance Improvements (#1439)**
    
    manticore-projects, 2022-04-14
  * **[maven-release-plugin] prepare for next development iteration**
    
    Tobias Warneke, 2022-04-10

Version jsqlparser-4.4
=============================================================


  * **[maven-release-plugin] prepare release jsqlparser-4.4**
    
    Tobias Warneke, 2022-04-10
  * **Json function Improvements (#1506)**
    
    manticore-projects, 2022-04-09
  * **fixes #1505**
    
    Tobias Warneke, 2022-04-09
  * **fixes #1502**
    
    Tobias Warneke, 2022-04-09
  * **Issue1500 - Circular References in `AllColumns` and `AllTableColumns` (#1501)**
    
    manticore-projects, 2022-04-03
  * **Optimize assertCanBeParsedAndDeparsed (#1389)**
    
    manticore-projects, 2022-04-02
  * **Add geometry distance operator (#1493)**
    
    Thomas Powell, 2022-04-02
  * **Support WITH TIES option in TOP #1435 (#1479)**
    
    Olivier Cavadenti, 2022-04-02
  * **https://github.com/JSQLParser/JSqlParser/issues/1483 (#1485)**
    
    gitmotte, 2022-04-02
  * **fixes #1482**
    
    Tobias Warneke, 2022-03-15
  * **fixes #1482**
    
    Tobias Warneke, 2022-03-15
  * **Extending CaseExpression, covering #1458 (#1459)**
    
    Mathieu Goeminne, 2022-03-15
  * **fixes #1471**
    
    Tobias Warneke, 2022-02-18
  * **fixes #1471**
    
    Tobias Warneke, 2022-02-18
  * **fixes #1470**
    
    Tobias Warneke, 2022-02-06
  * **Add support for IS DISTINCT FROM clause (#1457)**
    
    Tomer Shay (Shimshi), 2022-01-18
  * **fix fetch present in the end of union query (#1456)**
    
    chiangcho, 2022-01-18
  * **added SQL_CACHE implementation and changed**
    
    Tobias Warneke, 2022-01-09
  * **support for db2 with ru (#1446)**
    
    chiangcho, 2021-12-20
  * **[maven-release-plugin] prepare for next development iteration**
    
    Tobias Warneke, 2021-12-12

Version jsqlparser-4.3
=============================================================


  * **[maven-release-plugin] prepare release jsqlparser-4.3**
    
    Tobias Warneke, 2021-12-12
  * **updated readme.md to show all changes for version 4.3**
    
    Tobias Warneke, 2021-12-12
  * **Adjust Gradle to JUnit 5 (#1428)**
    
    manticore-projects, 2021-11-28
  * **corrected some maven plugin versions**
    
    Tobias Warneke, 2021-11-28
  * **fixes #1429**
    
    Tobias Warneke, 2021-11-23
  * **closes #1427**
    
    Tobias Warneke, 2021-11-21
  * **CreateTableTest**
    
    Tobias Warneke, 2021-11-21
  * **Support EMIT CHANGES for KSQL (#1426)**
    
    Olivier Cavadenti, 2021-11-21
  * **SelectTest.testMultiPartColumnNameWithDatabaseNameAndSchemaName**
    
    Tobias Warneke, 2021-11-21
  * **reformatted test source code**
    
    Tobias Warneke, 2021-11-21
  * **organize imports**
    
    Tobias Warneke, 2021-11-21
  * **replaced all junit 3 and 4 with junit 5 stuff**
    
    Tobias Warneke, 2021-11-21
  * **Support RESTART without value (#1425)**
    
    Olivier Cavadenti, 2021-11-20
  * **Add support for oracle UnPivot when use multi columns at once. (#1419)**
    
    LeiJun, 2021-11-19
  * **Fix issue in parsing TRY_CAST() function (#1391)**
    
    Prashant Sutar, 2021-11-19
  * **fixes #1414**
    
    Tobias Warneke, 2021-11-19
  * **Add support for expressions (such as columns) in AT TIME ZONE expressions (#1413)**
    
    Tomer Shay (Shimshi), 2021-11-19
  * **Add supported for quoted cast expressions for PostgreSQL (#1411)**
    
    Tomer Shay (Shimshi), 2021-11-19
  * **added USE SCHEMA <schema> and CREATE OR REPLACE <table> support; things that are allowed in Snowflake SQL (#1409)**
    
    Richard Kooijman, 2021-11-19
  * **Issue #420 Like Expression with Escape Expression (#1406)**
    
    manticore-projects, 2021-11-19
  * **fixes #1405 and some junit.jupiter stuff**
    
    Tobias Warneke, 2021-11-19
  * **#1401 add junit-jupiter-api (#1403)**
    
    gitmotte, 2021-11-19
  * **Support Postgres Dollar Quotes #1372 (#1395)**
    
    Olivier Cavadenti, 2021-11-19
  * **Add Delete / Update modifiers for MySQL #1254 (#1396)**
    
    Olivier Cavadenti, 2021-11-19
  * **Fixes #1381 (#1383)**
    
    manticore-projects, 2021-11-19
  * **Allows CASE ... ELSE ComplexExpression (#1388)**
    
    manticore-projects, 2021-11-02
  * **IN() with complex expressions (#1384)**
    
    manticore-projects, 2021-11-01
  * **Fixes #1385 and PR#1380 (#1386)**
    
    manticore-projects, 2021-10-22
  * **Fixes #1369 (#1370)**
    
    Ben Grabham, 2021-10-20
  * **Fixes #1371 (#1377)**
    
    manticore-projects, 2021-10-20
  * **LIMIT OFFSET with Expressions (#1378)**
    
    manticore-projects, 2021-10-20
  * **Oracle Multi Column Drop (#1379)**
    
    manticore-projects, 2021-10-20
  * **Support alias for UnPivot statement (see discussion #1374) (#1380)**
    
    fabriziodelfranco, 2021-10-20
  * **Issue1352 (#1353)**
    
    manticore-projects, 2021-10-09
  * **Enhance ALTER TABLE ... DROP CONSTRAINTS ... (#1351)**
    
    manticore-projects, 2021-10-08
  * **Function to use AllColumns or AllTableColumns Expression (#1350)**
    
    manticore-projects, 2021-10-08
  * **Postgres compliant ALTER TABLE ... RENAME TO ... (#1334)**
    
    manticore-projects, 2021-09-18
  * **Postgres compliant ALTER TABLE ... RENAME TO ... (#1334)**
    
    manticore-projects, 2021-09-18
  * **corrected readme to the new snapshot version**
    
    Tobias Warneke, 2021-09-08
  * **[maven-release-plugin] prepare for next development iteration**
    
    Tobias Warneke, 2021-09-08

Version jsqlparser-4.2
=============================================================


  * **[maven-release-plugin] prepare release jsqlparser-4.2**
    
    Tobias Warneke, 2021-09-08
  * **introducing test for issue #1328**
    
    Tobias Warneke, 2021-09-07
  * **included some distinct check**
    
    Tobias Warneke, 2021-09-07
  * **corrected a merge bug**
    
    Tobias Warneke, 2021-09-07
  * **Prepare4.2 (#1329)**
    
    manticore-projects, 2021-09-07
  * **CREATE TABLE AS (...) UNION (...) fails (#1309)**
    
    François Sécherre, 2021-09-07
  * **Fixes #1325 (#1327)**
    
    manticore-projects, 2021-09-06
  * **Implement Joins with multiple trailing ON Expressions (#1303)**
    
    manticore-projects, 2021-09-06
  * **Fix Gradle PMD and Checkstyle (#1318)**
    
    manticore-projects, 2021-09-01
  * **Fixes #1306 (#1311)**
    
    manticore-projects, 2021-08-28
  * **Update sets (#1317)**
    
    manticore-projects, 2021-08-27
  * **Special oracle tests (#1279)**
    
    manticore-projects, 2021-08-09
  * **Implements Hierarchical CONNECT_BY_ROOT Operator (#1282)**
    
    manticore-projects, 2021-08-09
  * **Implement Transact-SQL IF ELSE Statement Control Flows. (#1275)**
    
    manticore-projects, 2021-08-09
  * **Add some flexibility to the Alter Statement (#1293)**
    
    manticore-projects, 2021-08-02
  * **Implement Oracle's Alter System (#1288)**
    
    manticore-projects, 2021-08-02
  * **Implement Oracle Named Function Parameters Func( param1 => arg1, ...) (#1283)**
    
    manticore-projects, 2021-08-02
  * **Implement Gradle Buildsystem (#1271)**
    
    manticore-projects, 2021-08-02
  * **fixes #1272**
    
    Tobias Warneke, 2021-07-26
  * **Allowes JdbcParameter or JdbcNamedParameter for MySQL FullTextSearch (#1278)**
    
    manticore-projects, 2021-07-26
  * **Fixes #1267 Cast into RowConstructor (#1274)**
    
    manticore-projects, 2021-07-26
  * **Separate MySQL Special String Functions accepting Named Argument Separation as this could collide with ComplexExpressionList when InExpression is involved (#1285)**
    
    manticore-projects, 2021-07-26
  * **Implements Oracle RENAME oldTable TO newTable Statement (#1286)**
    
    manticore-projects, 2021-07-26
  * **Implement Oracle Purge Statement (#1287)**
    
    manticore-projects, 2021-07-26
  * **included jacoco to allow code coverage for netbeans**
    
    Tobias Warneke, 2021-07-18
  * **corrected a Lookahead problem**
    
    Tobias Warneke, 2021-07-16
  * **Json functions (#1263)**
    
    manticore-projects, 2021-07-16
  * **fixes #1255**
    
    Tobias Warneke, 2021-07-16
  * **Active JJDoc and let it create the Grammar BNF documentation (#1256)**
    
    manticore-projects, 2021-07-16
  * **Bump commons-io from 2.6 to 2.7 (#1265)**
    
    dependabot[bot], 2021-07-14
  * **Update README.md**
    
    Tobias, 2021-07-13
  * **Implement DB2 Special Register Date Time CURRENT DATE and CURRENT TIME (#1252)**
    
    manticore-projects, 2021-07-13
  * **Rename the PMD ruleset configuration file hoping for automatic synchronization with Codacy (#1251)**
    
    manticore-projects, 2021-07-13
  * **corrected .travis.yml**
    
    Tobias Warneke, 2021-07-05
  * **corrected .travis.yml**
    
    Tobias Warneke, 2021-07-05
  * **Update README.md**
    
    Tobias, 2021-07-05
  * **fixes #1250**
    
    Tobias Warneke, 2021-07-01
  * **[maven-release-plugin] prepare for next development iteration**
    
    Tobias Warneke, 2021-06-30

Version jsqlparser-4.1
=============================================================


  * **[maven-release-plugin] prepare release jsqlparser-4.1**
    
    Tobias Warneke, 2021-06-30
  * **fixes #1140**
    
    Tobias Warneke, 2021-06-30
  * **introduced #1248 halfway**
    
    Tobias Warneke, 2021-06-30
  * **Savepoint rollback (#1236)**
    
    manticore-projects, 2021-06-30
  * **Fixes Function Parameter List Brackets issue #1239 (#1240)**
    
    manticore-projects, 2021-06-30
  * **corrected javadoc problem**
    
    Tobias Warneke, 2021-06-27
  * **corrected some lookahead problem**
    
    Tobias Warneke, 2021-06-26
  * **RESET statement, SET PostgreSQL compatibility (#1104)**
    
    Роман Зотов, 2021-06-26
  * **corrected some lookahead problem**
    
    Tobias Warneke, 2021-06-26
  * **Implement Oracle Alter Session Statements (#1234)**
    
    manticore-projects, 2021-06-26
  * **fixes #1230**
    
    Tobias Warneke, 2021-06-26
  * **Support DELETE FROM T1 USING T2 WHERE ... (#1228)**
    
    francois-secherre, 2021-06-16
  * **Row access support (#1181)**
    
    Роман Зотов, 2021-06-16
  * **corrected lookahead problem of PR #1225**
    
    Tobias Warneke, 2021-06-14
  * **Delete queries without from, with a schema identifier fails (#1224)**
    
    François Sécherre, 2021-06-14
  * **Create temporary table t(c1, c2) as select ... (#1225)**
    
    francois-secherre, 2021-06-14
  * **Nested with items (#1221)**
    
    manticore-projects, 2021-06-10
  * **Implement GROUP BY () without columns (#1218)**
    
    manticore-projects, 2021-06-03
  * **TSQL Compliant NEXT VALUE FOR sequence_id (but keeping the spurious NEXTVAL FOR expression) (#1216)**
    
    manticore-projects, 2021-06-02
  * **Pmd clean up (#1215)**
    
    manticore-projects, 2021-06-02
  * **Add support for boolean 'XOR' operator (#1193)**
    
    Adaptive Recognition, 2021-06-02
  * **Update README.md**
    
    Tobias, 2021-05-31
  * **Implement WITH for DELETE, UPDATE and MERGE statements (#1217)**
    
    manticore-projects, 2021-05-31
  * **increases complex scanning range**
    
    Tobias Warneke, 2021-05-26
  * **Allow Complex Parsing of Functions (#1200)**
    
    manticore-projects, 2021-05-26
  * **Add support for AT TIME ZONE expressions (#1196)**
    
    Tomer Shay (Shimshi), 2021-05-25
  * **fixes #1211**
    
    Tobias Warneke, 2021-05-25
  * **fixes #1212**
    
    Tobias Warneke, 2021-05-25
  * **Fix Nested CASE WHEN performance, fixes issue #1162 (#1208)**
    
    manticore-projects, 2021-05-25
  * **Add support for casts in json expressions (#1189)**
    
    Tomer Shay (Shimshi), 2021-05-10
  * **fixes #1185**
    
    Tobias Warneke, 2021-05-04
  * **supporting/fixing unique inside sql function such as count eg - SELECT count(UNIQUE col2) FROM mytable (#1184)**
    
    RajaSudharsan Adhikesavan, 2021-05-01
  * **Oracle compliant ALTER TABLE ADD/MODIFY deparser (#1163)**
    
    manticore-projects, 2021-04-21
  * **Pmd (#1165)**
    
    manticore-projects, 2021-04-20
  * **function order by support (#1108)**
    
    Роман Зотов, 2021-04-20
  * **fixes #1159**
    
    Tobias Warneke, 2021-04-16
  * **added improvements of pr to readme**
    
    Tobias Warneke, 2021-04-16
  * **Assorted fixes to the Java CC Parser definition (#1153)**
    
    manticore-projects, 2021-04-16
  * **fixes #1138**
    
    Tobias Warneke, 2021-04-10
  * **fixes #1138**
    
    Tobias Warneke, 2021-04-10
  * **fixes #1137**
    
    Tobias Warneke, 2021-04-10
  * **fixes #1136**
    
    Tobias Warneke, 2021-04-10
  * **issue #1134 adressed**
    
    Tobias Warneke, 2021-03-20
  * **Add support for union_with_brackets_and_orderby (#1131)**
    
    Tomer Shay (Shimshi), 2021-03-14
  * **Add support for union without brackets and with limit (#1132)**
    
    Tomer Shay (Shimshi), 2021-03-14
  * **Add support for functions in an interval expression (#1099)**
    
    Tomer Shay (Shimshi), 2021-03-14
  * **subArray support arr[1:3] (#1109)**
    
    Роман Зотов, 2021-02-05
  * **bug fix (#769)**
    
    Kunal jha, 2021-02-05
  * **Array contructor support (#1105)**
    
    Роман Зотов, 2021-02-04
  * **Partial support construct tuple as simple expression (#1107)**
    
    Роман Зотов, 2021-01-31
  * **support create table parameters without columns, parameter values any names (#1106)**
    
    Роман Зотов, 2021-01-31
  * **fixes #995**
    
    Tobias Warneke, 2021-01-13
  * **fixes #1100**
    
    Tobias Warneke, 2021-01-13
  * **next correction of parenthesis around unions**
    
    Tobias Warneke, 2021-01-11
  * **fixes #992**
    
    Tobias Warneke, 2021-01-07
  * **corrected patch for case as table name**
    
    Tobias Warneke, 2021-01-07
  * **Added support for the Case keyword in table names (#1093)**
    
    Tomer Shay (Shimshi), 2021-01-07
  * **corrected some javadoc parameter**
    
    Tobias Warneke, 2021-01-03
  * **added missing pivot test files**
    
    Tobias Warneke, 2021-01-03
  * **fixes #282 - first refactoring to allow with clause as a start in insert and update**
    
    Tobias Warneke, 2021-01-02
  * **fixes #282 - first refactoring to allow with clause as a start in insert and update**
    
    Tobias Warneke, 2021-01-02
  * **Update README.md**
    
    Tobias, 2021-01-02
  * **fixes #887**
    
    Tobias Warneke, 2021-01-02
  * **fixes #1091 - added H2 casewhen function with conditional parameters**
    
    Tobias Warneke, 2021-01-01
  * **fixes #1091 - added H2 casewhen function with conditional parameters**
    
    Tobias Warneke, 2021-01-01
  * **[maven-release-plugin] prepare for next development iteration**
    
    Tobias Warneke, 2021-01-01

