# JSqlParser

This is a fork of the jsqlparser originally developed by ultimoamore.

Original project websites:

* http://jsqlparser.sourceforge.net
* http://sourceforge.net/projects/jsqlparser/

## Extensions

* Changed project tests to junit 4
* Changed project layout to maven project
* Added regexp (REGEXP) operator
* Added support for SELECT without FROM  (e.g. "SELECT 1+2")
* Moved parser from using StringBuffer to using StringBuilder
* Added support for CAST expression

```sql
select cast(col as varchar) from table
```
* Added support for modulo (a % b)
* Added support for brackets quotation
* Added support for NOT expr IS (expr IS NOT was already supported)
* Added support for Oracles (+) Join Syntax

```sql
select * from taba, tabb where taba.a=tabb.a(+)
```
* Added alias visitor to add aliases to selections
* Added connect visitor
* TableNamesFinder moved from tests to main source
* Added proper support for sets (union, intersect)

```sql
select a from taba union select b from tabb
select a from taba intersect select b from tabb
```
* Added support for `extract(year from datetime-expr)`
* Start implementation of analytical expressions
* merged support for CREATE VIEW 

## BUILDING

As the project is a Maven project, building is rather simple by running:

	mvn package

This will produce the jsqlparser-VERSION.jar file in the target/ directory.

## Maven Repository

At the moment I created a github maven repository. Starting from now I will deploy there. 
To use it this is the repository configuration:

```xml
<repositories>
     <repository>
         <id>jsqlparser-snapshots</id>
         <snapshots>
             <enabled>true</enabled>
         </snapshots>
         <url>https://raw.github.com/wumpz/maven_repo/master/snapshots</url>
     </repository>
</repositories>
```

And this is the dependency declaration in your pom:
```xml
<dependency>
	<groupId>net.sf.jsqlparser</groupId>
	<artifactId>jsqlparser</artifactId>
	<version>0.8.1-SNAPSHOT</version>
</dependency>
```
