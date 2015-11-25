# JSqlParser

[![Build Status](https://travis-ci.org/JSQLParser/JSqlParser.svg?branch=master)](https://travis-ci.org/JSQLParser/JSqlParser)   [![Coverage Status](https://coveralls.io/repos/JSQLParser/JSqlParser/badge.svg?branch=master)](https://coveralls.io/r/JSQLParser/JSqlParser?branch=master)

[![Flattr](http://api.flattr.com/button/flattr-badge-large.png)](https://flattr.com/submit/auto?user_id=wumpz&url=http%3A%2F%2Fgithub.com%2FJSQLParser%2FJSqlParser)  [![PayPal donate button](http://img.shields.io/paypal/donate.png?color=yellow)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=64CCN9JJANZXA "Help this JSqlParser version using Paypal")  

Look here for more information and examples: https://github.com/JSQLParser/JSqlParser/wiki.

## License

JSqlParser is dual licensed under **LGPL V2.1** and **Apache Software License, Version 2.0**.


## News

* Version **0.9.4** released.
* Please test the actual SNAPSHOT, if there are problems using the extended identifier token.
* Version **0.9.3** released.

More news can be found here: https://github.com/JSQLParser/JSqlParser/wiki/News.

## JSqlParser

JSqlParser is a SQL statement parser. It translates SQLs in a traversable hierarchy of Java classes. JSqlParser is not limited to one database but provides support for a lot of specials of Oracle, SqlServer, MySQL, PostgreSQL ... To name some, it has support for Oracles join syntax using (+), PostgreSQLs cast syntax using ::, relational operators like != and so on.

## Support
If you need help using JSqlParser feel free to file an issue or contact me.

## Contributions
To help JSqlParsers development you are encouraged to provide 
* feedback
* bugreports
* pull requests for new features
* improvement requests
* fund new features

Also I would like to know about needed examples or documentation stuff.

## Extensions in the latest SNAPSHOT version 0.9.5

* support for **INSERT LOW_PRIORITY INTO**
* support for **ORDER BY** and **LIMIT** in **UPDATE** and **DELETE** statements

~~~
UPDATE tablename SET ... ORDER BY col;
UPDATE tablename SET ... ORDER BY col LIMIT 10;
UPDATE table1 A SET ... LIMIT 10;
DELETE FROM tablename LIMIT 5;
DELETE FROM tablename ORDER BY col;
DELETE FROM tablename ORDER BY col LIMIT 10;
~~~

* support for **INSERT ... ON DUPLICATE KEY UPDATE**
* improved support for **ALTER TABLE** statements
* first Oracle hint support for **SELECT** statements 
* first **ALTER TABLE FOREIGN KEY** support
* first **MERGE** support
* first version of escaped single quotes support

~~~
select '\'' 
~~~

## Extensions of JSqlParser releases

* [Release Notes](https://github.com/JSQLParser/JSqlParser/releases)
* Modifications before GitHubs release tagging are listed in the [Older Releases](https://github.com/JSQLParser/JSqlParser/wiki/Older-Releases) page.



## BUILDING from the sources

As the project is a Maven project, building is rather simple by running:

	mvn package

This will produce the jsqlparser-VERSION.jar file in the target/ directory.

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
	<version>0.9.4</version>
</dependency>
```

