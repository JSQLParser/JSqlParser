# JSqlParser Organisation created and started

Some issues of wumpz repository JSQLParser are lost. Sorry about that. That repository is replaced by 
this new JSqlParser - place.


## JSqlParser

This is a fork of the jsqlparser originally developed by ultimoamore and wumpz, 
and this fork is aim at supports for a dialect of sql - mysql.

Original project websites:

* https://github.com/wumpz/JSqlParser

* http://jsqlparser.sourceforge.net
* http://sourceforge.net/projects/jsqlparser/

# 

## BUILDING

> As the project is a Maven project, building is rather simple by running:
> 
> 	mvn package
> 
> This will produce the jsqlparser-VERSION.jar file in the target/ directory.

Quote those for I' m not familiar with maven.


## Maven Repository

> At the moment I created a github maven repository. Starting from now I will deploy there. 
> To use it this is the repository configuration:
> 
> ```xml
> <repositories>
>      <repository>
>          <id>jsqlparser-snapshots</id>
>          <snapshots>
>              <enabled>true</enabled>
>          </snapshots>
>          <url>https://raw.github.com/wumpz/maven_repo/master/snapshots</url>
>      </repository>
> </repositories>
> ```
> 
> And this is the dependency declaration in your pom:
> ```xml
> <dependency>
> 	<groupId>net.sf.jsqlparser</groupId>
> 	<artifactId>jsqlparser</artifactId>
> 	<version>0.8.1-SNAPSHOT</version>
> </dependency>
> ```
