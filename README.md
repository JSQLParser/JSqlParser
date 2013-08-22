# JSqlParser Organisation

## JSqlParser

JSqlParser parses an SQL statement and translate it into a hierarchy of Java classes. 
The generated hierarchy can be navigated using the Visitor Pattern.

## Extensions Version 0.8.4

* Added support named JDBC parameters

```sql
SELECT * FROM mytable WHERE b = :param
```
* Added support for pivot expressions 
* Added support for boolean functions in where statements

```sql
select * from my_table where bool_func(col)
```
* Added support for Oracles old join syntax for more compare operations

```sql
select * from taba, tabb where taba.a<tabb.a(+)
```
* Added support for foreign keys in create table statements

```sql
create table testTable1 (a varchar(10), b varchar(20), foreign key a references testTable2(a))
create table testTable1 (a varchar(10), b varchar(20), constraint fkIdx foreign key a references testTable2(a))
```
* Added support for simple intervals

```sql
select 5 - INTERVAL '45 MINUTE' from mytable
```
* Added support for multi values IN expression

```sql
select * from mytable where (a,b,c) in (select a,b,c from mytable2)
```

## Extensions Version 0.8.3

* Added support for cross join 
* Allowed complex expressions in extract from
* Corrected cast expression to make type parameters usable (e.g. cast(col1 as varchar(255))
* Added support for column comma list in partition by statements
* Added support for columns names in create view statements

```sql
create view testView (col1,col2) as select a, b from table
```
* Added support for column cast using ::
* Added support for from clause in update statements

```sql
update tab1 set c=5 from tab1 inner join tab2 on tab1.col1=tab2.col2
``` 
* Corrected TableNamesFinder to work with update statements additions.
* Added support for simple create materialized view statements without additional parameters.

```sql
create materialized view testView as select a, b from table
```
* Added support for simple create index statements

```sql
create index myindex on mytab (mycol, mycol2)
```

## Extensions til Version 0.8.2

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
select a from taba except select b from tabb
select a from taba minus select b from tabb
```
* Added support for `extract(year from datetime-expr)`
* Start implementation of analytical expressions
* merged support for CREATE VIEW 
* Added lateral subquery support
* Added support for multi values insert statement

```sql
insert taba (col1,col2) values (1,2), (2,5), (3,20)
```
* Added support for multi values in select statement

```sql
SELECT col FROM (VALUES 1,2) AS MY_TABLE(col)
```
* Added extended support for analytic expressions (empty over clause, parameter within aggregat function)

```sql
SELECT sum(a) over () FROM taba
```

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
	<version>0.8.4-SNAPSHOT</version>
</dependency>
```

## Original project

This is a fork of the jsqlparser originally developed by ultimoamore.

Original project websites:

* http://jsqlparser.sourceforge.net
* http://sourceforge.net/projects/jsqlparser/
