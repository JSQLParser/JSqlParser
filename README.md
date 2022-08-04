# JSqlParser

![Build Status](https://github.com/JSQLParser/JSqlParser/actions/workflows/maven.yml/badge.svg)

[![Build Status (Legacy)](https://travis-ci.com/JSQLParser/JSqlParser.svg?branch=master)](https://travis-ci.com/JSQLParser/JSqlParser)   [![Coverage Status](https://coveralls.io/repos/JSQLParser/JSqlParser/badge.svg?branch=master)](https://coveralls.io/r/JSQLParser/JSqlParser?branch=master)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/6f9a2d7eb98f45969749e101322634a1)](https://www.codacy.com/gh/JSQLParser/JSqlParser/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=JSQLParser/JSqlParser&amp;utm_campaign=Badge_Grade)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.jsqlparser/jsqlparser/badge.svg)](http://maven-badges.herokuapp.com/maven-central/com.github.jsqlparser/jsqlparser)
[![Javadocs](https://www.javadoc.io/badge/com.github.jsqlparser/jsqlparser.svg)](https://www.javadoc.io/doc/com.github.jsqlparser/jsqlparser)

[![Gitter](https://badges.gitter.im/JSQLParser/JSqlParser.svg)](https://gitter.im/JSQLParser/JSqlParser?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Code Quality: Java](https://img.shields.io/lgtm/grade/java/g/JSQLParser/JSqlParser.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/JSQLParser/JSqlParser/context:java)
[![Total Alerts](https://img.shields.io/lgtm/alerts/g/JSQLParser/JSqlParser.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/JSQLParser/JSqlParser/alerts)

Look here for more information and examples: https://github.com/JSQLParser/JSqlParser/wiki.
 
## License

JSqlParser is dual licensed under **LGPL V2.1** or **Apache Software License, Version 2.0**.

## Discussion

Please provide feedback on:

* API changes: extend visitor with return values (https://github.com/JSQLParser/JSqlParser/issues/901)

## News
* Released version **4.4** of JSqlParser
* The array parsing is the default behaviour. Square bracket quotation has to be enabled using 
  a parser flag (**CCJSqlParser.withSquareBracketQuotation**).
* due to an API change the version will be 3.0
* JSqlParser uses now Java 8 at the minimum

More news can be found here: https://github.com/JSQLParser/JSqlParser/wiki/News.

## Alternatives to JSqlParser?
[**General SQL Parser**](http://www.sqlparser.com/features/introduce.php?utm_source=github-jsqlparser&utm_medium=text-general) looks pretty good, with extended SQL syntax (like PL/SQL and T-SQL) and java + .NET APIs. The tool is commercial (license available online), with a free download option.

## JSqlParser

JSqlParser is a SQL statement parser. It translates SQLs in a traversable hierarchy of Java classes. JSqlParser is not limited to one database but provides support for a lot of specials of Oracle, SqlServer, MySQL, PostgreSQL ... To name some, it has support for Oracles join syntax using (+), PostgreSQLs cast syntax using ::, relational operators like != and so on.

## Support
If you need help using JSqlParser feel free to file an issue or contact me.

## Contributions
To help JSqlParser's development you are encouraged to provide 
* feedback
* bugreports
* pull requests for new features
* improvement requests
* fund new features or sponsor JSqlParser ([**Sponsor**](https://www.paypal.me/wumpz))

**Please write in English, since it's the language most of the dev team knows.**

Any requests for examples or any particular documentation will be most welcome.

## Extensions in the latest SNAPSHOT version 4.6

* support for named windows in window expressions: `SELECT sum(c) OVER winName FROM mytable WINDOW winName AS (PARTITION BY pcol)`

Additionally, we have fixed many errors and improved the code quality and the test coverage.

## Extensions of JSqlParser releases

* [Release Notes](https://github.com/JSQLParser/JSqlParser/releases)
* Modifications before GitHub's release tagging are listed in the [Older Releases](https://github.com/JSQLParser/JSqlParser/wiki/Older-Releases) page.

## Building from the sources

As the project is a Maven project, building is rather simple by running:
```shell
mvn package
```

Since 4.2, alternatively Gradle can be used
```shell
gradle build
```
    
The project requires the following to build:
- Maven (or Gradle)
- JDK 8 or later. The JAR will target JDK 8, but the version of the maven-compiler-plugin that JSqlParser uses requires JDK 8+

This will produce the jsqlparser-VERSION.jar file in the `target/` directory (`build/libs/jsqlparser-VERSION.jar` in case of Gradle).

**To build this project without using Maven or Gradle, one has to build the parser by JavaCC using the CLI options it provides.**

## Debugging through problems

Refer to the [Visualize Parsing](https://github.com/JSQLParser/JSqlParser/wiki/Examples-of-SQL-parsing#visualize-parsing) section to learn how to run the parser in debug mode.

## Source Code conventions

Recently a checkstyle process was integrated into the build process. JSqlParser follows the sun java format convention. There are no TABs allowed. Use spaces.

```java
public void setUsingSelect(SubSelect usingSelect) {
    this.usingSelect = usingSelect;
    if (this.usingSelect != null) {
        this.usingSelect.setUseBrackets(false);
    }
}
```

This is a valid piece of source code:
* blocks without braces are not allowed
* after control statements (if, while, for) a whitespace is expected
* the opening brace should be in the same line as the control statement

## Maven Repository

JSQLParser is deployed at Sonatype open source maven repository. 
Starting from now I will deploy there. The first snapshot version there will be 0.8.5-SNAPSHOT.
To use it this is the repository configuration:

```xml
<repositories>
     <repository>
         <id>jsqlparser-snapshots</id>
         <snapshots>
             <enabled>true</enabled>
         </snapshots>
         <url>https://oss.sonatype.org/content/groups/public/</url>
     </repository>
</repositories>
```
These repository releases will be synchronised to Maven Central. Snapshots remain at Sonatype.

And this is the dependency declaration in your pom:
```xml
<dependency>
	<groupId>com.github.jsqlparser</groupId>
	<artifactId>jsqlparser</artifactId>
	<version>4.4</version>
</dependency>
```

