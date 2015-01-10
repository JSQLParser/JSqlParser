# JSqlParser

Look here for more information and examples: https://github.com/JSQLParser/JSqlParser/wiki.

## News

Version **0.9.1** released.

Version **0.9** released.

More news can be found here: https://github.com/JSQLParser/JSqlParser/wiki/News.

## JSqlParser

JSqlParser is a SQL statement parser. It translates SQLs in a traversable hierarchy of Java classes. JSqlParser is not limited to one database but provides support for a lot of specials of Oracle, SqlServer, MySQL, PostgreSQL ... To name some, it has support for Oracles join syntax using (+), PostgreSQLs cast syntax using ::, relational operators like != and so on.

## Contributions
To help JSqlParsers development you are encouraged to provide 
* feedback
* bugreports
* pull requests for new features
* improvement requests

Also I would like to know about needed examples or documentation stuff. 

## Extensions in the latest SNAPSHOT version 0.9.2

* support for inner with statements 

```sql
SELECT * FROM (WITH actor AS (SELECT 'a' aid FROM DUAL) SELECT aid FROM actor)
```

* support for Oracle collections 

```sql
SELECT * FROM mytable WHERE COL1 = lines(idx).field1
```

* support for PostgreSQL ANY - array syntax
* introduced additional parameters for create table statements (deparser extended)
* first support for *FOR UPDATE*

```sql
SELECT * FROM user_table FOR UPDATE
```

```sql
UPDATE mytable SET (col) = (SELECT a FROM mytable2)
```

* partially support SQL Server 2012 and Oracle 12c OFFSET ... FETCH ... 

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
	<version>0.9</version>
</dependency>
```

