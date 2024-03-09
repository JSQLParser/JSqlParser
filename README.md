# [JSqlParser 4.9 Website](https://jsqlparser.github.io/JSqlParser) <img src="src/site/sphinx/_images/logo-no-background.svg" alt="drawing" width="200" align="right"/>

![Build Status](https://github.com/JSQLParser/JSqlParser/actions/workflows/maven.yml/badge.svg)
[![Build Status (Legacy)](https://travis-ci.com/JSQLParser/JSqlParser.svg?branch=master)](https://travis-ci.com/JSQLParser/JSqlParser)   [![Coverage Status](https://coveralls.io/repos/JSQLParser/JSqlParser/badge.svg?branch=master)](https://coveralls.io/r/JSQLParser/JSqlParser?branch=master) [![Codacy Badge](https://app.codacy.com/project/badge/Grade/6f9a2d7eb98f45969749e101322634a1)](https://www.codacy.com/gh/JSQLParser/JSqlParser/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=JSQLParser/JSqlParser&amp;utm_campaign=Badge_Grade)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.jsqlparser/jsqlparser/badge.svg)](http://maven-badges.herokuapp.com/maven-central/com.github.jsqlparser/jsqlparser) [![Javadocs](https://www.javadoc.io/badge/com.github.jsqlparser/jsqlparser.svg)](https://www.javadoc.io/doc/com.github.jsqlparser/jsqlparser)
[![Gitter](https://badges.gitter.im/JSQLParser/JSqlParser.svg)](https://gitter.im/JSQLParser/JSqlParser?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

## Summary

Please visit the [WebSite](https://jsqlparser.github.io/JSqlParser). **JSqlParser** is a RDBMS agnostic SQL statement parser. It translates SQL statements into a traversable hierarchy of Java classes (see [Samples](https://jsqlparser.github.io/JSqlParser/usage.html#parse-a-sql-statements)):

```sql
SELECT 1 FROM dual WHERE a = b
```

```text
SQL Text
 └─Statements: statement.select.PlainSelect
    ├─selectItems: statement.select.SelectItem
    │  └─LongValue: 1
    ├─Table: dual
    └─where: expression.operators.relational.EqualsTo
       ├─Column: a
       └─Column: b
```

```java
String sqlStr = "select 1 from dual where a=b";

PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sqlStr);

SelectItem selectItem =
        select.getSelectItems().get(0);
Assertions.assertEquals(
        new LongValue(1)
        , selectItem.getExpression());

Table table = (Table) select.getFromItem();
Assertions.assertEquals("dual", table.getName());

EqualsTo equalsTo = (EqualsTo) select.getWhere();
Column a = (Column) equalsTo.getLeftExpression();
Column b = (Column) equalsTo.getRightExpression();
Assertions.assertEquals("a", a.getColumnName());
Assertions.assertEquals("b", b.getColumnName());
}
```

JSQLParser-4.9 is the last JDK8 compatible version and any future development will depend on JDK11.

## [Supported Grammar and Syntax](https://jsqlparser.github.io/JSqlParser/syntax.html)

**JSqlParser** aims to support the SQL standard as well as all major RDBMS. Any missing syntax or features can be added on demand.

| RDBMS                                                                                                           | Statements                                                                                                                              |
|-----------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| Oracle<br>MS SQL Server and Sybase<br>Postgres<br>MySQL and MariaDB<br>DB2<br>H2 and HSQLDB and Derby<br>SQLite | `SELECT`<br>`INSERT`, `UPDATE`, `UPSERT`, `MERGE`<br>`DELETE`, `TRUNCATE TABLE`<br>`CREATE ...`, `ALTER ....`, `DROP ...`<br>`WITH ...` |


**JSqlParser** can also be used to create SQL Statements from Java Code with a fluent API (see [Samples](https://jsqlparser.github.io/JSqlParser/usage.html#build-a-sql-statements)).

## Alternatives to JSqlParser?
[**General SQL Parser**](http://www.sqlparser.com/features/introduce.php?utm_source=github-jsqlparser&utm_medium=text-general) looks pretty good, with extended SQL syntax (like PL/SQL and T-SQL) and java + .NET APIs. The tool is commercial (license available online), with a free download option.

Alternatively the dual-licensed [JOOQ](https://www.jooq.org/doc/latest/manual/sql-building/sql-parser/) provides a hand-written Parser supporting a lot of RDBMS, translation between dialects, SQL transformation, can be used as a JDBC proxy for translation and transformation purposes.

## [Documentation](https://jsqlparser.github.io/JSqlParser)
  1. [Samples](https://jsqlparser.github.io/JSqlParser/usage.html#parse-a-sql-statements)
  2. [Build Instructions](https://jsqlparser.github.io/JSqlParser/usage.html) and [Maven Artifact](https://jsqlparser.github.io/JSqlParser/usage.html#build-dependencies)
  3. [Contribution](https://jsqlparser.github.io/JSqlParser/contribution.html)
  4. [Change Log](https://jsqlparser.github.io/JSqlParser/changelog.html#latest-changes-since-jsqlparser-version)
  5. [Issues](https://github.com/JSQLParser/JSqlParser/issues)

## License

**JSqlParser** is dual licensed under **LGPL V2.1** or **Apache Software License, Version 2.0**.
