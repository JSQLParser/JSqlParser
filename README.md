# JSqlParser

[![Build Status](https://travis-ci.org/JSQLParser/JSqlParser.svg?branch=master)](https://travis-ci.org/JSQLParser/JSqlParser)   [![Coverage Status](https://coveralls.io/repos/JSQLParser/JSqlParser/badge.svg?branch=master)](https://coveralls.io/r/JSQLParser/JSqlParser?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/00b2d91995764ae4805b55627aca8d39)](https://www.codacy.com/app/wumpz/JSqlParser?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=JSQLParser/JSqlParser&amp;utm_campaign=Badge_Grade)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.jsqlparser/jsqlparser/badge.svg)](http://maven-badges.herokuapp.com/maven-central/com.github.jsqlparser/jsqlparser)
[![Javadocs](https://www.javadoc.io/badge/com.github.jsqlparser/jsqlparser.svg)](https://www.javadoc.io/doc/com.github.jsqlparser/jsqlparser)

[![PayPal donate button](http://img.shields.io/paypal/donate.png?color=blue)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=64CCN9JJANZXA "Help this JSqlParser version using Paypal")  

[![Gitter](https://badges.gitter.im/JSQLParser/JSqlParser.svg)](https://gitter.im/JSQLParser/JSqlParser?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Code Quality: Java](https://img.shields.io/lgtm/grade/java/g/JSQLParser/JSqlParser.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/JSQLParser/JSqlParser/context:java)
[![Total Alerts](https://img.shields.io/lgtm/alerts/g/JSQLParser/JSqlParser.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/JSQLParser/JSqlParser/alerts)

Look here for more information and examples: https://github.com/JSQLParser/JSqlParser/wiki.

## License

JSqlParser is dual licensed under **LGPL V2.1** and **Apache Software License, Version 2.0**.


## News
* Changed behaviour of dotted multipart names for user variables, tables and columns to accept e.g. ORM class names. To achieve this some behaviour of name parsing had to be changed. Before this the parser would fail missing databasenames for SqlServer queries (server..schema.table). But this is allowed for the schema (server.database..table). Now the parser accepts missing inner names per se to avoid some very complicated parsing rules.
* Released version **1.2** of JSqlParser
* breaking **API** change: merge of *within group* and *over* (window expressions)
* Released version **1.1** of JSqlParser. 
* JSqlParser has now a build in checkstyle configuration to introduce source code conventions.
* Released first major version **1.0** of JSqlParser. 

More news can be found here: https://github.com/JSQLParser/JSqlParser/wiki/News.

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
* fund new features
* a little donation

**Please write in English, since it's the language most of the dev team knows.**

Also I would like to know about needed examples or documentation stuff.

## Extensions in the latest SNAPSHOT version 1.3

* support for **with - selects** statements in **create view** definitions
* support for block statements (begin ... end)
* support for additional raw string and byte prefixes (issue #659)
* support for special oracle type syntax **varchar2(255 BYTE)** (issue #273) 
* introduced dotted multipart names for uservariables (issue #608)
* changed behaviour of dotted multipart names for tables and columns to accept ORM class names (issue #163)
** the parser allows now empty inner names, to still accept missing schema names for SQLServer (db..col)
** methods like **getDatabase** will still work but have no sense using it for classnames
* named parameter for **OFFSET** (issue #612)
* corrected ISNULL regression (issue #610)
* refactored statement test classes to the class corresponding packages
* allowed nested postgresql casts (e.g. col::bigint::int)


## Extensions of JSqlParser releases

* [Release Notes](https://github.com/JSQLParser/JSqlParser/releases)
* Modifications before GitHub's release tagging are listed in the [Older Releases](https://github.com/JSQLParser/JSqlParser/wiki/Older-Releases) page.


## Building from the sources

As the project is a Maven project, building is rather simple by running:

	mvn package
    
The project requires the following to build:
- Maven 
- JDK 1.7 or later. The jar will target JDK 1.6, but the version of the maven-compiler-plugin that JsqlParser uses requires JDK 1.7+

This will produce the jsqlparser-VERSION.jar file in the target/ directory.

**To build this project without using Maven, one has to build the parser by JavaCC using the CLI options it provides.**

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

JSQLParser is deployed at sonatypes open source maven repository. 
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
This repositories releases will be synched to maven central. Snapshots remain at sonatype.

And this is the dependency declaration in your pom:
```xml
<dependency>
	<groupId>com.github.jsqlparser</groupId>
	<artifactId>jsqlparser</artifactId>
	<version>1.2</version>
</dependency>
```

