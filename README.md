# JSqlParser Organisation

## News

Version **0.8.8** released.

Due to incompatibilities using maven release with git v1.8.5 I skipped version 0.8.7. So the actual released version is **0.8.8**.

Recent changes in **0.8.7-SNAPSHOT** for some alias support changes introducing some API changes.

Version **0.8.6** released.


The first release version **0.8.5** is available at maven central (**http://repo1.maven.org/maven2/**) and at repository 
**https://oss.sonatype.org/content/groups/public/**.


The maven repository settings have been changed. Version **0.8.5-SNAPSHOT** will be the
first snapshot at sonatypes open source repository location. The following releases will
go there as well and published to maven central. To avoid problems with the original 
projects artifacts the *groupid* was changed to **com.github.jsqlparser**. You will find
configuration details below.

## JSqlParser

JSqlParser parses an SQL statement and translate it into a hierarchy of Java classes. 
The generated hierarchy can be navigated using the Visitor Pattern.

## Extensions Version 0.8.8

* Startet a simple utility class **SelectUtils** to collect basic **select** modification tools.
    * addExpression adds a new expression to the select list.
    * addJoin adds a new join to the select.
* Added support for optional " AS " for aliases. 

```sql
SELECT * FROM mytable myalias
```

* Added first support for ALTER TABLE statements

```sql
ALTER TABLE mytable ADD COLUMN mycolumn varchar (255)
```

* Added support for PostgreSQL regular expression match operators.

```sql
SELECT a, b FROM foo WHERE a ~ '[help].*';
SELECT a, b FROM foo WHERE a ~* '[help].*';
SELECT a, b FROM foo WHERE a !~ '[help].*';
SELECT a, b FROM foo WHERE a !~* '[help].*';
```

## Extensions Version 0.8.6

* Added first support for Oracle hierarchical queries

```sql
SELECT * FROM employees CONNECT BY employee_id = manager_id ORDER BY last_name;
SELECT * FROM employees START WITH employee_id = 100 CONNECT BY PRIOR employee_id = manager_id ORDER SIBLINGS BY last_name;
```

## Extensions Version 0.8.5

* Added support for mysql truncate function
* Changed repository location and groupid of JSqlParsers artifact.

```xml
<dependency>
	<groupId>com.github.jsqlparser</groupId>
	<artifactId>jsqlparser</artifactId>
	<version>0.8.5</version>
</dependency>
```

* Added support for postgresql type **character varying**.

## Extensions Version 0.8.4

* Added support for named JDBC parameters

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
	<version>0.8.6</version>
</dependency>
```

## Original project

This is a fork of the jsqlparser originally developed by ultimoamore.

Original project websites:

* http://jsqlparser.sourceforge.net
* http://sourceforge.net/projects/jsqlparser/
