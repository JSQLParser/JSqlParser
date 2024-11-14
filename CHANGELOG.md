# JSqlParser changelog

Changelog of JSqlParser.

## jsqlparser-5.0 (2024-06-30)

### Breaking changes

-  Visitors return Objects and accept parameters ([5bd28](https://github.com/JSQLParser/JSqlParser/commit/5bd28c8b309df6c) Andreas Reichel)  
-  Visitors return Objects ([131a9](https://github.com/JSQLParser/JSqlParser/commit/131a988ccea2d91) Andreas Reichel)  
-  Visitors return Objects ([c2328](https://github.com/JSQLParser/JSqlParser/commit/c2328120e7a79ff) Andreas Reichel)  
-  Visitors return Objects ([ec497](https://github.com/JSQLParser/JSqlParser/commit/ec49762708e920a) Andreas Reichel)  
-  Visitors return Objects ([681ca](https://github.com/JSQLParser/JSqlParser/commit/681cac933d83516) Andreas Reichel)  

### Features

-  provide compatibility methods ([3f995](https://github.com/JSQLParser/JSqlParser/commit/3f99548b99bbfe3) Andreas Reichel)  
-  apply the new parametrized Visitor patterns to all entities and provide default implementations ([e1692](https://github.com/JSQLParser/JSqlParser/commit/e1692990c543ed1) Andreas Reichel)  
-  syntax sugar ([2fce4](https://github.com/JSQLParser/JSqlParser/commit/2fce4c009b77d85) Andreas Reichel)  
-  Visitors return Objects and accept parameters ([5bd28](https://github.com/JSQLParser/JSqlParser/commit/5bd28c8b309df6c) Andreas Reichel)  
-  Visitors return Objects ([131a9](https://github.com/JSQLParser/JSqlParser/commit/131a988ccea2d91) Andreas Reichel)  
-  Visitors return Objects ([c2328](https://github.com/JSQLParser/JSqlParser/commit/c2328120e7a79ff) Andreas Reichel)  
-  Visitors return Objects ([ec497](https://github.com/JSQLParser/JSqlParser/commit/ec49762708e920a) Andreas Reichel)  
-  Visitors return Objects ([681ca](https://github.com/JSQLParser/JSqlParser/commit/681cac933d83516) Andreas Reichel)  
-  Allow OUTER keyword as function parameter name (#2021) ([fc90c](https://github.com/JSQLParser/JSqlParser/commit/fc90c0b5e566533) Chris Crabtree)  
-  BigQuery `SELECT AS STRUCT ...` and `SELECT AS VALUE ...` ([5c360](https://github.com/JSQLParser/JSqlParser/commit/5c360a2fc95c261) Andreas Reichel)  
-  add syntax sugar ([2ace7](https://github.com/JSQLParser/JSqlParser/commit/2ace74d1047e87d) Andreas Reichel)  
-  `AllColumns`, DuckDB uses `EXCLUDE` instead of `EXCEPT` ([1ad42](https://github.com/JSQLParser/JSqlParser/commit/1ad4234280f7a70) Andreas Reichel)  
-  syntax sugar ([ae1ef](https://github.com/JSQLParser/JSqlParser/commit/ae1eff9f7434c08) Andreas Reichel)  
-  syntax sugar ([81846](https://github.com/JSQLParser/JSqlParser/commit/818464c93ae665a) Andreas Reichel)  
-  syntax sugar ([2cb3e](https://github.com/JSQLParser/JSqlParser/commit/2cb3e589b60e192) Andreas Reichel)  
-  syntax sugar ([b2eed](https://github.com/JSQLParser/JSqlParser/commit/b2eed1e910c97de) Andreas Reichel)  
-  Databricks IGNORE/RESPECT NULLS ([e9c9a](https://github.com/JSQLParser/JSqlParser/commit/e9c9a173a660bbe) Andreas Reichel)  
-  Databricks IGNORE/RESPECT NULLS ([544b1](https://github.com/JSQLParser/JSqlParser/commit/544b1683789f20b) Andreas Reichel)  
-  Capture expression name part delimiters (#2001) ([0368b](https://github.com/JSQLParser/JSqlParser/commit/0368b9ebad76742) Chris Crabtree)  
-  syntax sugar ([ca5c5](https://github.com/JSQLParser/JSqlParser/commit/ca5c553efde37eb) Andreas Reichel)  
-  translate HEX to Unicode String and ByteArray String ([df519](https://github.com/JSQLParser/JSqlParser/commit/df519333ff34740) Andreas Reichel)  
-  `StructType` syntax sugar ([6e9bf](https://github.com/JSQLParser/JSqlParser/commit/6e9bf42b0b2d783) Andreas Reichel)  
-  `Values` implement `FromItem` ([e426c](https://github.com/JSQLParser/JSqlParser/commit/e426c5a67c505b5) Andreas Reichel)  
-  add `ParenthesedSelect` delegate ([66d05](https://github.com/JSQLParser/JSqlParser/commit/66d05a2bb7c41f3) Andreas Reichel)  
-  add `ParenthesedSelect` delegate ([f1699](https://github.com/JSQLParser/JSqlParser/commit/f16999393589702) Andreas Reichel)  
-  Simplify traversing the AST bottom to top ([bddc4](https://github.com/JSQLParser/JSqlParser/commit/bddc41cddf5b5bf) Andreas Reichel)  
-  AST Node access for `FromItem` ([c1edf](https://github.com/JSQLParser/JSqlParser/commit/c1edf0f8f21bd52) Andreas Reichel)  
-  RedShift specific Window function IGNORE | RESPECT NULLS ([321c8](https://github.com/JSQLParser/JSqlParser/commit/321c88098a75791) Andreas Reichel)  
-  RedShift allows `TOP` before `DISTINCT`, see https://docs.aws.amazon.com/redshift/latest/dg/r_SELECT_list.html ([13e61](https://github.com/JSQLParser/JSqlParser/commit/13e61a726a87c2f) Andreas Reichel)  
-  Redshift `APPROXIMATE` Aggregate functions ([e4ece](https://github.com/JSQLParser/JSqlParser/commit/e4ece0c3ecd7ce3) Andreas Reichel)  
-  add `CCJSqlParserUtil.sanitizeSingleSql(String sqlStr)` to help MyBatikPlus users to clean their statements ([1606e](https://github.com/JSQLParser/JSqlParser/commit/1606e5f0492a485) Andreas Reichel)  
-  return any `UnsupportedStatement` content ([063d2](https://github.com/JSQLParser/JSqlParser/commit/063d2442d82f920) Andreas Reichel)  
-  re-enable `UnsupportedStatement` ([82b45](https://github.com/JSQLParser/JSqlParser/commit/82b459bfcd23851) Andreas Reichel)  
-  better statement error recovery ([b3d3a](https://github.com/JSQLParser/JSqlParser/commit/b3d3a8e492f74a8) Andreas Reichel)  
-  Syntax Sugar for the parser features ([1d943](https://github.com/JSQLParser/JSqlParser/commit/1d9438e7ef1a86f) Andreas Reichel)  
-  allow `EXTRACT` to be parsed as regular function also ([b85dc](https://github.com/JSQLParser/JSqlParser/commit/b85dc2fd0004652) Andreas Reichel)  
-  syntax sugar ([a3858](https://github.com/JSQLParser/JSqlParser/commit/a38581acd538d95) Andreas Reichel)  
-  syntax sugar ([df7c7](https://github.com/JSQLParser/JSqlParser/commit/df7c792184c61a6) Andreas Reichel)  
-  Syntax sugar ([67bfa](https://github.com/JSQLParser/JSqlParser/commit/67bfae673421d7c) Andreas Reichel)  
-  syntax sugar ([b0317](https://github.com/JSQLParser/JSqlParser/commit/b03170e180175b1) Andreas Reichel)  
-  syntax sugar ([57a29](https://github.com/JSQLParser/JSqlParser/commit/57a296b2c8c5bb0) Andreas Reichel)  
-  remove Aliases of `ParenthesedSelect`, `LateralSubSelect` and `ParenthesedFromItem` from the Table Names ([46682](https://github.com/JSQLParser/JSqlParser/commit/466826b9b115cb7) Andreas Reichel)  
-  better access to the `DataType` checks ([edeaf](https://github.com/JSQLParser/JSqlParser/commit/edeafc311c2ab7e) Andreas Reichel)  
-  Add Data Type information to task for making it easy to understand the expected return type ([31c55](https://github.com/JSQLParser/JSqlParser/commit/31c5533f49776c6) Andreas Reichel)  
-  Implicit Casts `SELECT DOUBLE PRECISION '1'` ([411a3](https://github.com/JSQLParser/JSqlParser/commit/411a3da9facf206) Andreas Reichel)  
-  Function Column Aliases without an Alias Name `func(x) (a, b, c)` ([b4ef7](https://github.com/JSQLParser/JSqlParser/commit/b4ef763614bf3a4) Andreas Reichel)  
-  Support BigQuery specific Aggregate clauses ([0179c](https://github.com/JSQLParser/JSqlParser/commit/0179cc0cac9ceeb) Andreas Reichel)  
-  syntax sugar for Binary Expressions like Conact, Addition, Multiplication ([ffdde](https://github.com/JSQLParser/JSqlParser/commit/ffddeef7199a056) Andreas Reichel)  
-  Hex to Long conversion ([620db](https://github.com/JSQLParser/JSqlParser/commit/620db709e48c22e) Andreas Reichel)  
-  syntax sugar for Expressions ([a5693](https://github.com/JSQLParser/JSqlParser/commit/a56934da1d3a7ae) Andreas Reichel)  
-  Salesforce SOQL `INCLUDES` and `EXCLUDES` operators (#1985) ([f3f0e](https://github.com/JSQLParser/JSqlParser/commit/f3f0e051358a493) lucarota)  
-  Google BigQuery `CAST` with `FORMAT` clause ([0d813](https://github.com/JSQLParser/JSqlParser/commit/0d813f03faa2b3b) Andreas Reichel)  
-  DuckDB Lambda Functions ([23679](https://github.com/JSQLParser/JSqlParser/commit/236793aaeabc30f) Andreas Reichel)  
-  DuckDB `STRUCT` with curly brackets and explicit Column Type Cast ([1cd57](https://github.com/JSQLParser/JSqlParser/commit/1cd576b32c774e8) Andreas Reichel)  
-  `RECURSIVE` does not need to be a reserved ([5cb4c](https://github.com/JSQLParser/JSqlParser/commit/5cb4c55067f4fe2) Andreas Reichel)  
-  DuckDB `STRUCT` with curly brackets ([339d6](https://github.com/JSQLParser/JSqlParser/commit/339d6baece2c199) Andreas Reichel)  
-  BigQuery `STRUCT` data types and literal ([4c187](https://github.com/JSQLParser/JSqlParser/commit/4c187d51055a3d8) Andreas Reichel)  
-  TablesNamesFinder can return also references to WITH items ([9d645](https://github.com/JSQLParser/JSqlParser/commit/9d64511239a4514) Andreas Reichel)  
-  allow double-quoted `DateTimeLiteral` like `DATETIME "2005-01-03 12:34:56"` ([f6790](https://github.com/JSQLParser/JSqlParser/commit/f6790913b754850) Andreas Reichel)  
-  support `DATETIME` literal used for Google BigQuery ([a386d](https://github.com/JSQLParser/JSqlParser/commit/a386d297c418921) Andreas Reichel)  
-  link `TOP` to AST node ([79c42](https://github.com/JSQLParser/JSqlParser/commit/79c42ed31eb6986) Andreas Reichel)  

### Bug Fixes

-  `AllTableColumns`, DuckDB specific `EXCLUDE` ([c9ecf](https://github.com/JSQLParser/JSqlParser/commit/c9ecfc6ddbdd139) Andreas Reichel)  
-  `AllColumns` Replacement shall be about Columns only ([f4b40](https://github.com/JSQLParser/JSqlParser/commit/f4b40e43a4f8d3d) Andreas Reichel)  
-  `FromItem` with Alias without `AS` keyword ([5f580](https://github.com/JSQLParser/JSqlParser/commit/5f580af190c6fbb) Andreas Reichel)  
-  set `stringValue` in `DoubleValue.setValue` (#2009) ([e07f8](https://github.com/JSQLParser/JSqlParser/commit/e07f8d019ddf38d) Damian)  
-  try working around `UnsupportedStatement` issue ([fbe97](https://github.com/JSQLParser/JSqlParser/commit/fbe97a8deb84ae9) Andreas Reichel)  
-  allow `BASE64` keyword ([7daf7](https://github.com/JSQLParser/JSqlParser/commit/7daf7af36d825f7) Andreas Reichel)  
-  `StructType` expressions must use Visitor instead of `toString()` ([b95d8](https://github.com/JSQLParser/JSqlParser/commit/b95d8e3e4ee01b0) Andreas Reichel)  
-  `AnyComparisionItem` with extra brackets ([4e1a1](https://github.com/JSQLParser/JSqlParser/commit/4e1a1535f4ef706) Andreas Reichel)  
-  `FOR UPDATE` clause should come after the select body ([cf7fe](https://github.com/JSQLParser/JSqlParser/commit/cf7fe157de372f3) Andreas Reichel)  
-  initialise the `SelectDeparser` with an `ExpressionDeparser` (but not with an empty Adaptor only) ([f417c](https://github.com/JSQLParser/JSqlParser/commit/f417c8f248c7bb1) Andreas Reichel)  
-  `ALTER ...` shall `captureRest()` only to the next statement terminator ([15d14](https://github.com/JSQLParser/JSqlParser/commit/15d14ab0b9dcadf) Andreas Reichel)  
-  correct the wrong Assertion ([8461e](https://github.com/JSQLParser/JSqlParser/commit/8461e8ad1a3f5e3) Andreas Reichel)  
-  don't insert space after certain punctuation ([159c2](https://github.com/JSQLParser/JSqlParser/commit/159c28ee8f68cab) Andreas Reichel)  
-  treat Array Brackets `[..]` as syntax characters and surround by space when normalizing for comparison ([c9d1e](https://github.com/JSQLParser/JSqlParser/commit/c9d1eaefca91c6e) Andreas Reichel)  
-  `REGEXP` does not need to be reserved ([f6524](https://github.com/JSQLParser/JSqlParser/commit/f65240f381f9855) Andreas Reichel)  
-  `REGEXP` does not need to be reserved ([a9e67](https://github.com/JSQLParser/JSqlParser/commit/a9e67667b9c1590) Andreas Reichel)  
-  Array Arguments without `ARRAY` keyword ([0f9a8](https://github.com/JSQLParser/JSqlParser/commit/0f9a8ec02786f5d) Andreas Reichel)  
-  Function with Array Arguments ([f782e](https://github.com/JSQLParser/JSqlParser/commit/f782eda7afa17d3) Andreas Reichel)  
-  parsing `SelectItem` shall support `Xor` ([c8839](https://github.com/JSQLParser/JSqlParser/commit/c883920a1175ffc) Andreas Reichel)  

### Other changes

**switched to version 5.0-SNAPSHOT**


[275e0](https://github.com/JSQLParser/JSqlParser/commit/275e0c0627bb8a2) Tobias Warneke *2024-06-30 20:26:08*

**corrected license header**


[5fb9f](https://github.com/JSQLParser/JSqlParser/commit/5fb9f568684ace1) Tobias Warneke *2024-06-30 20:21:47*

**corrected license header**


[456d5](https://github.com/JSQLParser/JSqlParser/commit/456d53b09c48f77) Tobias Warneke *2024-06-30 20:02:41*

**support custom DeParser (#2013)**


[74793](https://github.com/JSQLParser/JSqlParser/commit/7479342dd95125a) Redkale *2024-05-29 06:02:40*

**Add missing java.sql require (#1999)**

* Add missing java.sql 
* Update maven checkstyle 
* Fix gradle checkstyle 
* Bump surefire plugin 
* Skip modules in tests 

[df48c](https://github.com/JSQLParser/JSqlParser/commit/df48c4ba5b2b44f) Ethan McCue *2024-04-30 05:13:54*

**Add module info (#1998)**

* Add module info 
* Trailing newline 

[761b4](https://github.com/JSQLParser/JSqlParser/commit/761b45b2f6c4b81) Ethan McCue *2024-04-30 04:36:41*

****


[89ac0](https://github.com/JSQLParser/JSqlParser/commit/89ac0fc3c0af712) Tobias Warneke *2024-03-09 22:12:33*


## jsqlparser-4.9 (2024-03-09)

### Features

-  add DB2 special register `CURRENT TIMEZONE` ([c412d](https://github.com/JSQLParser/JSqlParser/commit/c412d6a52f9b2ea) Andreas Reichel)  
-  add additional CREATE VIEW modifiers (#1964) ([67e22](https://github.com/JSQLParser/JSqlParser/commit/67e220425f24148) David Goss)  
-  with no log (#1953) ([d9c44](https://github.com/JSQLParser/JSqlParser/commit/d9c44499d096b1f) mjh)  
-  support keyword "only" for postgresql (#1952) ([f1676](https://github.com/JSQLParser/JSqlParser/commit/f1676dd992911d9) 猫屎咖啡)  
-  support any number/order of merge operations (#1938) ([f1c52](https://github.com/JSQLParser/JSqlParser/commit/f1c525a1eaf3087) David Goss)  

### Bug Fixes

-  chained function calls of `SimpleFunction` ([98055](https://github.com/JSQLParser/JSqlParser/commit/9805581accf89d2) Andreas Reichel)  
-  issue #1948 `Between` with expression ([b9453](https://github.com/JSQLParser/JSqlParser/commit/b9453f228adf9ad) Andreas Reichel)  
-  return NULL when parsing empty Strings ([94fb8](https://github.com/JSQLParser/JSqlParser/commit/94fb87237f36cce) Andreas Reichel)  
-  allow Parameters like `$1`,`$2` ([17f5f](https://github.com/JSQLParser/JSqlParser/commit/17f5f2ad680dfdb) Andreas Reichel)  
-  allow `DATA` as `ColumnType()` keyword ([72a51](https://github.com/JSQLParser/JSqlParser/commit/72a51e58413a291) Andreas Reichel)  
-  make analytic expression visitor null-safe (#1944) ([768c6](https://github.com/JSQLParser/JSqlParser/commit/768c63f4660509b) David Goss)  
-  Fixes parsing failing for ALTER MODIFY queries not containing datatype (#1961) ([029fd](https://github.com/JSQLParser/JSqlParser/commit/029fd42e84e65ee) Tanish Grover)  
-  tables not find in parentheses join sql. (#1956) ([182f4](https://github.com/JSQLParser/JSqlParser/commit/182f484dc43945b) hancher)  
-  issue1875 (#1957) ([98aa9](https://github.com/JSQLParser/JSqlParser/commit/98aa90cb988580a) mjh)  
-  ExpressionVisitor.visit(AllTableColumns) method isn't being called. (#1942) ([bc166](https://github.com/JSQLParser/JSqlParser/commit/bc16618eaa8fd93) Brian S. O&#x27;Neill)  

### Other changes

****


[2319d](https://github.com/JSQLParser/JSqlParser/commit/2319da81bb27f4e) Tobias Warneke *2024-03-09 20:49:14*

**Handle select in ExpressionVisitorAdapter (#1972)**


[424a8](https://github.com/JSQLParser/JSqlParser/commit/424a852ac8071d7) Kaartic Sivaraam *2024-02-23 23:32:07*

**Update README.md**

* Fixes  #1968 

[8dcfb](https://github.com/JSQLParser/JSqlParser/commit/8dcfb4a3bf5682d) manticore-projects *2024-02-17 12:03:43*

**Guard Values against null/empty values (#1965)**

* Guard Values against null/empty values 
* The classes modified by this commit are &#x60;DoubleValue&#x60;, &#x60;LongValue&#x60;, and 
* &#x60;TimeValue&#x60;. Both &#x60;null&#x60; and empty strings provided to their 
* constructors fail, but they provide very different error messages 
* (NullPointerException and StringIndexOutOfBoundsException), which is 
* neither sensible nor helpful in debugging. 
* This commit adds a guard to throw &#x60;IllegalArgumentException&#x60; for both 
* cases in order to improve coherency and usefulness of the error 
* messages. 
* fix checkstyle issues 

[b0032](https://github.com/JSQLParser/JSqlParser/commit/b00322efa0c77d2) Heewon Lee *2024-02-14 07:34:40*

**support oracle alter table truncate partition  (#1954)**

* feat: oracle alter table truncate partition 
* feat: oracle alter table truncate partition 
* feat: code format 
* feat: code format 
* --------- 
* Co-authored-by: mjh &lt;majh118@chinaunicom.cn&gt; 

[cc7aa](https://github.com/JSQLParser/JSqlParser/commit/cc7aa01913a7201) mjh *2024-02-04 07:19:19*

**Build with Automatic-Module-Name for compatibility with the Java module system. (#1941)**


[92e02](https://github.com/JSQLParser/JSqlParser/commit/92e02c6da69d917) Brian S. O&#x27;Neill *2024-01-06 14:04:45*

**Create maven_deploy.yml**


[b4070](https://github.com/JSQLParser/JSqlParser/commit/b40705785751b49) Tobias *2023-12-28 22:31:54*

**corrected hopefully maven snapshot deployment**


[a70f0](https://github.com/JSQLParser/JSqlParser/commit/a70f0d1f3f3d91e) Tobias Warneke *2023-12-28 22:20:08*

**corrected hopefully maven snapshot deployment**


[f0d3a](https://github.com/JSQLParser/JSqlParser/commit/f0d3ab6b42193ae) Tobias Warneke *2023-12-28 22:17:43*

**finally done**


[6c1ca](https://github.com/JSQLParser/JSqlParser/commit/6c1caff118f84bd) Tobias Warneke *2023-12-28 00:29:26*


## jsqlparser-4.8 (2023-12-28)

### Features

-  support mysql with rollup (#1923) ([77f6f](https://github.com/JSQLParser/JSqlParser/commit/77f6fb8c92b3378) jxnu-liguobin)  
-  Support `FOR SHARE` (#1922) ([815f8](https://github.com/JSQLParser/JSqlParser/commit/815f8753d552d89) jxnu-liguobin)  
-  [MySQL] Support `TABLE STATEMENT` (#1921) ([313a4](https://github.com/JSQLParser/JSqlParser/commit/313a4b42444b2d2) jxnu-liguobin)  
-  Support `RENAME INDEX` for MySQL, `RENAME CONSTRAINT` for PostgreSQL (#1920) ([989a8](https://github.com/JSQLParser/JSqlParser/commit/989a84bb215283b) jxnu-liguobin)  
-  Add support comment in `create view` for MySQL and MariaDb (#1913) ([4d47e](https://github.com/JSQLParser/JSqlParser/commit/4d47e0ab7bc2872) jxnu-liguobin)  
-  Add support for `REFRESH MATERIALIZED VIEW` (#1911) ([425c7](https://github.com/JSQLParser/JSqlParser/commit/425c72eb7d7f931) jxnu-liguobin)  
-  `SimpleFunction` for faster parsing of simple, but deep nested functions ([085d7](https://github.com/JSQLParser/JSqlParser/commit/085d7504235e58c) Andreas Reichel)  
-  add support for snowflake merge statements (#1887) ([36b80](https://github.com/JSQLParser/JSqlParser/commit/36b806dede06260) David Goss)  
-  `ColDataType` supports `PUBLIC` schema and all non-restricted keywords for type ([1088d](https://github.com/JSQLParser/JSqlParser/commit/1088db7aea0b2f9) Andreas Reichel)  
-  T-SQL Join Hints ([5f09e](https://github.com/JSQLParser/JSqlParser/commit/5f09ec4914fbdd1) Andreas Reichel)  
-  old TSQL Joins `*=` and `=*` ([0b50d](https://github.com/JSQLParser/JSqlParser/commit/0b50da4cca555b6) Andreas Reichel)  
-  MS SQL Server `Merge` `Output` clause ([7bd42](https://github.com/JSQLParser/JSqlParser/commit/7bd42edaa0d9aed) Andreas Reichel)  
-  MS SQL Server `UPDATE ...` Index Hint ([f919e](https://github.com/JSQLParser/JSqlParser/commit/f919e00c30ff5df) Andreas Reichel)  
-  Postgres `Contains` and `ContainedBy` Operators ([28a4c](https://github.com/JSQLParser/JSqlParser/commit/28a4c080b718aba) Andreas Reichel)  
-  Postgres `Contains` and `ContainedBy` Operators ([09d6d](https://github.com/JSQLParser/JSqlParser/commit/09d6dfe7bc7acb8) Andreas Reichel)  
-  Clickhouse `GLOBAL IN ...` ([ced0d](https://github.com/JSQLParser/JSqlParser/commit/ced0d0090c5c9a9) Andreas Reichel)  
-  `CREATE INDEX IF NOT EXISTS...` ([da13d](https://github.com/JSQLParser/JSqlParser/commit/da13d7dc1dd1608) Andreas Reichel)  
-  support clickhouse global keyword in IN Expression ([a9ed7](https://github.com/JSQLParser/JSqlParser/commit/a9ed79825110df7) hezw)  

### Bug Fixes

-  refactor `JsonExpression`, avoiding expensive semantic lookahead and improving performance ([56515](https://github.com/JSQLParser/JSqlParser/commit/56515aba6ca893f) Andreas Reichel)  
-  `GO` shall terminate statement only, when appearing alone on an empty line ([14637](https://github.com/JSQLParser/JSqlParser/commit/14637ce64763b42) Andreas Reichel)  
-  De-Parse Oracle Hints in UPDATE, INSERT, DELETE and MERGE ([aaca0](https://github.com/JSQLParser/JSqlParser/commit/aaca05855f9a11b) Andreas Reichel)  
-  `UpdateSet` shall not have brackets with single element only ([15b9a](https://github.com/JSQLParser/JSqlParser/commit/15b9aef7ca05416) Andreas Reichel)  
-  make `GLOBAL` a restricted keyword, not usable as an Alias ([dd6cf](https://github.com/JSQLParser/JSqlParser/commit/dd6cf23150f4804) Andreas Reichel)  
-  Postgres `NextVal()` function ([e3afa](https://github.com/JSQLParser/JSqlParser/commit/e3afa5fbdebc715) Andreas Reichel)  
-  optional `Expression` in `FETCH` clause ([daee3](https://github.com/JSQLParser/JSqlParser/commit/daee30f7ae88bea) Andreas Reichel)  
-  allow `RAW` as `CreateParameter` ([ecd40](https://github.com/JSQLParser/JSqlParser/commit/ecd40386585a519) Andreas Reichel)  

### Other changes

**problem with old sonatype repo?**


[66dd0](https://github.com/JSQLParser/JSqlParser/commit/66dd0cffad8255d) Tobias Warneke *2023-12-28 00:10:08*

**problem with old sonatype repo?**


[19bde](https://github.com/JSQLParser/JSqlParser/commit/19bdef65c0e04c3) Tobias Warneke *2023-12-27 22:50:54*

**problem with old sonatype repo?**


[d6b4c](https://github.com/JSQLParser/JSqlParser/commit/d6b4cc374db4197) Tobias Warneke *2023-12-27 22:43:59*

**problem with old sonatype repo?**


[5ff53](https://github.com/JSQLParser/JSqlParser/commit/5ff53e835e675ea) Tobias Warneke *2023-12-27 00:54:26*

****


[6a327](https://github.com/JSQLParser/JSqlParser/commit/6a327b186528d1a) Tobias Warneke *2023-12-26 23:15:57*

**npe in memory leak verifier**


[63955](https://github.com/JSQLParser/JSqlParser/commit/639555315180cbf) Tobias Warneke *2023-12-26 23:01:20*

**allow reinitializing of javacc semanticize**


[44274](https://github.com/JSQLParser/JSqlParser/commit/44274b252c21cd1) Tobias Warneke *2023-12-26 22:53:28*

**Allowed to build JSqlParser on slower computers by increasing a fixed timeout. This should take machine power into account.**


[806d3](https://github.com/JSQLParser/JSqlParser/commit/806d3a39e8f093e) Tobias Warneke *2023-12-26 20:31:48*

**upgraded some plugins**


[cff03](https://github.com/JSQLParser/JSqlParser/commit/cff03ca200c674c) Tobias Warneke *2023-12-26 12:59:38*

**upgraded some plugins**


[256a1](https://github.com/JSQLParser/JSqlParser/commit/256a1eff904834b) Tobias Warneke *2023-12-25 23:53:18*

**upgraded some plugins**


[b2bd0](https://github.com/JSQLParser/JSqlParser/commit/b2bd025b424e472) Tobias Warneke *2023-12-25 23:51:46*

**corrected license header of some files**


[23ba3](https://github.com/JSQLParser/JSqlParser/commit/23ba326db05e863) Tobias Warneke *2023-12-25 23:39:37*

**Update sphinx.yml**


[2974f](https://github.com/JSQLParser/JSqlParser/commit/2974f4d20e2d785) manticore-projects *2023-12-16 07:13:01*

**Update sphinx.yml**


[a35fb](https://github.com/JSQLParser/JSqlParser/commit/a35fbe77b33a07b) manticore-projects *2023-12-16 04:42:01*

**Update build.gradle**


[546b3](https://github.com/JSQLParser/JSqlParser/commit/546b3ee00e4c3f8) manticore-projects *2023-12-15 09:14:49*

**Update build.gradle**


[48b3a](https://github.com/JSQLParser/JSqlParser/commit/48b3acbeef56b2d) manticore-projects *2023-12-15 09:11:51*

**Closed #1814, mysql and mariadb can use `index type` before `ON` (#1918)**


[b0aff](https://github.com/JSQLParser/JSqlParser/commit/b0aff31314a8df4) jxnu-liguobin *2023-12-15 04:56:55*

**Fix conflict (#1915)**


[2ae1d](https://github.com/JSQLParser/JSqlParser/commit/2ae1d53e56e45b2) jxnu-liguobin *2023-12-14 07:22:57*

**Fix typo in migration.rst (#1888)**

* Found a typo in the 4.7 migration document. Trivial PR. Please merge. 

[902e4](https://github.com/JSQLParser/JSqlParser/commit/902e4c46f783985) Ed Sabol *2023-11-10 03:05:39*

**Unit tests support multi-os and higher versions of jdk (#1886)**

* fix: tokenBlockPattern support \r\n or \r 
* test: remove nashorn ignore annotation to support jdk11+ 

[97e92](https://github.com/JSQLParser/JSqlParser/commit/97e9229d15df7d6) human-user *2023-11-08 03:07:04*

**Support for Nested With Clauses Added**


[59104](https://github.com/JSQLParser/JSqlParser/commit/59104fd96f29a2e) MathewJoseph31 *2023-09-12 12:01:59*

**Support for Array Contains (&>) and ContainedBy (<&) operator added**


[727c7](https://github.com/JSQLParser/JSqlParser/commit/727c732fd217843) MathewJoseph31 *2023-09-12 12:01:20*

**Support for postgres overlap operator && added, natural left/right/full outer joins added**


[6955c](https://github.com/JSQLParser/JSqlParser/commit/6955c4391e65a33) MathewJoseph31 *2023-09-12 12:01:15*

**add support for index hints in Update statement for MySQL**


[9a67d](https://github.com/JSQLParser/JSqlParser/commit/9a67d1277a0bf80) joeqiao *2022-11-08 01:27:25*

**added support for T-SQL left and right joins (*= and =*)**


[786c8](https://github.com/JSQLParser/JSqlParser/commit/786c8fc65858ff6) Nico *2019-01-29 11:11:07*


## jsqlparser-4.7 (2023-09-02)

### Breaking changes

-  add support for INTERPRET function parsing (#1816) ([180ec](https://github.com/JSQLParser/JSqlParser/commit/180ec68cc9fa7eb) Matteo Sist)  
-  Remove `ItemsList`, `MultiExpressionList`, `Replace` ([14170](https://github.com/JSQLParser/JSqlParser/commit/141708eabc4f2ea) Andreas Reichel)  
-  Consolidate the `ExpressionList`, removing many redundant List alike Classes and Productions ([288b1](https://github.com/JSQLParser/JSqlParser/commit/288b177fe9c8a4c) Andreas Reichel)  
-  remove `SelectExpressionItem` in favor of `SelectItem` ([b9057](https://github.com/JSQLParser/JSqlParser/commit/b9057d2b75cd1d7) Andreas Reichel)  
-  ClickHouse `Select...` ``FINAL` modifier ([4b7f2](https://github.com/JSQLParser/JSqlParser/commit/4b7f21c54c24d04) Andreas Reichel)  

### Features

-  H2 BYTEA Values `X'01' '02'` ([54828](https://github.com/JSQLParser/JSqlParser/commit/54828a456a7f192) Andreas Reichel)  
-  BigQuery Except(..) Replace(..) syntax ([4b4ae](https://github.com/JSQLParser/JSqlParser/commit/4b4ae04f44ff18b) Andreas Reichel)  
-  implement a few missing expressions ([04128](https://github.com/JSQLParser/JSqlParser/commit/0412897f9ea809f) Andreas Reichel)  
-  SQL:2016 TABLESAMPLE clause ([4d8a5](https://github.com/JSQLParser/JSqlParser/commit/4d8a512191a4a1b) Andreas Reichel)  
-  add a method checking balanced brackets ([52df3](https://github.com/JSQLParser/JSqlParser/commit/52df32dd8ec2c10) Andreas Reichel)  
-  add support for INTERPRET function parsing (#1816) ([180ec](https://github.com/JSQLParser/JSqlParser/commit/180ec68cc9fa7eb) Matteo Sist)  
-  MySQL `NOT RLIKE`, `NOT REGEXP` expressions ([f1325](https://github.com/JSQLParser/JSqlParser/commit/f132547f56a1edd) Andreas Reichel)  
-  Postgres `NOTNULL` support ([386dc](https://github.com/JSQLParser/JSqlParser/commit/386dc7a0df98f1c) manticore-projects)  
-  `QUALIFY` clause ([75e4d](https://github.com/JSQLParser/JSqlParser/commit/75e4d30747a7e6e) Andreas Reichel)  
-  T-SQL `FOR ...` clause ([8027d](https://github.com/JSQLParser/JSqlParser/commit/8027dbf2cbf9163) Andreas Reichel)  
-  Quoted Identifiers can contain double-quotes (PostgreSQL) ([73c55](https://github.com/JSQLParser/JSqlParser/commit/73c55fda1ac6a42) Andreas Reichel)  
-  functions blocks, parenthesed JSON Expressions ([5263b](https://github.com/JSQLParser/JSqlParser/commit/5263b91f3e555b7) Andreas Reichel)  
-  functions blocks, parenthesed JSON Expressions ([e19dc](https://github.com/JSQLParser/JSqlParser/commit/e19dc0e081f741d) Andreas Reichel)  
-  parse CREATE TRIGGER as UnsupportedStatement ([64b03](https://github.com/JSQLParser/JSqlParser/commit/64b0331f772278b) Andreas Reichel)  
-  chaining JSON Expressions ([6ef5e](https://github.com/JSQLParser/JSqlParser/commit/6ef5e0b6ee06211) Andreas Reichel)  
-  Write API documentation to the WebSite via XMLDoclet ([c5366](https://github.com/JSQLParser/JSqlParser/commit/c53667f8eff30e3) Andreas Reichel)  
-  `MEMBER OF` condition as shown at https://dev.mysql.com/doc/refman/8.0/en/json-search-functions.html#operator_member-of ([6e7a7](https://github.com/JSQLParser/JSqlParser/commit/6e7a78dfc563749) Andreas Reichel)  
-  access Elements of Array Columns ([09a70](https://github.com/JSQLParser/JSqlParser/commit/09a70a499121792) Andreas Reichel)  
-  JdbcNamedParameter allows "&" (instead of ":") ([c07a4](https://github.com/JSQLParser/JSqlParser/commit/c07a43b3c128a5d) Andreas Reichel)  
-  Consolidate the `ExpressionList`, removing many redundant List alike Classes and Productions ([288b1](https://github.com/JSQLParser/JSqlParser/commit/288b177fe9c8a4c) Andreas Reichel)  
-  ClickHouse `LIMIT ... BY ...` clause ([4d5e2](https://github.com/JSQLParser/JSqlParser/commit/4d5e26d3febe686) Andreas Reichel)  
-  implement SQL:2016 Convert() and Trim() ([3a27a](https://github.com/JSQLParser/JSqlParser/commit/3a27a9dd4add700) Andreas Reichel)  
-  Switch off contradicting `JOIN` qualifiers, when setting a qualifier ([b6ea8](https://github.com/JSQLParser/JSqlParser/commit/b6ea8b162450545) Andreas Reichel)  
-  Test if a JOIN is an INNER JOIN according to the SQL:2016 ([6281b](https://github.com/JSQLParser/JSqlParser/commit/6281b07a543b088) Andreas Reichel)  
-  ClickHouse `Select...` ``FINAL` modifier ([4b7f2](https://github.com/JSQLParser/JSqlParser/commit/4b7f21c54c24d04) Andreas Reichel)  
-  Multi-Part Names for Variables and Parameters ([9da7a](https://github.com/JSQLParser/JSqlParser/commit/9da7a06ebe9b036) Andreas Reichel)  
-  Oracle `HAVING` before `GROUP BY` ([4efb9](https://github.com/JSQLParser/JSqlParser/commit/4efb99f1510ad16) Andreas Reichel)  
-  Lateral View ([8a1bd](https://github.com/JSQLParser/JSqlParser/commit/8a1bdeccbadb04f) Andreas Reichel)  
-  FETCH uses EXPRESSION ([0979b](https://github.com/JSQLParser/JSqlParser/commit/0979b2e5ea76b8c) Andreas Reichel)  
-  Support more Statement Separators ([b0814](https://github.com/JSQLParser/JSqlParser/commit/b08148414bd8f30) Andreas Reichel)  
-  CREATE VIEW ... REFRESH AUTO... ([1c8d8](https://github.com/JSQLParser/JSqlParser/commit/1c8d8daf48ebac1) Andreas Reichel)  
-  Oracle Alternative Quoting ([c57c4](https://github.com/JSQLParser/JSqlParser/commit/c57c427032c91d0) Andreas Reichel)  
-  make important Classes Serializable ([b94b2](https://github.com/JSQLParser/JSqlParser/commit/b94b2cc6a8f8c7d) Andreas Reichel)  

### Bug Fixes

-  ExpressionList of Expressions in `Values` ([994e6](https://github.com/JSQLParser/JSqlParser/commit/994e6c63d065a48) Andreas Reichel)  
-  check for NULL before iterating ([beb68](https://github.com/JSQLParser/JSqlParser/commit/beb68d55239da97) Andreas Reichel)  
-  Backslash escaped single quote `'\''` ([a2975](https://github.com/JSQLParser/JSqlParser/commit/a29754341adeffc) Andreas Reichel)  
-  `INSERT` must use simple Column Names only ([420d7](https://github.com/JSQLParser/JSqlParser/commit/420d7d834760f14) Andreas Reichel)  
-  SPHINX modules and themes ([6f277](https://github.com/JSQLParser/JSqlParser/commit/6f277654b9344ec) Andreas Reichel)  
-  expose IntervalExpression attributes and use DeParser ([b6fab](https://github.com/JSQLParser/JSqlParser/commit/b6fab2a484e0b47) Andreas Reichel)  
-  throw the specific exception ([cb960](https://github.com/JSQLParser/JSqlParser/commit/cb960a35647a19a) Andreas Reichel)  
-  Complex Parsing Approach ([4f048](https://github.com/JSQLParser/JSqlParser/commit/4f0488ccb4611f0) Andreas Reichel)  
-  issue #1789 ([32ec5](https://github.com/JSQLParser/JSqlParser/commit/32ec56114c1fbc4) Andreas Reichel)  
-  issue #1789 ([d20c8](https://github.com/JSQLParser/JSqlParser/commit/d20c8e94de64e2a) Andreas Reichel)  
-  issue #1791 ([88d1b](https://github.com/JSQLParser/JSqlParser/commit/88d1b62f0038a9a) Andreas Reichel)  
-  Java Version 8 ([7cecd](https://github.com/JSQLParser/JSqlParser/commit/7cecd293cf4e0ea) Andreas Reichel)  
-  find the correct position when field belongs to an internal class ([21389](https://github.com/JSQLParser/JSqlParser/commit/21389b712995674) Andreas Reichel)  
-  Remove tests for `()`, since `ParenthesedExpressionList` will catch those too ([905ef](https://github.com/JSQLParser/JSqlParser/commit/905ef6512d592d6) Andreas Reichel)  
-  assign Enum case insensitive ([fc577](https://github.com/JSQLParser/JSqlParser/commit/fc577caa4146878) Andreas Reichel)  

### Other changes

****


[d45f2](https://github.com/JSQLParser/JSqlParser/commit/d45f29ef42a6859) Tobias Warneke *2023-09-01 22:07:49*

**Fixing a problem with an OP_CONCAT in WhenExpression (#1837)**

* fix: Concatenation in inner ELSE statement (Second level of Case Expression) 
* fix: broken tests 
* fix: Delete lookahead(3) 

[f05cb](https://github.com/JSQLParser/JSqlParser/commit/f05cb7ff4aa46c5) amigalev *2023-08-20 04:43:30*

**Update Gradle JavaCC parser to latest version (3.0.0) (#1843)**


[c59a0](https://github.com/JSQLParser/JSqlParser/commit/c59a088dfaee75a) Zbynek Konecny *2023-08-05 22:14:21*

**Update sql-parser-error.md**


[41d70](https://github.com/JSQLParser/JSqlParser/commit/41d705bb1036b34) manticore-projects *2023-07-26 00:37:51*

**Update sql-parser-error.md**


[812c6](https://github.com/JSQLParser/JSqlParser/commit/812c6cae3a8438b) manticore-projects *2023-07-26 00:37:14*

**Update sql-parser-error.md**


[b34d3](https://github.com/JSQLParser/JSqlParser/commit/b34d3c88a881c0f) manticore-projects *2023-07-25 00:09:05*

**Update sphinx.yml**

* fix the FURO theme 

[51cc4](https://github.com/JSQLParser/JSqlParser/commit/51cc444ff98ad1d) manticore-projects *2023-06-01 02:49:23*

**Create gradle.yml**


[be7fc](https://github.com/JSQLParser/JSqlParser/commit/be7fc53cff240be) manticore-projects *2023-05-18 10:16:14*

**Update sphinx.yml**


[11323](https://github.com/JSQLParser/JSqlParser/commit/11323388ab4abfd) manticore-projects *2023-05-14 13:10:16*

****


[0aa8a](https://github.com/JSQLParser/JSqlParser/commit/0aa8a629b9cecc2) Tobias Warneke *2023-04-27 21:18:29*

**Fix #1758: Use long for Feature.timeOut (#1759)**

* Co-authored-by: Tobias &lt;t.warneke@gmx.net&gt; 

[3314e](https://github.com/JSQLParser/JSqlParser/commit/3314edf0ea17772) Tomasz Zarna *2023-04-27 20:30:31*

**Ignoring unnecessarily generated jacoco report (#1762)**

* Ignoring unnecessarily generated jacoco report 
* Ignoring unnecessarily generated by pmd plugin 
* --------- 
* Co-authored-by: other &lt;other@ECE-A55006.austin.utexas.edu&gt; 
* Co-authored-by: Tobias &lt;t.warneke@gmx.net&gt; 

[1bbb1](https://github.com/JSQLParser/JSqlParser/commit/1bbb1443d84684c) optimizing-ci-builds *2023-04-27 19:50:42*

**Ignoring unnecessarily generated by pmd plugin (#1763)**

* Co-authored-by: other &lt;other@ECE-A55006.austin.utexas.edu&gt; 

[52648](https://github.com/JSQLParser/JSqlParser/commit/52648277e69fa07) optimizing-ci-builds *2023-04-27 19:49:15*

**Refactor Parenthesed SelectBody and FromItem (#1754)**

* Fixes #1684: Support CREATE MATERIALIZED VIEW with AUTO REFRESH 
* Support parsing create view statements in Redshift with AUTO REFRESH 
* option. 
* Reduce cyclomatic complexity in CreateView.toString 
* Extract adding the force option into a dedicated method resulting in the 
* cyclomatic complexity reduction of the CreateView.toString method. 
* Enhanced Keywords 
* Add Keywords and document, which keywords are allowed for what purpose 
* Fix incorrect tests 
* Define Reserved Keywords explicitly 
* Derive All Keywords from Grammar directly 
* Generate production for Object Names (semi-) automatically 
* Add parametrized Keyword Tests 
* Fix test resources 
* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Sphinx Documentation 
* Update the MANTICORE Sphinx Theme, but ignore it in GIT 
* Add the content to the Sphinx sites 
* Add a Gradle function to derive Stable and Snapshot version from GIT Tags 
* Add a Gradle GIT change task 
* Add a Gradle sphinx task 
* Add a special Test case for illustrating the use of JSQLParser 
* doc: request for &#x60;Conventional Commit&#x60; messages 
* feat: make important Classes Serializable 
* Implement Serializable for persisting via ObjectOutputStream 
* chore: Make Serializable 
* doc: Better integration of the RR diagrams 
* - apply neutral Sphinx theme 
* - insert the RR diagrams into the sphinx sources 
* - better documentation on Gradle dependencies 
* - link GitHub repository 
* Merge 
* feat: Oracle Alternative Quoting 
* - add support for Oracle Alternative Quoting e.g. &#x60;q&#x27;(...)&#x27;&#x60; 
* - fixes #1718 
* - add a Logo and FavIcon to the Website 
* - document recent changes on Quoting/Escaping 
* - add an example on building SQL from Java 
* - rework the README.md, promote the Website 
* - add Spotless Formatter, using Google Java Style (with Tab&#x3D;4 Spaces) 
* style: Appease PMD/Codacy 
* doc: fix the issue template 
* - fix the issue template 
* - fix the -SNAPSHOT version number 
* Update issue templates 
* Update issue templates 
* feat: Support more Statement Separators 
* - &#x60;GO&#x60; 
* - Slash &#x60;/&#x60; 
* - Two empty lines 
* feat: FETCH uses EXPRESSION 
* - &#x60;FETCH&#x60; uses &#x60;EXPRESSION&#x60; instead of SimpleJDBCParameter only 
* - Visit/Accept &#x60;FETCH&#x60; &#x60;EXPRESSION&#x60; instead of &#x60;append&#x60; to String 
* - Visit/Accept &#x60;OFFSET&#x60; &#x60;EXPRESSION&#x60; instead of &#x60;append&#x60; to String 
* - Gradle: remove obsolete/incompatible &#x60;jvmArgs&#x60; from Test() 
* style: apply Spotless 
* test: commit missing test 
* fix: JSon Operator can use Simple Function 
* Supports &#x60;Function() -&gt;&gt; Literal&#x60; (although &#x60;Function()&#x60; would not allow Nested Expression Parameters) 
* fixes #1571 
* style: Reformat changed files and headers 
* style: Remove unused variable 
* feat: Add support for Hangul &quot;\uAC00&quot;-&quot;\uD7A3&quot; 
* fixes #1747 
* style: expose &#x60;SetStatement&#x60; key-value list 
* fixes #1746 
* style: Appease PMD/Codacy 
* feat: &#x60;ConflictTarget&#x60; allows multiple &#x60;IndexColumnNames&#x60; 
* fixes #1749 
* fixes #1633 
* fixes #955 
* doc: fix reference in the Java Doc 
* build: better Upload Groovy Task 
* feat: ParenthesedSelectBody and ParenthesedFromItem 
* - First properly working version 
* - Work in progress, 13 tests failing 
* feat: ParenthesedSelectBody and ParenthesedFromItem 
* - delete unneeded ParenthesedJoin 
* - rename ParenthesisFromItem into ParenthesedFromItem 
* feat: ParenthesedSelectBody and ParenthesedFromItem 
* - fix &#x60;NULLS FIRST&#x60; and &#x60;NULLS LAST&#x60; 
* feat: ParenthesedSelectBody and ParenthesedFromItem 
* - fix Oracle Hints 
* feat: ParenthesedSelectBody and ParenthesedFromItem 
* - parse &#x60;SetOperation&#x60; only after a (first plain) SelectBody has found, this fixes the performance issue 
* - one more special Oracle Test succeeds 
* - 5 remaining test failures 
* feat: ParenthesedSelectBody and ParenthesedFromItem 
* - extract &#x60;OrderByElements&#x60; into &#x60;SelectBody&#x60; 
* - one more special Oracle Test succeeds 
* - all tests succeed 
* style: Appease PMD/Codacy 
* style: Appease PMD/Codacy 
* feat: Refactor SelectBody implementations 
* - &#x60;SelectBody&#x60; implements &#x60;FromItem&#x60; 
* - get rid of &#x60;SubSelect&#x60; and &#x60;SpecialSubSelect&#x60; 
* - &#x60;Merge&#x60; can use &#x60;FromItem&#x60; instead of &#x60;SubSelect&#x60; or &#x60;Table&#x60; 
* - &#x60;LateralSubSelect&#x60; extends &#x60;ParenthesedSelectBody&#x60; directly 
* - Simplify the &#x60;Select&#x60; statement, although it is still redundant since     &#x60;SelectBody&#x60; also could implement &#x60;Statement&#x60; directly 
* - &#x60;WithItem&#x60; can use &#x60;SelectBody&#x60; directly, which allows for nested &#x60;WithItems&#x60; 
* BREAKING-CHANGE: Lots of redundant methods and intermediate removed 
* feat: Refactor SelectBody implementations 
* - &#x60;SelectBody&#x60; implements &#x60;Statement&#x60; and so makes &#x60;Select&#x60; redundant 
* - get rid of &#x60;ValuesList&#x60; 
* - refactor &#x60;ValuesStatement&#x60; into &#x60;Values&#x60; which just implements &#x60;SelectBody&#x60; (and becomes a &#x60;Statement&#x60; and a &#x60;FromItem&#x60;), move to &#x60;select&#x60; package 
* BREAKING-CHANGE: Lots of redundant methods and intermediate removed 
* style: Code cleanup 
* - remove 3 unused/obsolete productions 
* - appease PMD/Codacy 
* feat: Merge &#x60;SelectBody&#x60; into &#x60;Select&#x60; Statement 
* - former &#x60;SelectBody&#x60; implements &#x60;Statement&#x60; and so becomes &#x60;Select&#x60; 
* - this reduces the AST by 1 hierarchy level 
* style: Remove unused import 
* test: @Disabled invalid Test 
* style: Appease PMD/Codacy 
* test: Add a SubSelect Parsing Test 
* --------- 
* Co-authored-by: zaza &lt;tzarna@gmail.com&gt; 

[a312d](https://github.com/JSQLParser/JSqlParser/commit/a312dcdc2d618f1) manticore-projects *2023-04-27 19:38:24*

****


[c1c92](https://github.com/JSQLParser/JSqlParser/commit/c1c92ade94ebe60) Tobias Warneke *2023-04-01 19:54:09*

**Assorted Fixes #7 (#1745)**

* Fixes #1684: Support CREATE MATERIALIZED VIEW with AUTO REFRESH 
* Support parsing create view statements in Redshift with AUTO REFRESH 
* option. 
* Reduce cyclomatic complexity in CreateView.toString 
* Extract adding the force option into a dedicated method resulting in the 
* cyclomatic complexity reduction of the CreateView.toString method. 
* Enhanced Keywords 
* Add Keywords and document, which keywords are allowed for what purpose 
* Fix incorrect tests 
* Define Reserved Keywords explicitly 
* Derive All Keywords from Grammar directly 
* Generate production for Object Names (semi-) automatically 
* Add parametrized Keyword Tests 
* Fix test resources 
* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Sphinx Documentation 
* Update the MANTICORE Sphinx Theme, but ignore it in GIT 
* Add the content to the Sphinx sites 
* Add a Gradle function to derive Stable and Snapshot version from GIT Tags 
* Add a Gradle GIT change task 
* Add a Gradle sphinx task 
* Add a special Test case for illustrating the use of JSQLParser 
* doc: request for &#x60;Conventional Commit&#x60; messages 
* feat: make important Classes Serializable 
* Implement Serializable for persisting via ObjectOutputStream 
* chore: Make Serializable 
* doc: Better integration of the RR diagrams 
* - apply neutral Sphinx theme 
* - insert the RR diagrams into the sphinx sources 
* - better documentation on Gradle dependencies 
* - link GitHub repository 
* Merge 
* feat: Oracle Alternative Quoting 
* - add support for Oracle Alternative Quoting e.g. &#x60;q&#x27;(...)&#x27;&#x60; 
* - fixes #1718 
* - add a Logo and FavIcon to the Website 
* - document recent changes on Quoting/Escaping 
* - add an example on building SQL from Java 
* - rework the README.md, promote the Website 
* - add Spotless Formatter, using Google Java Style (with Tab&#x3D;4 Spaces) 
* style: Appease PMD/Codacy 
* doc: fix the issue template 
* - fix the issue template 
* - fix the -SNAPSHOT version number 
* Update issue templates 
* Update issue templates 
* feat: Support more Statement Separators 
* - &#x60;GO&#x60; 
* - Slash &#x60;/&#x60; 
* - Two empty lines 
* feat: FETCH uses EXPRESSION 
* - &#x60;FETCH&#x60; uses &#x60;EXPRESSION&#x60; instead of SimpleJDBCParameter only 
* - Visit/Accept &#x60;FETCH&#x60; &#x60;EXPRESSION&#x60; instead of &#x60;append&#x60; to String 
* - Visit/Accept &#x60;OFFSET&#x60; &#x60;EXPRESSION&#x60; instead of &#x60;append&#x60; to String 
* - Gradle: remove obsolete/incompatible &#x60;jvmArgs&#x60; from Test() 
* style: apply Spotless 
* test: commit missing test 
* fix: JSon Operator can use Simple Function 
* Supports &#x60;Function() -&gt;&gt; Literal&#x60; (although &#x60;Function()&#x60; would not allow Nested Expression Parameters) 
* fixes #1571 
* style: Reformat changed files and headers 
* style: Remove unused variable 
* feat: Add support for Hangul &quot;\uAC00&quot;-&quot;\uD7A3&quot; 
* fixes #1747 
* style: expose &#x60;SetStatement&#x60; key-value list 
* fixes #1746 
* style: Appease PMD/Codacy 
* feat: &#x60;ConflictTarget&#x60; allows multiple &#x60;IndexColumnNames&#x60; 
* fixes #1749 
* fixes #1633 
* fixes #955 
* doc: fix reference in the Java Doc 
* build: better Upload Groovy Task 
* --------- 
* Co-authored-by: zaza &lt;tzarna@gmail.com&gt; 

[31ef1](https://github.com/JSQLParser/JSqlParser/commit/31ef1aaf23e2917) manticore-projects *2023-03-21 22:04:58*

**disable xml report (#1748)**

* Co-authored-by: other &lt;other@ECE-A55006.austin.utexas.edu&gt; 

[476d9](https://github.com/JSQLParser/JSqlParser/commit/476d96965492131) optimizing-ci-builds *2023-03-21 21:58:25*

**Assorted Fixes #6 (#1740)**

* Fixes #1684: Support CREATE MATERIALIZED VIEW with AUTO REFRESH 
* Support parsing create view statements in Redshift with AUTO REFRESH 
* option. 
* Reduce cyclomatic complexity in CreateView.toString 
* Extract adding the force option into a dedicated method resulting in the 
* cyclomatic complexity reduction of the CreateView.toString method. 
* Enhanced Keywords 
* Add Keywords and document, which keywords are allowed for what purpose 
* Fix incorrect tests 
* Define Reserved Keywords explicitly 
* Derive All Keywords from Grammar directly 
* Generate production for Object Names (semi-) automatically 
* Add parametrized Keyword Tests 
* Fix test resources 
* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Sphinx Documentation 
* Update the MANTICORE Sphinx Theme, but ignore it in GIT 
* Add the content to the Sphinx sites 
* Add a Gradle function to derive Stable and Snapshot version from GIT Tags 
* Add a Gradle GIT change task 
* Add a Gradle sphinx task 
* Add a special Test case for illustrating the use of JSQLParser 
* doc: request for &#x60;Conventional Commit&#x60; messages 
* feat: make important Classes Serializable 
* Implement Serializable for persisting via ObjectOutputStream 
* chore: Make Serializable 
* doc: Better integration of the RR diagrams 
* - apply neutral Sphinx theme 
* - insert the RR diagrams into the sphinx sources 
* - better documentation on Gradle dependencies 
* - link GitHub repository 
* Merge 
* feat: Oracle Alternative Quoting 
* - add support for Oracle Alternative Quoting e.g. &#x60;q&#x27;(...)&#x27;&#x60; 
* - fixes #1718 
* - add a Logo and FavIcon to the Website 
* - document recent changes on Quoting/Escaping 
* - add an example on building SQL from Java 
* - rework the README.md, promote the Website 
* - add Spotless Formatter, using Google Java Style (with Tab&#x3D;4 Spaces) 
* style: Appease PMD/Codacy 
* doc: fix the issue template 
* - fix the issue template 
* - fix the -SNAPSHOT version number 
* Update issue templates 
* Update issue templates 
* feat: Support more Statement Separators 
* - &#x60;GO&#x60; 
* - Slash &#x60;/&#x60; 
* - Two empty lines 
* feat: FETCH uses EXPRESSION 
* - &#x60;FETCH&#x60; uses &#x60;EXPRESSION&#x60; instead of SimpleJDBCParameter only 
* - Visit/Accept &#x60;FETCH&#x60; &#x60;EXPRESSION&#x60; instead of &#x60;append&#x60; to String 
* - Visit/Accept &#x60;OFFSET&#x60; &#x60;EXPRESSION&#x60; instead of &#x60;append&#x60; to String 
* - Gradle: remove obsolete/incompatible &#x60;jvmArgs&#x60; from Test() 
* style: apply Spotless 
* test: commit missing test 
* feat: Unicode CJK Unified Ideographs (Unicode block) 
* fixes #1741 
* feat: Unicode CJK Unified Ideographs (Unicode block) 
* fixes #1741 
* feat: Functions with nested Attributes 
* Supports &#x60;SELECT schemaName.f1(arguments).f2(arguments).f3.f4&#x60; and similar constructs 
* fixes #1742 
* fixes #1050 
* --------- 
* Co-authored-by: zaza &lt;tzarna@gmail.com&gt; 

[adeed](https://github.com/JSQLParser/JSqlParser/commit/adeed5359c65b8f) manticore-projects *2023-03-09 21:22:40*

**version 4.7-SNAPSHOT**


[74570](https://github.com/JSQLParser/JSqlParser/commit/745701bfb90a233) Tobias Warneke *2023-02-23 21:41:03*

**Update issue templates**


[4aeaf](https://github.com/JSQLParser/JSqlParser/commit/4aeafbc68f0525c) manticore-projects *2023-02-01 01:37:53*

**Update issue templates**


[46314](https://github.com/JSQLParser/JSqlParser/commit/46314c41eb06957) manticore-projects *2023-02-01 01:24:35*

**Sphinx Documentation**

* Update the MANTICORE Sphinx Theme, but ignore it in GIT 
* Add the content to the Sphinx sites 
* Add a Gradle function to derive Stable and Snapshot version from GIT Tags 
* Add a Gradle GIT change task 
* Add a Gradle sphinx task 
* Add a special Test case for illustrating the use of JSQLParser 

[2ef66](https://github.com/JSQLParser/JSqlParser/commit/2ef6637afffa943) Andreas Reichel *2023-01-21 04:06:00*

**Define Reserved Keywords explicitly**

* Derive All Keywords from Grammar directly 
* Generate production for Object Names (semi-) automatically 
* Add parametrized Keyword Tests 

[f49e8](https://github.com/JSQLParser/JSqlParser/commit/f49e828fc9c5f2f) Andreas Reichel *2023-01-21 04:05:51*

**Adjust Gradle to JUnit 5**

* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 

[e960a](https://github.com/JSQLParser/JSqlParser/commit/e960a35e591ce07) Andreas Reichel *2023-01-21 04:05:51*

**Enhanced Keywords**

* Add Keywords and document, which keywords are allowed for what purpose 

[b5321](https://github.com/JSQLParser/JSqlParser/commit/b5321d6e8bac588) Andreas Reichel *2023-01-21 04:05:51*

**Remove unused imports**


[a016b](https://github.com/JSQLParser/JSqlParser/commit/a016be0c7f8a46f) Andreas Reichel *2023-01-21 04:05:51*

**Fix test resources**


[86f33](https://github.com/JSQLParser/JSqlParser/commit/86f337dbafd10ab) Andreas Reichel *2023-01-21 04:05:51*

**Do not mark SpeedTest for concurrent execution**


[67f79](https://github.com/JSQLParser/JSqlParser/commit/67f7951a048a05d) Andreas Reichel *2023-01-21 04:05:51*

**Fix incorrect tests**


[5fae2](https://github.com/JSQLParser/JSqlParser/commit/5fae2f5984c3b39) Andreas Reichel *2023-01-21 04:05:51*

**Remove unused imports**


[3ba54](https://github.com/JSQLParser/JSqlParser/commit/3ba5410bf052091) Andreas Reichel *2023-01-21 04:05:51*

**Adjust Gradle to JUnit 5**

* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 

[2d51a](https://github.com/JSQLParser/JSqlParser/commit/2d51a82d3e9e51c) Andreas Reichel *2023-01-21 04:05:51*

**Do not mark SpeedTest for concurrent execution**


[232af](https://github.com/JSQLParser/JSqlParser/commit/232aff6873f24f9) Andreas Reichel *2023-01-21 04:05:51*

**Reduce cyclomatic complexity in CreateView.toString**

* Extract adding the force option into a dedicated method resulting in the 
* cyclomatic complexity reduction of the CreateView.toString method. 

[ea447](https://github.com/JSQLParser/JSqlParser/commit/ea4477bb775ebdb) zaza *2023-01-08 20:43:40*

**Fixes #1684: Support CREATE MATERIALIZED VIEW with AUTO REFRESH**

* Support parsing create view statements in Redshift with AUTO REFRESH 
* option. 

[74715](https://github.com/JSQLParser/JSqlParser/commit/747152a9fc1bfd1) zaza *2022-12-11 20:03:52*


## jsqlparser-4.6 (2023-02-23)

### Bug Fixes

-  add missing public Getter (#1632) ([d2212](https://github.com/JSQLParser/JSqlParser/commit/d2212776ac5eb83) manticore-projects)  

### Other changes

**actualized release plugin**


[9911a](https://github.com/JSQLParser/JSqlParser/commit/9911ad7a990356f) Tobias Warneke *2023-02-23 21:17:52*

**actualized release plugin**


[0b2c3](https://github.com/JSQLParser/JSqlParser/commit/0b2c33b29928ec4) Tobias Warneke *2023-02-23 21:16:43*

****


[b07f7](https://github.com/JSQLParser/JSqlParser/commit/b07f791b27c3ee4) Tobias Warneke *2023-02-23 19:50:39*

**Update build.gradle**


[35233](https://github.com/JSQLParser/JSqlParser/commit/35233882aaffb0e) Tobias *2023-02-17 20:20:25*

**Update README.md**


[0b092](https://github.com/JSQLParser/JSqlParser/commit/0b09229a3d92547) Tobias *2023-02-17 16:27:41*

**Oracle Alternative Quoting (#1722)**

* Fixes #1684: Support CREATE MATERIALIZED VIEW with AUTO REFRESH 
* Support parsing create view statements in Redshift with AUTO REFRESH 
* option. 
* Reduce cyclomatic complexity in CreateView.toString 
* Extract adding the force option into a dedicated method resulting in the 
* cyclomatic complexity reduction of the CreateView.toString method. 
* Enhanced Keywords 
* Add Keywords and document, which keywords are allowed for what purpose 
* Fix incorrect tests 
* Define Reserved Keywords explicitly 
* Derive All Keywords from Grammar directly 
* Generate production for Object Names (semi-) automatically 
* Add parametrized Keyword Tests 
* Fix test resources 
* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Sphinx Documentation 
* Update the MANTICORE Sphinx Theme, but ignore it in GIT 
* Add the content to the Sphinx sites 
* Add a Gradle function to derive Stable and Snapshot version from GIT Tags 
* Add a Gradle GIT change task 
* Add a Gradle sphinx task 
* Add a special Test case for illustrating the use of JSQLParser 
* doc: request for &#x60;Conventional Commit&#x60; messages 
* feat: make important Classes Serializable 
* Implement Serializable for persisting via ObjectOutputStream 
* chore: Make Serializable 
* doc: Better integration of the RR diagrams 
* - apply neutral Sphinx theme 
* - insert the RR diagrams into the sphinx sources 
* - better documentation on Gradle dependencies 
* - link GitHub repository 
* Merge 
* feat: Oracle Alternative Quoting 
* - add support for Oracle Alternative Quoting e.g. &#x60;q&#x27;(...)&#x27;&#x60; 
* - fixes #1718 
* - add a Logo and FavIcon to the Website 
* - document recent changes on Quoting/Escaping 
* - add an example on building SQL from Java 
* - rework the README.md, promote the Website 
* - add Spotless Formatter, using Google Java Style (with Tab&#x3D;4 Spaces) 
* style: Appease PMD/Codacy 
* doc: fix the issue template 
* - fix the issue template 
* - fix the -SNAPSHOT version number 
* Update issue templates 
* Update issue templates 
* feat: Support more Statement Separators 
* - &#x60;GO&#x60; 
* - Slash &#x60;/&#x60; 
* - Two empty lines 
* --------- 
* Co-authored-by: zaza &lt;tzarna@gmail.com&gt; 

[e71e5](https://github.com/JSQLParser/JSqlParser/commit/e71e57dfe4b377c) manticore-projects *2023-02-07 20:18:52*

**Issue1673 case within brackets (#1675)**

* fix: add missing public Getter 
* Add public Getter for &#x60;updateSets&#x60; 
* Fixes #1630 
* fix: Case within brackets 
* fixes #1673 

[2ced7](https://github.com/JSQLParser/JSqlParser/commit/2ced7ded930f8b0) manticore-projects *2023-01-31 20:56:01*

**Added support for SHOW INDEX from table (#1704)**

* Added support for SHOW INDEX from table 
* Added * import 
* fix for javadoc 
* added &lt;doclint&gt;none&lt;/doclint&gt; 

[a2618](https://github.com/JSQLParser/JSqlParser/commit/a2618321135d517) Jayant Kumar Yadav *2023-01-31 20:54:05*

****


[d33f6](https://github.com/JSQLParser/JSqlParser/commit/d33f6f5a658751d) Tobias Warneke *2023-01-22 15:43:07*

**Sphinx Website (#1624)**

* Enhanced Keywords 
* Add Keywords and document, which keywords are allowed for what purpose 
* Fix incorrect tests 
* Define Reserved Keywords explicitly 
* Derive All Keywords from Grammar directly 
* Generate production for Object Names (semi-) automatically 
* Add parametrized Keyword Tests 
* Fix test resources 
* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Keyword test adopt JUnit5 
* Update keywords 
* CheckStyle sanitation of method names 
* Merge Master 
* Add Jupiter Parameters dependency again 
* Automate the &#x60;updateKeywords&#x60; Step 
* Update PMD and rules 
* Rewrite test expected to fail 
* Appease Codacy 
* Remove broken rule warning about perfectly fine switch-case statements 
* Force Changes 
* Fix Merge Issues 
* Read Tokens directly from the Grammar File without invoking JTREE 
* - read Tokens per REGEX Matcher 
* - move Reserved Keywords from Grammar into ParserKeywordsUtils 
* - adjust the Tests 
* Appease PMD/Codacy 
* Extract the Keywords from the Grammar by using JTRee (instead of Regex) 
* Add some tests to ensure, that all Keywords or found 
* Appease Codacy/PMD 
* Separate UpdateKeywords Task again 
* Including it into compileJavacc won&#x27;t work since it depends on compiling the ParserKeywordUtils.java 
* Single file compilation did not work 
* Clean-up the imports 
* Add JavaCC dependency to Maven for building ParserKeywordsUtils 
* Add JavaCC dependency to Maven for building ParserKeywordsUtils 
* Merge Upstream 
* Merge Master 
* Fixes broken PR #1524 and Commit fb6e950ce0e62ebcd7a44ba9eea679da2b04b2ed 
* Add AST Visualization 
* Show the Statement&#x27;s Java Objects in a tree hierarchy 
* Sphinx Documentation 
* Update the MANTICORE Sphinx Theme, but ignore it in GIT 
* Add the content to the Sphinx sites 
* Add a Gradle function to derive Stable and Snapshot version from GIT Tags 
* Add a Gradle GIT change task 
* Add a Gradle sphinx task 
* Add a special Test case for illustrating the use of JSQLParser 
* test: Document an additional Special Oracle test success 
* doc: ignore the autogenerated changelog.rst in GIT 
* build: temporarily reduce the Code Coverage requirements 
* Temporarily reduce the Coverage checks regarding Minimum Coverage and Maximum Missed Lines in order to get the Keywords PR accepted. We should do a major Code cleanup afterwards. 
* build: Clean-up the Gradle Build 
* Prefix the Sphinx Prolog Variables with JSQLPARSER in order to allow for build the Main Website for various projects 
* Remove some redundant version requests for PMD, CheckStyle and friends 
* Remove JUnit-4 dependency and add HarmCrest 
* Complete the PUBLISHING task 
* doc: Explain the &#x60;&#x60;updateKeywords&#x60;&#x60; Gradle Task 
* build: Un-escape the Unicode on the changelog file 
* build: Un-escape the Unicode on the changelog file 
* doc: Cleanup 
* Unescape unicode characters from Git Changelog 
* Remove obsolete code from Sphinx&#x27; conf.py 
* doc: Properly un-escape the Git Commit message 
* doc: request for &#x60;Conventional Commit&#x60; messages 
* doc: correctly refer to &#x60;RelObjectNameWithoutValue()&#x60; 
* build: upload the built files via Excec/SFTP 
* doc: Add an example on Token White-listing 
* doc: write the correct Git Repository 
* doc: pronounce the OVERLAPS example more 
* feat: make important Classes Serializable 
* Implement Serializable for persisting via ObjectOutputStream 
* doc: Add the &quot;How to Use&quot; java code 
* chore: Make Serializable 
* fix: Non-serializable field in serializable class 
* build: various fixes to the Maven build file 
* add the Keywords Documentation file to the task 
* exclude the Sphinx files from the license header plugin 
* fix the JavaDoc plugin options 
* build: add the Keywords Documentation file to the task 
* doc: add a page about actually Reserved Keywords 
* build: avoid PMD/Codacy for Sphinx Documentation 
* update Changelog 
* build: Add Sphinx GitHub Action 
* Add a GitHub Action, which will 
* - Install Sphinx and Extensions 
* - Install Gradle Wrapper 
* - Run Gradle Wrapper Task &#x60;sphinx&#x60; 
* - Deploy the generated static HTML site to GH Pages 
* fix: fix a merge error, brackets 
* fix: remove JavaCC dependency 
* Parse Tokens via Regex 
* Move JavaCC Token Parser into the KeywordsTest 
* Make JavaCC a Test Dependency only 
* doc: Fix Maven Artifact Version 
* style: Avoid throwing raw exception types. 
* style: Avoid throwing raw exception types. 
* doc: Better integration of the RR diagrams 
* - apply neutral Sphinx theme 
* - insert the RR diagrams into the sphinx sources 
* - better documentation on Gradle dependencies 
* - link GitHub repository 
* build: gradle, execute all Checks after Test 
* Co-authored-by: Tobias &lt;t.warneke@gmx.net&gt; 

[be8e7](https://github.com/JSQLParser/JSqlParser/commit/be8e7a8a1d77184) manticore-projects *2023-01-20 21:45:35*

**Assorted Fixes #5 (#1715)**

* refactor: Merge REPLACE into UPSERT 
* fixes #1706 
* feat: &#x60;DROP TEMPORARY TABLE ...&#x60; 
* fixes #1712 
* build: PMD compliance 
* ci: Merge master 
* feat: Configurable backslash &#x60;\&#x60; escaping 
* - Enables &#x60;\&#x60; as escape character in String Literals (beside SQL:2016 compliant &#x60;&#x27;&#x60;) 
* - Default is OFF (since its not SQL:2016 compliant) 
* - Activate per Parser Feature 
* - Fixes #1638 
* - Fixes #1209 
* - Fixes #1173 
* - Fixes #1172 
* - Fixes #832 
* - Fixes #827 
* - Fixes #578 
* BREAKING-CHANGE: Backslash Escaping needs to be activated explicitly or else Backslash won&#x27;t work as Escape Character. 
* style: Checkstyle 
* style: remove dead code 
* style: PMD compliance 
* style: Checkstyle, unused import 
* feat: allow &#x60;S_CHAR_LITERAL&#x60; to break lines 
* - fixes #875 

[a00d7](https://github.com/JSQLParser/JSqlParser/commit/a00d77a100bfab7) manticore-projects *2023-01-20 21:32:20*

**Support DROP MATERIALIZED VIEW statements (#1711)**


[1af68](https://github.com/JSQLParser/JSqlParser/commit/1af682d436055ad) Tomasz Zarna *2023-01-12 21:37:42*

**corrected readme**


[4dfd2](https://github.com/JSQLParser/JSqlParser/commit/4dfd2e43fcdd3ab) Tobias Warneke *2023-01-04 21:07:17*

**Update README.md**

* lgtm removed 

[954b8](https://github.com/JSQLParser/JSqlParser/commit/954b8dd2e760a01) Tobias *2022-12-27 10:34:18*

**Fix #1686: add support for creating views with "IF NOT EXISTS" clause (#1690)**


[0f34f](https://github.com/JSQLParser/JSqlParser/commit/0f34f5bc647365d) Tomasz Zarna *2022-12-22 21:52:35*

**Assorted Fixes #4 (#1676)**

* support clickhouse global keyword in join 
* fix: add missing public Getter 
* Add public Getter for &#x60;updateSets&#x60; 
* Fixes #1630 
* feat: Clickhouse GLOBAL JOIN 
* All credits to @julianzlzhang 
* fixes #1615 
* fixes #1535 
* feat: IF/ELSE statements supports Block 
* Make &#x60;If... Else...&#x60; statements work with Blocks 
* Make &#x60;Statement()&#x60; production work with &#x60;Block()&#x60; 
* Rewrite the &#x60;Block()&#x60; related Unit Tests 
* fixes #1682 
* fix: Revert unintended changes to the Special Oracle Tests 
* fix: &#x60;SET&#x60; statement supports &#x60;UserVariable&#x60; 
* Make &#x60;SetStatement&#x60; parse Objects instead of Names only 
* Add Grammar to accept &#x60;UserVariable&#x60; (e.g. &quot;set @Flag &#x3D; 1&quot;) 
* Add Test Case for &#x60;UserVariable&#x60; 
* fixes #1682 
* feat: Google Spanner Support 
* Replaces PR #1415, all credit goes to @s13o 
* Re-arranged some recently added Tokens in alphabetical order 
* Update Keywords 
* fix: fix JSonExpression, accept Expressions 
* Make JSonExpression accept Expressions 
* Add Testcase 
* Expose Idents() and Operators() 
* Fixes #1696 
* test: add Test for Issue #1237 
* Co-authored-by: Zhang Zhongliang &lt;zhangzhongliang@xiaomi.com&gt; 

[8d9db](https://github.com/JSQLParser/JSqlParser/commit/8d9db7052c3aeb5) manticore-projects *2022-12-22 21:17:55*

**Fixed download war script in the renderRR task (#1659)**

* Co-authored-by: Hai Chang &lt;haichang@microsoft.com&gt; 

[08a92](https://github.com/JSQLParser/JSqlParser/commit/08a92fcd7b4f7f2) haha1903 *2022-12-10 09:23:53*

**Assorted fixes (#1666)**

* fix: add missing public Getter 
* Add public Getter for &#x60;updateSets&#x60; 
* Fixes #1630 
* feat: LISTAGG() with OVER() clause 
* fixes issue #1652 
* fixes 3 more Special Oracle Tests 
* fix: White-list CURRENT_DATE and CURRENT_TIMESTAMP tokens 
* allows CURRENT_DATE(3) and CURRENT_TIMESTAMP(3) as regular functions 
* fixes #1507 
* fixes #1607 
* feat: Deparser for Expression Lists 
* Visit each Expression of a List instead ExpressionList.toString() 
* fixes #1608 
* fix: Lookahead needed 

[bff26](https://github.com/JSQLParser/JSqlParser/commit/bff268a7c699947) manticore-projects *2022-11-20 10:06:01*

**Fix parsing statements with multidimensional array PR2 (#1665)**

* Fix parsing statements with multidimensional array 
* fix: Whitelist LOCKED keyword 
* Co-authored-by: Andrei Lisouski &lt;alisousk@akamai.com&gt; 

[e1865](https://github.com/JSQLParser/JSqlParser/commit/e186588f044753f) manticore-projects *2022-11-20 09:59:26*

**removed disabled from Keyword tests and imports**


[af6c2](https://github.com/JSQLParser/JSqlParser/commit/af6c2702c8a505c) Tobias Warneke *2022-11-02 23:02:38*

**removed disabled from Keyword tests**


[89a9a](https://github.com/JSQLParser/JSqlParser/commit/89a9a575fac1ba8) Tobias Warneke *2022-11-02 22:58:19*

****


[8a018](https://github.com/JSQLParser/JSqlParser/commit/8a0183311b01d2d) Tobias Warneke *2022-10-28 22:30:25*

****


[67de4](https://github.com/JSQLParser/JSqlParser/commit/67de469e585060f) Tobias Warneke *2022-10-25 23:26:28*

**Enhanced Keywords (#1382)**

* Enhanced Keywords 
* Add Keywords and document, which keywords are allowed for what purpose 
* Fix incorrect tests 
* Define Reserved Keywords explicitly 
* Derive All Keywords from Grammar directly 
* Generate production for Object Names (semi-) automatically 
* Add parametrized Keyword Tests 
* Fix test resources 
* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Keyword test adopt JUnit5 
* Update keywords 
* CheckStyle sanitation of method names 
* Merge Master 
* Add Jupiter Parameters dependency again 
* Automate the &#x60;updateKeywords&#x60; Step 
* Update PMD and rules 
* Rewrite test expected to fail 
* Appease Codacy 
* Remove broken rule warning about perfectly fine switch-case statements 
* Force Changes 
* Fix Merge Issues 
* Read Tokens directly from the Grammar File without invoking JTREE 
* - read Tokens per REGEX Matcher 
* - move Reserved Keywords from Grammar into ParserKeywordsUtils 
* - adjust the Tests 
* Appease PMD/Codacy 
* Extract the Keywords from the Grammar by using JTRee (instead of Regex) 
* Add some tests to ensure, that all Keywords or found 
* Appease Codacy/PMD 
* Separate UpdateKeywords Task again 
* Including it into compileJavacc won&#x27;t work since it depends on compiling the ParserKeywordUtils.java 
* Single file compilation did not work 
* Clean-up the imports 
* Add JavaCC dependency to Maven for building ParserKeywordsUtils 
* Add JavaCC dependency to Maven for building ParserKeywordsUtils 
* Merge Upstream 
* Merge Master 
* Fixes broken PR #1524 and Commit fb6e950ce0e62ebcd7a44ba9eea679da2b04b2ed 
* Add AST Visualization 
* Show the Statement&#x27;s Java Objects in a tree hierarchy 
* build: temporarily reduce the Code Coverage requirements 
* Temporarily reduce the Coverage checks regarding Minimum Coverage and Maximum Missed Lines in order to get the Keywords PR accepted. We should do a major Code cleanup afterwards. 
* build:  JSQLParser is a build  dependency 
* chore:  Update keywords 
* feat: add line count to output 

[4863e](https://github.com/JSQLParser/JSqlParser/commit/4863eb5a8e30a5d) manticore-projects *2022-10-25 23:15:32*

**#1610 Support for SKIP LOCKED tokens on SELECT statements (#1649)**

* Co-authored-by: Lucas Dillmann &lt;lucas.dillmann@totvs.com.br&gt; 

[e6d50](https://github.com/JSQLParser/JSqlParser/commit/e6d50f756e99846) Lucas Dillmann *2022-10-25 22:59:09*

**Assorted fixes (#1646)**

* fix: add missing public Getter 
* Add public Getter for &#x60;updateSets&#x60; 
* Fixes #1630 
* fix: Assorted Fixes 
* SelectExpressionItem with Function and Complex Parameters 
* Tables with Oracle DB Links 
* Make Table Name Parts accessible 
* Fixes #1644 
* Fixes #1643 
* fix: Revert correct test case 

[15ff8](https://github.com/JSQLParser/JSqlParser/commit/15ff84348228278) manticore-projects *2022-10-16 20:15:36*

**actualized multiple dependencies**


[34502](https://github.com/JSQLParser/JSqlParser/commit/34502d0e66ad214) Tobias Warneke *2022-09-28 20:17:35*

**Bump h2 from 1.4.200 to 2.1.210 (#1639)**

* Bumps [h2](https://github.com/h2database/h2database) from 1.4.200 to 2.1.210. 
* - [Release notes](https://github.com/h2database/h2database/releases) 
* - [Commits](https://github.com/h2database/h2database/compare/version-1.4.200...version-2.1.210) 
* --- 
* updated-dependencies: 
* - dependency-name: com.h2database:h2 
* dependency-type: direct:development 
* ... 
* Signed-off-by: dependabot[bot] &lt;support@github.com&gt; 
* Signed-off-by: dependabot[bot] &lt;support@github.com&gt; 
* Co-authored-by: dependabot[bot] &lt;49699333+dependabot[bot]@users.noreply.github.com&gt; 

[fc3c4](https://github.com/JSQLParser/JSqlParser/commit/fc3c4cfd6b1eda9) dependabot[bot] *2022-09-28 19:52:31*

**Support BigQuery SAFE_CAST (#1622) (#1634)**

* Co-authored-by: Zhang, Dequn &lt;deqzhang@paypal.com&gt; 

[d9985](https://github.com/JSQLParser/JSqlParser/commit/d9985ae4f559cda) dequn *2022-09-20 18:22:25*

**Support timestamptz dateliteral (#1621)**

* support timestamptz as datetime literal 
* rename test 

[81a64](https://github.com/JSQLParser/JSqlParser/commit/81a648eba8db92d) Todd Pollak *2022-08-31 20:31:44*

**fixes #1617**


[b0aae](https://github.com/JSQLParser/JSqlParser/commit/b0aae378864c6e1) Tobias Warneke *2022-08-31 20:22:25*

**fixes #419**


[427e9](https://github.com/JSQLParser/JSqlParser/commit/427e90f6b861e23) Tobias Warneke *2022-08-31 19:01:57*

**Closes #1604, added simple OVERLAPS support (#1611)**


[236a5](https://github.com/JSQLParser/JSqlParser/commit/236a50b800a4794) Rob Audenaerde *2022-08-16 08:21:03*

**Fixes  PR #1524 support hive alter sql (#1609)**

* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Fixes broken PR #1524 and Commit fb6e950ce0e62ebcd7a44ba9eea679da2b04b2ed 

[2619c](https://github.com/JSQLParser/JSqlParser/commit/2619ce0a6fd8bd5) manticore-projects *2022-08-14 16:29:18*

**#1524  support hive alter sql : ALTER TABLE name ADD COLUMNS (col_spec[, col_spec ...]) (#1605)**

* Co-authored-by: zhum@aotain.com &lt;zm7705264&gt; 

[fb6e9](https://github.com/JSQLParser/JSqlParser/commit/fb6e950ce0e62eb) Zhumin-lv-wn *2022-08-03 20:56:44*

**fixes #1581**


[732e8](https://github.com/JSQLParser/JSqlParser/commit/732e840e99740ff) Tobias Warneke *2022-07-25 06:43:39*

**Using own Feature - constant for "delete with returning" #1597 (#1598)**


[d3218](https://github.com/JSQLParser/JSqlParser/commit/d3218483f7f33ec) gitmotte *2022-07-25 04:55:20*

****


[2f491](https://github.com/JSQLParser/JSqlParser/commit/2f4916d3e512e14) Tobias Warneke *2022-07-22 23:19:59*


## jsqlparser-4.5 (2022-07-22)

### Other changes

**introduced changelog generator**


[e0f0e](https://github.com/JSQLParser/JSqlParser/commit/e0f0eabdfd1e820) Tobias Warneke *2022-07-22 22:47:00*

**fixes #1596**


[60d64](https://github.com/JSQLParser/JSqlParser/commit/60d648397b01c2d) Tobias Warneke *2022-07-22 22:31:12*

**integrated test for #1595**


[b3927](https://github.com/JSQLParser/JSqlParser/commit/b392733f25468f1) Tobias Warneke *2022-07-19 22:04:18*

****


[09830](https://github.com/JSQLParser/JSqlParser/commit/09830c9fb999bc6) Tobias Warneke *2022-07-19 21:44:43*

**reduced time to parse exception to minimize impact on building time**


[191b9](https://github.com/JSQLParser/JSqlParser/commit/191b9fd2c796aa1) Tobias Warneke *2022-07-19 21:40:35*

**add support for drop column if exists (#1594)**


[fcfdf](https://github.com/JSQLParser/JSqlParser/commit/fcfdfb7458fd28f) rrrship *2022-07-19 21:38:40*

**PostgreSQL INSERT ... ON CONFLICT Issue #1551 (#1552)**

* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Support Postgres INSERT ... ON CONFLICT 
* Fixes #1551 
* Refactor UpdateSet.toString(), which is used by Insert and Update 
* Allow KEEP keyword 
* Enables special Oracle Test keywordasidentifier04.sql, now 191 tests succeed 
* Sanitize before push 
* Tweak Grammar in order to survive the Maven Build 
* Ammend the README 
* Move Plugin configuration files to the CONFIG folder (hoping, that Codacy will find it there) 
* Update PMD in the Maven configuration 
* Update PMD in the Maven and Gradle configuration 
* Appease Codacy 
* Co-authored-by: Tobias &lt;t.warneke@gmx.net&gt; 

[5ae09](https://github.com/JSQLParser/JSqlParser/commit/5ae09ad097c7294) manticore-projects *2022-07-19 21:18:02*

**Configurable Parser Timeout via Feature (#1592)**

* Configurable Parser Timeout via Feature 
* Fixes #1582 
* Implement Parser Timeout Feature, e. g. &#x60;CCJSqlParserUtil.parse(sqlStr, parser -&gt; parser.withTimeOut(60000));&#x60; 
* Add a special test failing after a long time only, to test TimeOut vs. Parser Exception 
* Appease Codacy 
* Appease Codacy 
* Co-authored-by: Tobias &lt;t.warneke@gmx.net&gt; 

[74000](https://github.com/JSQLParser/JSqlParser/commit/74000130e850788) manticore-projects *2022-07-19 20:48:49*

**fixes #1590**


[cfba6](https://github.com/JSQLParser/JSqlParser/commit/cfba6e54df4ed58) Tobias Warneke *2022-07-19 20:26:19*

**fixes #1590**


[1abaf](https://github.com/JSQLParser/JSqlParser/commit/1abaf4cdbed1938) Tobias Warneke *2022-07-19 20:17:50*

**extended support Postgres' `Extract( field FROM source)` where `field` is a String instead of a Keyword (#1591)**

* Fixes #1582 
* Amend the ExtractExpression 
* Add Test case for issue #1582 
* Amend the README 

[2b3ce](https://github.com/JSQLParser/JSqlParser/commit/2b3ce25a23b264a) manticore-projects *2022-07-19 19:25:23*

****


[87a37](https://github.com/JSQLParser/JSqlParser/commit/87a37d73f29ff55) Tobias Warneke *2022-07-14 19:30:27*

****


[26545](https://github.com/JSQLParser/JSqlParser/commit/26545484caa9372) Tobias Warneke *2022-07-14 19:23:39*

**Closes #1579. Added ANALYZE <table> support. (#1587)**


[e5c8a](https://github.com/JSQLParser/JSqlParser/commit/e5c8a89ded6d5ca) Rob Audenaerde *2022-07-14 19:22:47*

****


[b4a5c](https://github.com/JSQLParser/JSqlParser/commit/b4a5ce1374ab4f1) Tobias Warneke *2022-07-14 19:01:29*

****


[b08f2](https://github.com/JSQLParser/JSqlParser/commit/b08f205ea573553) Tobias Warneke *2022-07-14 18:56:19*

**Closes #1583:: Implement Postgresql optional TABLE in TRUNCATE (#1585)**

* Closes #1583 
* Closes #1583, removed unnecessary local variable. 
* Closes #1583, proper support for deparsing. 

[26248](https://github.com/JSQLParser/JSqlParser/commit/262482610b80d18) Rob Audenaerde *2022-07-14 18:55:44*

****


[6b242](https://github.com/JSQLParser/JSqlParser/commit/6b2422e9cca5d56) Tobias Warneke *2022-07-14 18:53:41*

**Support table option character set and index options (#1586)**

* Support table option character set and index options 
* Signed-off-by: luofei &lt;luoffei@outlook.com&gt; 
* move test 
* Signed-off-by: luofei &lt;luoffei@outlook.com&gt; 

[27cdf](https://github.com/JSQLParser/JSqlParser/commit/27cdfa9ca1237f6) luofei *2022-07-14 18:46:14*

**corrected a last minute bug**


[afbaf](https://github.com/JSQLParser/JSqlParser/commit/afbaf53f4d5e727) Tobias Warneke *2022-07-09 15:28:17*

**corrected a last minute bug**


[f3d2b](https://github.com/JSQLParser/JSqlParser/commit/f3d2b19dda25d09) Tobias Warneke *2022-07-09 15:25:36*

**corrected a last minute bug**


[8378e](https://github.com/JSQLParser/JSqlParser/commit/8378ea4343e1a97) Tobias Warneke *2022-07-09 15:23:50*

**fixes #1576**


[48ea0](https://github.com/JSQLParser/JSqlParser/commit/48ea0e2238e8186) Tobias Warneke *2022-07-09 14:26:07*

**added simple test for #1580**


[5fdab](https://github.com/JSQLParser/JSqlParser/commit/5fdabf13251b193) Tobias Warneke *2022-07-07 20:13:12*

**disabled test for large cnf expansion and stack overflow problem**


[d3000](https://github.com/JSQLParser/JSqlParser/commit/d30005b4486618b) Tobias Warneke *2022-07-07 19:30:37*

**Add test for LikeExpression.setEscape and LikeExpression.getStringExpression (#1568)**

* Add test for LikeExpression.setEscape and LikeExpression.getStringExpression 
* like + set escape test for $ as escape character 

[bcf6f](https://github.com/JSQLParser/JSqlParser/commit/bcf6ff4157277f9) Caro *2022-07-07 19:27:43*

****


[a8a05](https://github.com/JSQLParser/JSqlParser/commit/a8a05535ca6e7c9) Tobias Warneke *2022-07-06 20:22:51*

**add support for postgres drop function statement (#1557)**


[964fa](https://github.com/JSQLParser/JSqlParser/commit/964fa49ff25cd46) rrrship *2022-07-06 20:06:09*

****


[afbb5](https://github.com/JSQLParser/JSqlParser/commit/afbb595c749d2c2) Tobias Warneke *2022-07-06 19:53:35*

**Add support for Hive dialect GROUPING SETS. (#1539)**

* Add support for Hive GROUPING SETS dialect &#x60;GROUP BY a, b, c GROUPING SETS ((a, b), (a, c))&#x60; 
* Simplify HiveTest::testGroupByGroupingSets. 

[03c58](https://github.com/JSQLParser/JSqlParser/commit/03c58de9d341a13) chenwl *2022-07-06 19:40:41*

**fixes #1566**


[886f0](https://github.com/JSQLParser/JSqlParser/commit/886f06dac867b55) Tobias Warneke *2022-06-28 20:55:12*

**Postgres NATURAL LEFT/RIGHT joins (#1560)**

* Postgres NATURAL LEFT/RIGHT joins 
* Fixes #1559 
* Make NATURAL an optional Join Keyword, which can be combined with LEFT, RIGHT, INNER 
* Add tests 
* Postgres NATURAL LEFT/RIGHT joins 
* Amend readme 
* Revert successful Oracle test 

[74a0f](https://github.com/JSQLParser/JSqlParser/commit/74a0f2fb22e24fe) manticore-projects *2022-06-28 20:15:34*

**compound statement tests (#1545)**


[c1c38](https://github.com/JSQLParser/JSqlParser/commit/c1c38fe26b1fe90) Matthew Rathbone *2022-06-08 19:11:08*

**Allow isolation keywords as column name and aliases (#1534)**


[fc5a9](https://github.com/JSQLParser/JSqlParser/commit/fc5a9a3dbb91e8e) Tomer Shay (Shimshi) *2022-05-19 21:01:44*

**added github action badge**


[e4ec0](https://github.com/JSQLParser/JSqlParser/commit/e4ec041bdcf5683) Tobias *2022-05-16 09:31:36*

**Create maven.yml**

* started maven build using github actions 

[b7e5c](https://github.com/JSQLParser/JSqlParser/commit/b7e5c151df37f5e) Tobias *2022-05-16 09:24:24*

**introduced deparser and toString correction for insert output clause**


[51105](https://github.com/JSQLParser/JSqlParser/commit/5110598f0a2a774) Tobias Warneke *2022-05-15 22:07:19*

**revived compilable status after merge**


[75489](https://github.com/JSQLParser/JSqlParser/commit/75489bfc3a0355c) Tobias Warneke *2022-05-15 21:29:21*

**INSERT with SetOperations (#1531)**

* INSERT with SetOperations 
* Simplify the INSERT production 
* Use SetOperations for Select and Values 
* Better Bracket handling for WITH ... SELECT ... 
* Fixes #1491 
* INSERT with SetOperations 
* Appease Codazy/PMD 
* INSERT with SetOperations 
* Appease Codazy/PMD 
* Update Readme 
* List the changes 
* Minor rephrases 
* Correct the Maven Artifact Example 
* Fix the two test cases (missing white space) 
* Remove unused import 

[b5672](https://github.com/JSQLParser/JSqlParser/commit/b5672c54386cdf8) manticore-projects *2022-05-15 20:29:06*

**#1516 rename without column keyword (#1533)**

* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* &#x60;RENAME ... TO ...&#x60; without &#x60;COLUMN&#x60; keyword 
* Fixes #1516 

[e8f07](https://github.com/JSQLParser/JSqlParser/commit/e8f0750d75e74c7) manticore-projects *2022-05-11 20:44:34*

**Add support for `... ALTER COLUMN ... DROP DEFAULT` (#1532)**


[de0e8](https://github.com/JSQLParser/JSqlParser/commit/de0e8715ad7cab5) manticore-projects *2022-05-11 20:37:08*

****


[81caf](https://github.com/JSQLParser/JSqlParser/commit/81caf3af5eb2762) Tobias Warneke *2022-05-11 20:15:28*

**#1527 DELETE ... RETURNING ... (#1528)**

* #1527 DELETE ... RETURNING ... 
* Fixes #1527 
* Add DELETE... RETURNING ... expression 
* Simplify INSERT ... RETURNING ... expression 
* Simply UPDATE ... RETURNING ... expression 
* TSQL Output Clause 
* According to https://docs.microsoft.com/en-us/sql/t-sql/queries/output-clause-transact-sql?view&#x3D;sql-server-ver15 
* Implement Output Clause for INSERT, UPDATE and DELETE 
* Add Tests according the Microsoft Documentation 
* Appease Codacy/PMD 

[4d815](https://github.com/JSQLParser/JSqlParser/commit/4d8152159454069) manticore-projects *2022-05-11 20:04:23*

**fixs #1520 (#1521)**


[f7f9d](https://github.com/JSQLParser/JSqlParser/commit/f7f9d270b13377d) chiangcho *2022-05-11 19:51:47*

****


[22fef](https://github.com/JSQLParser/JSqlParser/commit/22fef8c95eddbce) Tobias Warneke *2022-05-11 19:45:25*

**Unsupported statement (#1519)**

* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Implement UnsupportedStatement 
* - Add Feature allowUnsupportedStatement, default&#x3D;false 
* - Fully implement UnsupportedStatement for the Statement() production 
* - Partially implement UnsupportedStatement for the Statements() production, works only when UnsupportedStatement comes first 
* Revert unintended changes of the test resources 
* Reformat BLOCK production 
* Disable STATEMENTS() test, which will never fail and add comments to this regard 

[59bb9](https://github.com/JSQLParser/JSqlParser/commit/59bb9a4e40753cf) manticore-projects *2022-05-11 19:23:35*

**fixes #1518**


[bc113](https://github.com/JSQLParser/JSqlParser/commit/bc11309777df6b4) Tobias Warneke *2022-04-26 21:06:44*

**Update bug_report.md (#1512)**

* Focus more on the particular SQL Statement and the JSQLParser Version. 
* Link to the Online Formatter for testing. 

[13441](https://github.com/JSQLParser/JSqlParser/commit/13441f47fbd8023) manticore-projects *2022-04-22 22:29:07*

**changed to allow #1481**


[0cc2a](https://github.com/JSQLParser/JSqlParser/commit/0cc2a29c4d7b3ad) Tobias Warneke *2022-04-22 21:56:27*

**Performance Improvements (#1439)**

* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Performance Improvements 
* Simplify the Primary Expression Production 
* Try to simple parse without Complex Expressions first, before parsing complex and slow (if supported by max nesting depth) 
* Add Test cases for issues #1397 and #1438 
* Update Libraries to its latest version 
* Remove JUnit 4 from Gradle 
* Appease PMD 
* Update Gradle Plugins to its latest versions 
* Let Parser timeout after 6 seconds and fail gently 
* Add a special test verifying the clean up after timeout 
* Revert unintended changes to the Test Resources 
* Appease PMD/Codacy 
* Correct the Gradle &quot;+&quot; dependencies 
* Bump version to 4.4.-SNAPSHOT 
* update build file 
* revert unwarranted changes in test files 
* strip the Exception Class Name from the Message 
* maxDepth &#x3D; 10 collides with the Parser Timeout &#x3D; 6 seconds 
* License Headers 
* Unused imports 
* Bump version to 4.5-SNAPSHOT 
* Reduce test loops to fit intothe timeout 

[181a2](https://github.com/JSQLParser/JSqlParser/commit/181a21ab90870e1) manticore-projects *2022-04-14 21:18:18*


## jsqlparser-4.4 (2022-04-10)

### Other changes

****


[00b24](https://github.com/JSQLParser/JSqlParser/commit/00b2440852b847a) Tobias Warneke *2022-04-09 22:43:11*

**Json function Improvements (#1506)**

* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Improve JSON Functions 
* Space around the &#x60;:&#x60; delimiter of JSON Functions 
* Improve JSON Functions 
* Enforce &#x60;KEY&#x60; as &#x60;S_CHAR_LITERAL&#x60; 
* Allow &#x60;Column&#x60; as &#x60;VALUE&#x60; 
* Temporarily disable Postgres Syntax 
* Improve JSON Functions 
* Bring back Postgres Syntax 
* Enable MySQL Syntax JSON_OBJECT(key, value [, key, value, ...]) 
* Fix some more tests, where key was not a String 
* Appease Codacy 
* Let JSON_OBJECT accept Expressions as value 
* set Version &#x3D; 4.4-SNAPSHOT 

[e3f53](https://github.com/JSQLParser/JSqlParser/commit/e3f531caf7ad9ba) manticore-projects *2022-04-09 22:37:36*

**fixes #1505**


[41c77](https://github.com/JSQLParser/JSqlParser/commit/41c77ca5dd75ae1) Tobias Warneke *2022-04-09 21:16:45*

****


[f3fac](https://github.com/JSQLParser/JSqlParser/commit/f3facb762de3ef7) Tobias Warneke *2022-04-09 20:20:23*

**fixes #1502**


[fea85](https://github.com/JSQLParser/JSqlParser/commit/fea8575fbed4cbb) Tobias Warneke *2022-04-09 20:10:41*

**Issue1500 - Circular References in `AllColumns` and `AllTableColumns` (#1501)**

* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 
* Remove circular reference revealed by issue #1500 
* Add test for Issue 1500 Circular reference for All Columns Expression 
* Fix Test case 
* Add Test for AllTableColumn due to similar circular reference 
* Remove similar circular reference from AllTableColumn 
* Update dependencies 
* Adjust Jacoco Missed Lines count 

[0949d](https://github.com/JSQLParser/JSqlParser/commit/0949df9d789123c) manticore-projects *2022-04-03 18:51:35*

****


[62677](https://github.com/JSQLParser/JSqlParser/commit/62677a68fcc5c34) Tobias Warneke *2022-04-02 23:59:19*

**Optimize assertCanBeParsedAndDeparsed (#1389)**

* Optimize assertCanBeParsedAndDeparsed 
* - Avoid redundant calls of buildSqlString() 
* - Replace String.replaceAll() with Matcher.replaceAll() based on precompiled Regex Patterns 
* Reset the testcase results 

[ea316](https://github.com/JSQLParser/JSqlParser/commit/ea3164a1e418f3b) manticore-projects *2022-04-02 22:40:09*

**Add geometry distance operator (#1493)**

* Add support for geometry distance operators in PostGIS. 
* Fix missing imports. 
* Co-authored-by: Thomas Powell &lt;tpowell@palantir.com&gt; 

[98c47](https://github.com/JSQLParser/JSqlParser/commit/98c476a6c9fa1a1) Thomas Powell *2022-04-02 22:31:08*

**Support WITH TIES option in TOP #1435 (#1479)**

* Support WITH TIES option in TOP 
* - Add the support of WITH TIES option in SELECT TOP statement. 
* add specific test 

[1756a](https://github.com/JSQLParser/JSqlParser/commit/1756adcade48fb4) Olivier Cavadenti *2022-04-02 21:26:54*

**fixes #1482**


[7ddb7](https://github.com/JSQLParser/JSqlParser/commit/7ddb7c8e056be6d) Tobias Warneke *2022-03-15 21:51:56*

**fixes #1482**


[251cc](https://github.com/JSQLParser/JSqlParser/commit/251cc3c09c477d1) Tobias Warneke *2022-03-15 21:48:13*

**Extending CaseExpression, covering #1458 (#1459)**

* Add unit tests for Case expressions. 
* More tests for CaseExpression. 
* Switch expression becomes an Expression instead of a Condition. 
* It allows complex expressions in the switch, similarly to what is allowed in when clauses. 

[4df13](https://github.com/JSQLParser/JSqlParser/commit/4df1391a28a7402) Mathieu Goeminne *2022-03-15 20:07:43*

**fixes #1471**


[3695e](https://github.com/JSQLParser/JSqlParser/commit/3695e0479448ab9) Tobias Warneke *2022-02-18 23:03:24*

**fixes #1471**


[e789c](https://github.com/JSQLParser/JSqlParser/commit/e789c9c7869dc27) Tobias Warneke *2022-02-18 22:32:04*

**fixes #1470**


[c7075](https://github.com/JSQLParser/JSqlParser/commit/c70758266b5af51) Tobias Warneke *2022-02-06 22:21:12*

**Add support for IS DISTINCT FROM clause (#1457)**

* Co-authored-by: Tomer Shay &lt;tomer@Tomers-MBP.lan&gt; 

[31ed3](https://github.com/JSQLParser/JSqlParser/commit/31ed383ff0f3903) Tomer Shay (Shimshi) *2022-01-18 07:01:14*

**fix fetch present in the end of union query (#1456)**


[6e632](https://github.com/JSQLParser/JSqlParser/commit/6e6321481a15965) chiangcho *2022-01-18 07:00:14*

**added SQL_CACHE implementation and changed**


[cf012](https://github.com/JSQLParser/JSqlParser/commit/cf0128ac884c2b8) Tobias Warneke *2022-01-09 12:16:39*

**support for db2 with ru (#1446)**


[3e976](https://github.com/JSQLParser/JSqlParser/commit/3e976528094e646) chiangcho *2021-12-20 22:50:56*

****


[13878](https://github.com/JSQLParser/JSqlParser/commit/1387891cc837f64) Tobias Warneke *2021-12-12 15:37:58*


## jsqlparser-4.3 (2021-12-12)

### Other changes

**updated readme.md to show all changes for version 4.3**


[f0396](https://github.com/JSQLParser/JSqlParser/commit/f039659d1fb5b35) Tobias Warneke *2021-12-12 15:20:32*

**Adjust Gradle to JUnit 5 (#1428)**

* Adjust Gradle to JUnit 5 
* Parallel Test execution 
* Gradle Caching 
* Explicitly request for latest JavaCC 7.0.10 
* Do not mark SpeedTest for concurrent execution 
* Remove unused imports 

[af7bc](https://github.com/JSQLParser/JSqlParser/commit/af7bc1cc06700c3) manticore-projects *2021-11-28 21:43:10*

**corrected some maven plugin versions**


[0acb2](https://github.com/JSQLParser/JSqlParser/commit/0acb28fe33bc7df) Tobias Warneke *2021-11-28 21:40:56*

**fixes #1429**


[bc891](https://github.com/JSQLParser/JSqlParser/commit/bc891e7dcf1d86d) Tobias Warneke *2021-11-23 06:29:25*

**closes #1427**


[46424](https://github.com/JSQLParser/JSqlParser/commit/46424d93784f205) Tobias Warneke *2021-11-21 19:42:11*

**CreateTableTest**


[50ef7](https://github.com/JSQLParser/JSqlParser/commit/50ef7edc3ed6bd6) Tobias Warneke *2021-11-21 12:21:21*

**Support EMIT CHANGES for KSQL (#1426)**

* - Add the EMIT CHANGES syntax used in KSQL. 

[f6c17](https://github.com/JSQLParser/JSqlParser/commit/f6c17412accdd18) Olivier Cavadenti *2021-11-21 12:20:56*

**SelectTest.testMultiPartColumnNameWithDatabaseNameAndSchemaName**


[d8735](https://github.com/JSQLParser/JSqlParser/commit/d873526fe9f9a38) Tobias Warneke *2021-11-21 12:17:29*

**reformatted test source code**


[fb455](https://github.com/JSQLParser/JSqlParser/commit/fb455a7efe1ed04) Tobias Warneke *2021-11-21 12:11:43*

**organize imports**


[31921](https://github.com/JSQLParser/JSqlParser/commit/31921285376bb41) Tobias Warneke *2021-11-21 12:09:26*

**replaced all junit 3 and 4 with junit 5 stuff**


[2c672](https://github.com/JSQLParser/JSqlParser/commit/2c6724769e76429) Tobias Warneke *2021-11-21 12:03:37*

****


[fce5b](https://github.com/JSQLParser/JSqlParser/commit/fce5b9953a5a9c1) Tobias Warneke *2021-11-20 00:08:05*

**Support RESTART without value (#1425)**

* Since Postgre 8.4, RESTART in ALTER SEQUENCE can be set without value. 

[98b66](https://github.com/JSQLParser/JSqlParser/commit/98b66be4b2919df) Olivier Cavadenti *2021-11-20 00:00:32*

**Add support for oracle UnPivot when use multi columns at once. (#1419)**

* Co-authored-by: LeiJun &lt;02280245@yto.net.cn&gt; 

[8e8bb](https://github.com/JSQLParser/JSqlParser/commit/8e8bb708636e6c6) LeiJun *2021-11-19 23:22:29*

****


[1fe92](https://github.com/JSQLParser/JSqlParser/commit/1fe92bc61914135) Tobias Warneke *2021-11-19 22:36:41*

**Fix issue in parsing TRY_CAST() function (#1391)**

* Fix issue in parsing TRY_CAST() function 
* Fix issue in parsing TRY_CAST() function 
* Add parser, deparser, validator and vistior implementation for try_cast function 
* Update toString() method of TryCastExpression class 

[bfcf0](https://github.com/JSQLParser/JSqlParser/commit/bfcf00f9dfcc0a3) Prashant Sutar *2021-11-19 22:24:49*

**fixes #1414**


[93b8c](https://github.com/JSQLParser/JSqlParser/commit/93b8c8b96d5558d) Tobias Warneke *2021-11-19 22:21:21*

**Add support for expressions (such as columns) in AT TIME ZONE expressions (#1413)**

* Co-authored-by: EverSQL &lt;tomer@eversql.com&gt; 

[ebe17](https://github.com/JSQLParser/JSqlParser/commit/ebe171b3b502089) Tomer Shay (Shimshi) *2021-11-19 22:04:40*

**Add supported for quoted cast expressions for PostgreSQL (#1411)**

* Co-authored-by: EverSQL &lt;tomer@eversql.com&gt; 

[dbbce](https://github.com/JSQLParser/JSqlParser/commit/dbbcebbf0490e1c) Tomer Shay (Shimshi) *2021-11-19 21:54:37*

**added USE SCHEMA <schema> and CREATE OR REPLACE <table> support; things that are allowed in Snowflake SQL (#1409)**

* Co-authored-by: Richard Kooijman &lt;richard.kooijman@inergy.nl&gt; 

[f35d2](https://github.com/JSQLParser/JSqlParser/commit/f35d24cfbb88342) Richard Kooijman *2021-11-19 21:40:45*

**Issue #420 Like Expression with Escape Expression (#1406)**

* Issue #420 Like Expression with Escape Expression 
* Fixes issue #420 
* CheckStyle compliance 

[8eaa4](https://github.com/JSQLParser/JSqlParser/commit/8eaa4d2fc243f0a) manticore-projects *2021-11-19 21:38:54*

**fixes #1405 and some junit.jupiter stuff**


[9adab](https://github.com/JSQLParser/JSqlParser/commit/9adab8d059685ff) Tobias Warneke *2021-11-19 21:32:23*

**#1401 add junit-jupiter-api (#1403)**


[80ff5](https://github.com/JSQLParser/JSqlParser/commit/80ff50e0296c107) gitmotte *2021-11-19 21:10:18*

**Support Postgres Dollar Quotes #1372 (#1395)**


[0bd4c](https://github.com/JSQLParser/JSqlParser/commit/0bd4c198e483616) Olivier Cavadenti *2021-11-19 20:32:26*

**Add Delete / Update modifiers for MySQL #1254 (#1396)**

* Add Delete / Update modifiers for MySQL #1254 
* fix codacy issues + pr return 
* simplify low_priority 

[7be5d](https://github.com/JSQLParser/JSqlParser/commit/7be5d8e65e23f6e) Olivier Cavadenti *2021-11-19 20:31:00*

**Fixes #1381 (#1383)**

* Allow Complex Expressions as SelectItem 

[cdf0f](https://github.com/JSQLParser/JSqlParser/commit/cdf0f095294b04a) manticore-projects *2021-11-19 20:27:44*

**Allows CASE ... ELSE ComplexExpression (#1388)**

* Fixes #1375 
* Co-authored-by: Tobias &lt;t.warneke@gmx.net&gt; 

[60a7d](https://github.com/JSQLParser/JSqlParser/commit/60a7d103853cb5e) manticore-projects *2021-11-02 20:48:39*

**IN() with complex expressions (#1384)**

* IN() with Complex Expressions 
* Fixes #905 
* Allow Complex Expressions and multiple SubSelects for the IN() Expression 
* Tune the Test Coverage 
* Remove unused import 
* Reset TEST status 

[c4232](https://github.com/JSQLParser/JSqlParser/commit/c42322440d5fb36) manticore-projects *2021-11-01 21:23:48*

**Fixes #1385 and PR#1380 (#1386)**

* Add another Alias() Keyword related LOOKAHEAD 
* Fix a Keyword Spelling Error in the Deparser 
* Remove UNPIVOT from the PARENTHESIS Deparser, as it was an ugly workaround made obsolete by PR #1380 

[8c1eb](https://github.com/JSQLParser/JSqlParser/commit/8c1eba24be61cf0) manticore-projects *2021-10-22 20:19:15*

**Fixes #1369 (#1370)**

* Issue1369 
* Add test 

[2335e](https://github.com/JSQLParser/JSqlParser/commit/2335ed136e92163) Ben Grabham *2021-10-20 21:44:06*

**Fixes #1371 (#1377)**

* Fixes #1371 
* Postgres specific JSON_OBJECT syntax supporting: 
* SELECT json_object(&#x27;{a, 1, b, 2}&#x27;); 
* SELECT json_object(&#x27;{{a, 1}, {b, 2}}&#x27;); 
* SELECT json_object(&#x27;{a, b}&#x27;, &#x27;{1,2 }&#x27;); 
* Improve Test Coverage 

[cbffe](https://github.com/JSQLParser/JSqlParser/commit/cbffe6b58cd0074) manticore-projects *2021-10-20 21:13:54*

**LIMIT OFFSET with Expressions (#1378)**

* Fixes #933 

[a52db](https://github.com/JSQLParser/JSqlParser/commit/a52db54ff61be34) manticore-projects *2021-10-20 21:05:27*

**Oracle Multi Column Drop (#1379)**

* Fixes #1363 

[9ad18](https://github.com/JSQLParser/JSqlParser/commit/9ad18d29efb66a2) manticore-projects *2021-10-20 21:01:04*

**Support alias for UnPivot statement (see discussion #1374) (#1380)**

* - Changed JSqlParserCC.jjt file to add the alias to the UnPivot lexical entity. 
* - Added Alias to the UnPivot object. 
* - Improved SelectDeParser to correctly deparse SubSelect&#x27;s UnPivot component. 

[0c0c3](https://github.com/JSQLParser/JSqlParser/commit/0c0c32e9cda0f1a) fabriziodelfranco *2021-10-20 20:18:13*

**Issue1352 (#1353)**

* Fixes #1352 
* Allow SYSTEM as table- or column- name 
* Fixes #1352 
* Allow SYSTEM as tablename 
* Fixes #1352 
* Allow SYSTEM as tablename and columnname 
* Fixes #1352 
* Allow QUERY as tablename and columnname 
* Fixes #1352 
* Allow FULLTEXT as tablename and columnname 
* Co-authored-by: Tobias &lt;t.warneke@gmx.net&gt; 

[a8afd](https://github.com/JSQLParser/JSqlParser/commit/a8afd9a4e6a8bc0) manticore-projects *2021-10-09 21:31:45*

**Enhance ALTER TABLE ... DROP CONSTRAINTS ... (#1351)**

* Enhance ALTER TABLE ... DROP CONSTRAINTS ... 
* Add support for DROP PRIMARY KEY, DROP UNIQUE(...) 
* Add support for DROP FOREIGN KEY(...) 
* Fixes #1342 
* Remove one useless PMD rule 
* Add more tests 
* Adjust Test Coverage 

[388b7](https://github.com/JSQLParser/JSqlParser/commit/388b7c3afff4f50) manticore-projects *2021-10-08 23:02:46*

**Function to use AllColumns or AllTableColumns Expression (#1350)**

* Fix a trivial MERGE error from Commit 4797a8d676625fcc6cf8c9e3b403ca120b6a8141 
* Function use AllColumns or AllTableColumns 
* Fixes #1346 
* Remove one useless PMD rule 

[b0ada](https://github.com/JSQLParser/JSqlParser/commit/b0adaa8de1421ea) manticore-projects *2021-10-08 21:58:04*

**Postgres compliant ALTER TABLE ... RENAME TO ... (#1334)**

* Fix a trivial MERGE error from Commit 4797a8d676625fcc6cf8c9e3b403ca120b6a8141 
* Fixes #1333 
* Postgres compliant ALTER TABLE ... RENAME TO ... 
* Postgres compliant ALTER TABLE IF EXISTS ... RENAME TO ... 
* Postgres compliant ALTER TABLE IF EXISTS ... RENAME TO ... 

[f353e](https://github.com/JSQLParser/JSqlParser/commit/f353ec830deb719) manticore-projects *2021-09-18 11:35:17*

**Postgres compliant ALTER TABLE ... RENAME TO ... (#1334)**

* Fix a trivial MERGE error from Commit 4797a8d676625fcc6cf8c9e3b403ca120b6a8141 
* Fixes #1333 
* Postgres compliant ALTER TABLE ... RENAME TO ... 
* Postgres compliant ALTER TABLE IF EXISTS ... RENAME TO ... 
* Postgres compliant ALTER TABLE IF EXISTS ... RENAME TO ... 

[5e904](https://github.com/JSQLParser/JSqlParser/commit/5e9045f431d9cc4) manticore-projects *2021-09-18 11:34:50*

**corrected readme to the new snapshot version**


[b8867](https://github.com/JSQLParser/JSqlParser/commit/b8867d71c0f29d3) Tobias Warneke *2021-09-08 09:57:36*


## jsqlparser-4.2 (2021-09-08)

### Other changes

**introducing test for issue #1328**


[56a39](https://github.com/JSQLParser/JSqlParser/commit/56a39a4693a991f) Tobias Warneke *2021-09-07 09:57:20*

**included some distinct check**


[1264a](https://github.com/JSQLParser/JSqlParser/commit/1264a2033c0062a) Tobias Warneke *2021-09-07 09:49:48*

**corrected a merge bug**


[d53f8](https://github.com/JSQLParser/JSqlParser/commit/d53f8a7fb6b898c) Tobias Warneke *2021-09-07 09:28:35*

**Prepare4.2 (#1329)**

* Implement caching of the Gradle and Maven files 
* Provided by @YunLemon via PR #1307 
* Fix CREATE TABLE AS SELECT ... UNION SELECT ... 
* Provided by @fanchuo via PR #1309 
* Fix #1316 
* Add more specific tests verifying the nature of the UpdateSets 
* Allow &quot;SELECT *&quot; (without FROM) to parse, its a valid SELECT statement 
* Add the enhancements since Release 4.1 
* Adjust the Coverage 
* Improve Test Coverage 
* Revert the Special Oracle Tests (accidentally set to FAILURE) 
* Co-authored-by: Tobias &lt;t.warneke@gmx.net&gt; 

[4797a](https://github.com/JSQLParser/JSqlParser/commit/4797a8d676625fc) manticore-projects *2021-09-07 08:56:12*

**CREATE TABLE AS (...) UNION (...) fails (#1309)**


[a7b5c](https://github.com/JSQLParser/JSqlParser/commit/a7b5c2b078e3ebe) François Sécherre *2021-09-07 08:51:01*

**Fixes #1325 (#1327)**

* Removes redundant production Identifier() and uses RelObjectnameWithoutValue() instead for MS SQL Server Hints 

[cf0d7](https://github.com/JSQLParser/JSqlParser/commit/cf0d74f6572d6aa) manticore-projects *2021-09-06 12:09:01*

**Implement Joins with multiple trailing ON Expressions (#1303)**

* Implement Joins with multiple trailing ON Expressions 
* Fixes #1302 
* Fixes SpecialOracleTest JOIN17, now 190/273 tests pass 
* Fixes #1229 
* Merge MASTER 
* Refactor the appendTo() method in favour of the traditional toString() 
* Remove unused imports 

[d18c5](https://github.com/JSQLParser/JSqlParser/commit/d18c59bf845c57d) manticore-projects *2021-09-06 11:34:05*

**Fix Gradle PMD and Checkstyle (#1318)**

* Fixes #1306 
* Nested Cases with Complex Expressions 
* Reduce coverage for Java 8 
* GROUP BY with Complex Expressions 
* Fixes #1308 
* Update Sets with Complex Expressions 
* Fixes #1316 
* Update Sets with Complex Expressions 
* Fix existing tests 
* Add tests for the new functionality 
* Implement PMD/Codacy recommendations 
* Add Checkstyle Configuration to Gradle 
* Add Checkstyle Config files 
* Fix additional exceptions in Test Sources 

[2e876](https://github.com/JSQLParser/JSqlParser/commit/2e876130b46d087) manticore-projects *2021-09-01 22:01:40*

****


[3f2e7](https://github.com/JSQLParser/JSqlParser/commit/3f2e76cf07ba699) Tobias Warneke *2021-08-28 20:59:29*

**Fixes #1306 (#1311)**

* Fixes #1306 
* Nested Cases with Complex Expressions 
* Reduce coverage for Java 8 
* GROUP BY with Complex Expressions 
* Fixes #1308 

[8f632](https://github.com/JSQLParser/JSqlParser/commit/8f632b92e511b28) manticore-projects *2021-08-28 20:28:55*

**Update sets (#1317)**

* Fixes #1306 
* Nested Cases with Complex Expressions 
* Reduce coverage for Java 8 
* GROUP BY with Complex Expressions 
* Fixes #1308 
* Update Sets with Complex Expressions 
* Fixes #1316 
* Update Sets with Complex Expressions 
* Fix existing tests 
* Add tests for the new functionality 
* Implement PMD/Codacy recommendations 

[21e5e](https://github.com/JSQLParser/JSqlParser/commit/21e5ebac02822e2) manticore-projects *2021-08-27 21:37:05*

****


[50313](https://github.com/JSQLParser/JSqlParser/commit/50313376d5e6554) Tobias Warneke *2021-08-25 21:12:12*

**Special oracle tests (#1279)**

* Allow keywords: LINK, GROUPING() 
* Deparse ParenthesisFromItem&#x27;s Pivot and UnPivot correctly 
* Write Test results to the SQL File 
* Reduce the noise during the test 
* Update/correct the list of expected passing files 
* Get the benchmark from the list of expected passing files 
* There are no Pivots or UnPivots yet, so we will return NULL. 
* Record the expected Test Results on each SQL Source 
* Fail the test when any expected success suddenly fails 
* Improve Test Coverage 
* Appease Codacy 

[346ee](https://github.com/JSQLParser/JSqlParser/commit/346eea5fbcf2461) manticore-projects *2021-08-09 21:55:00*

**Implements Hierarchical CONNECT_BY_ROOT Operator (#1282)**

* Implements Hierarchical CONNECT_BY_ROOT Operator 
* Fixes Issue #1269 
* Resolves some Special Oracle Tests 
* Improve Test Coverage 
* Co-authored-by: Tobias &lt;t.warneke@gmx.net&gt; 

[b6014](https://github.com/JSQLParser/JSqlParser/commit/b60140740aab93e) manticore-projects *2021-08-09 21:53:08*

**Implement Transact-SQL IF ELSE Statement Control Flows. (#1275)**

* Implement Transact-SQL IF ELSE Statement Control Flows. 
* Fixes #1273 except for Blocks. 
* Improce Test Coverage 
* Adjust the required Test Coverage for JDK 8 

[750c3](https://github.com/JSQLParser/JSqlParser/commit/750c30aafe83957) manticore-projects *2021-08-09 21:43:50*

**Add some flexibility to the Alter Statement (#1293)**

* in order to allow: 
* ALTER TABLE ... MOVE TABLESPACE ... 
* ALTER TABLE ... COMPRESS NOLOGGING 
* ALTER TABLE ... ROWFORMAT&#x3D;DYNAMIC 
* Fixes #1033 

[a88e9](https://github.com/JSQLParser/JSqlParser/commit/a88e921970c57b8) manticore-projects *2021-08-02 20:51:19*

**Implement Oracle's Alter System (#1288)**


[4ac5a](https://github.com/JSQLParser/JSqlParser/commit/4ac5ab468b1dd1d) manticore-projects *2021-08-02 20:37:40*

**Implement Oracle Named Function Parameters Func( param1 => arg1, ...) (#1283)**

* Fixes #1270 

[c8a5d](https://github.com/JSQLParser/JSqlParser/commit/c8a5d7c3dfc97f4) manticore-projects *2021-08-02 20:32:41*

**Implement Gradle Buildsystem (#1271)**

* Gradle build 
* implement SpotBugs, PMD and JaCoCo 
* implement RR diagrams 
* Move Special Oracle Test resources into the correct package 
* Implement a basic Gradle/Maven compatibility workaround for the Special Oracle Test 
* Fix the Gradle Wrapper and add the folder to git 

[6933d](https://github.com/JSQLParser/JSqlParser/commit/6933d86e0fa2d48) manticore-projects *2021-08-02 20:18:48*

**fixes #1272**


[48a11](https://github.com/JSQLParser/JSqlParser/commit/48a1133ced7dd19) Tobias Warneke *2021-07-26 21:14:20*

**Allowes JdbcParameter or JdbcNamedParameter for MySQL FullTextSearch (#1278)**

* Fixes issue #1223 

[e6c91](https://github.com/JSQLParser/JSqlParser/commit/e6c91b6a813a1e0) manticore-projects *2021-07-26 21:06:38*

**Fixes #1267 Cast into RowConstructor (#1274)**

* Fixes #1267 Cast into RowConstructor 
* Improve Test Coverage 
* Improve Test Coverage 
* Improve Test Coverage 
* Co-authored-by: Tobias &lt;t.warneke@gmx.net&gt; 

[c89cf](https://github.com/JSQLParser/JSqlParser/commit/c89cf21641d672b) manticore-projects *2021-07-26 21:02:19*

****


[fecc9](https://github.com/JSQLParser/JSqlParser/commit/fecc95d0ee93100) Tobias Warneke *2021-07-26 20:47:58*

**Separate MySQL Special String Functions accepting Named Argument Separation as this could collide with ComplexExpressionList when InExpression is involved (#1285)**

* Fixes #1284 
* Co-authored-by: Tobias &lt;t.warneke@gmx.net&gt; 

[c074a](https://github.com/JSQLParser/JSqlParser/commit/c074a21ad70dd0e) manticore-projects *2021-07-26 20:34:06*

**Implements Oracle RENAME oldTable TO newTable Statement (#1286)**

* Implements Oracle RENAME oldTable TO newTable Statement 
* Fixes #1253 
* Implement MariaDB specific syntax 
* Remove redundant License Headers 
* Use LinkedHashMap to preserve the order of the Entries 
* Increase Test Coverage 
* Co-authored-by: Tobias &lt;t.warneke@gmx.net&gt; 

[f7f7b](https://github.com/JSQLParser/JSqlParser/commit/f7f7bcd65be8f4c) manticore-projects *2021-07-26 20:26:47*

**Implement Oracle Purge Statement (#1287)**


[f86bc](https://github.com/JSQLParser/JSqlParser/commit/f86bc2e432305fb) manticore-projects *2021-07-26 20:18:39*

**included jacoco to allow code coverage for netbeans**


[f85b4](https://github.com/JSQLParser/JSqlParser/commit/f85b4b630008d9e) Tobias Warneke *2021-07-18 21:15:14*

**corrected a Lookahead problem**


[db90e](https://github.com/JSQLParser/JSqlParser/commit/db90e74f1d5d6e8) Tobias Warneke *2021-07-16 21:31:17*

**Json functions (#1263)**

* Implement Json Aggregate Functions JSON_OBJECTAGG() and JSON_ARRAYAGG() 
* fix the returned type 
* Implement JSON_OBJECT and JSON_ARRAY 
* Solves #1260 and dbeaver/dbeaver/#13141 
* Better workaround for NULL, NULL NULL ON NULL 
* Remove the workaround for NULL ON NULL (without expression) 
* Implement &quot;PMD.MissingBreakInSwitch&quot; in order to appease Codacy 
* Improve Test Coverage 
* Improve Test Coverage 
* KEYs can be SQL Value Expressions 
* Add another testcase 

[59bf0](https://github.com/JSQLParser/JSqlParser/commit/59bf07f5425ecb4) manticore-projects *2021-07-16 21:24:44*

**fixes #1255**


[2c732](https://github.com/JSQLParser/JSqlParser/commit/2c732ad9bfbe34e) Tobias Warneke *2021-07-16 21:12:45*

**Active JJDoc and let it create the Grammar BNF documentation (#1256)**

* Clean-up the Site generation 

[1ecff](https://github.com/JSQLParser/JSqlParser/commit/1ecffd2c1a6e8d8) manticore-projects *2021-07-16 20:38:05*

**Bump commons-io from 2.6 to 2.7 (#1265)**

* Bumps commons-io from 2.6 to 2.7. 
* --- 
* updated-dependencies: 
* - dependency-name: commons-io:commons-io 
* dependency-type: direct:development 
* ... 
* Signed-off-by: dependabot[bot] &lt;support@github.com&gt; 
* Co-authored-by: dependabot[bot] &lt;49699333+dependabot[bot]@users.noreply.github.com&gt; 

[09898](https://github.com/JSQLParser/JSqlParser/commit/09898107fe6d001) dependabot[bot] *2021-07-14 05:27:57*

**Update README.md**


[e6303](https://github.com/JSQLParser/JSqlParser/commit/e630354df1cd0f4) Tobias *2021-07-13 05:34:12*

**Implement DB2 Special Register Date Time CURRENT DATE and CURRENT TIME (#1252)**

* Implement DB2 Special Register Date Time CURRENT DATE and CURRENT TIME 
* Fixes issue #1249 
* (Although there are more Special Registers which are not supported yet.) 
* Make the spaces mandatory 
* Add 2 more tests 

[05157](https://github.com/JSQLParser/JSqlParser/commit/05157a841897033) manticore-projects *2021-07-13 05:32:38*

**Rename the PMD ruleset configuration file hoping for automatic synchronization with Codacy (#1251)**

* Solves Issue #1220 

[930b7](https://github.com/JSQLParser/JSqlParser/commit/930b7a561876b7e) manticore-projects *2021-07-13 05:28:43*

**corrected .travis.yml**


[11124](https://github.com/JSQLParser/JSqlParser/commit/111244ab565a160) Tobias Warneke *2021-07-05 20:57:22*

**corrected .travis.yml**


[5cf7e](https://github.com/JSQLParser/JSqlParser/commit/5cf7eeaa6f7074d) Tobias Warneke *2021-07-05 20:52:40*

****


[3e0e7](https://github.com/JSQLParser/JSqlParser/commit/3e0e7f38a11fe21) Tobias Warneke *2021-07-05 20:39:11*

**Update README.md**


[ec16d](https://github.com/JSQLParser/JSqlParser/commit/ec16d1f68c83063) Tobias *2021-07-05 20:27:41*

**fixes #1250**


[9e434](https://github.com/JSQLParser/JSqlParser/commit/9e4341a26be1f7f) Tobias Warneke *2021-07-01 12:24:36*

****


[05d68](https://github.com/JSQLParser/JSqlParser/commit/05d684416f338b6) Tobias Warneke *2021-06-30 22:12:00*


## jsqlparser-4.1 (2021-06-30)

### Other changes

**fixes #1140**


[1a917](https://github.com/JSQLParser/JSqlParser/commit/1a9173f7d65a026) Tobias Warneke *2021-06-30 21:50:28*

**introduced #1248 halfway**


[1deea](https://github.com/JSQLParser/JSqlParser/commit/1deeab8b349343f) Tobias Warneke *2021-06-30 21:15:56*

**Savepoint rollback (#1236)**

* Implement SAVEPOINT and ROLLBACK statements, fixes issue #1235 
* Activate a test which is supported now. 

[2cae6](https://github.com/JSQLParser/JSqlParser/commit/2cae62dbf74a744) manticore-projects *2021-06-30 19:57:56*

**Fixes Function Parameter List Brackets issue #1239 (#1240)**


[750f3](https://github.com/JSQLParser/JSqlParser/commit/750f3d1e28fa0d9) manticore-projects *2021-06-30 19:45:32*

****


[0ba44](https://github.com/JSQLParser/JSqlParser/commit/0ba44c59fcea1cc) Tobias Warneke *2021-06-30 19:41:44*

**corrected javadoc problem**


[f7ea4](https://github.com/JSQLParser/JSqlParser/commit/f7ea4a5040ac7c7) Tobias Warneke *2021-06-27 00:20:35*

**corrected some lookahead problem**


[59c2a](https://github.com/JSQLParser/JSqlParser/commit/59c2a9432ee23d7) Tobias Warneke *2021-06-26 23:57:06*

**RESET statement, SET PostgreSQL compatibility (#1104)**

* Support 
* RESET statement  (https://www.postgresql.org/docs/current/sql-reset.html) 
* SET [LOCAL|SESSION]  (https://www.postgresql.org/docs/current/sql-set.html) 
* SET search_path&#x3D;my_schema, public ( https://www.postgresql.org/docs/current/sql-set.html 
* value New value of parameter. Values can be specified as string constants, identifiers, numbers, or comma-separated lists of these) 
* Update ResetStatementTest.java 
* remove Tim Zone token 
* Co-authored-by: Tobias &lt;t.warneke@gmx.net&gt; 

[13503](https://github.com/JSQLParser/JSqlParser/commit/13503edff30f06d) Роман Зотов *2021-06-26 22:44:33*

**corrected some lookahead problem**


[6711f](https://github.com/JSQLParser/JSqlParser/commit/6711f6043c168f7) Tobias Warneke *2021-06-26 22:42:24*

**Implement Oracle Alter Session Statements (#1234)**

* Implement Oracle Alter Session Statements according to https://docs.oracle.com/cd/B19306_01/server.102/b14200/statements_2012.htm 
* Implement PMD Rule &quot;SwitchStmtsShouldHaveDefault&quot; 
* Reorganize Test Case imports 

[3a46a](https://github.com/JSQLParser/JSqlParser/commit/3a46a29d6936611) manticore-projects *2021-06-26 22:38:19*

**fixes #1230**


[3082d](https://github.com/JSQLParser/JSqlParser/commit/3082de3a689c88e) Tobias Warneke *2021-06-26 22:31:43*

**Support DELETE FROM T1 USING T2 WHERE ... (#1228)**

* Co-authored-by: Francois Secherre &lt;secherre.nospam@gmail.com&gt; 

[96cd4](https://github.com/JSQLParser/JSqlParser/commit/96cd483ab85783d) francois-secherre *2021-06-16 05:27:14*

**Row access support (#1181)**

* Row acess support 
* Remove IN Left Expression List, replaced by RowConstructor Expression 
* Remove IN Left Expression List, replaced by RowConstructor Expression 
* Formatting 

[27e6a](https://github.com/JSQLParser/JSqlParser/commit/27e6a9f0e07320e) Роман Зотов *2021-06-16 05:15:47*

**corrected lookahead problem of PR #1225**


[aec76](https://github.com/JSQLParser/JSqlParser/commit/aec76eae23f1edd) Tobias Warneke *2021-06-14 05:27:40*

**Delete queries without from, with a schema identifier fails (#1224)**

* Delete queries without from, with a schema identifier fails 
* Better tests 
* Fix style issue 
* Deparse should match for DELETE WITHOUT FROM queries 
* Co-authored-by: François Sécherre &lt;francois.secherre@ouicar.fr&gt; 

[d70e1](https://github.com/JSQLParser/JSqlParser/commit/d70e151c0f22f22) François Sécherre *2021-06-14 05:15:15*

**Create temporary table t(c1, c2) as select ... (#1225)**

* Co-authored-by: Francois Secherre &lt;secherre.nospam@gmail.com&gt; 

[b62f1](https://github.com/JSQLParser/JSqlParser/commit/b62f19feb3b497c) francois-secherre *2021-06-14 05:13:13*

**Nested with items (#1221)**

* Nested WithItems, fixes issue #1186 
* Remove redundant Test 
* Avoid altering the nb-configuration 
* Mention Nested WITH CTEs in the readme 
* Eliminate dead/unused MultiExpression Code 

[8eb3d](https://github.com/JSQLParser/JSqlParser/commit/8eb3d9a586a182e) manticore-projects *2021-06-10 05:50:08*

**Implement GROUP BY () without columns (#1218)**

* Implement GROUP BY () without columns 
* Migrate GroupByElement to ExpressionList 
* Also solves issue #1210 automatically 
* Solves issue #1168, add a test for it. 

[999db](https://github.com/JSQLParser/JSqlParser/commit/999db01658c30f9) manticore-projects *2021-06-03 05:55:35*

**TSQL Compliant NEXT VALUE FOR sequence_id (but keeping the spurious NEXTVAL FOR expression) (#1216)**

* Co-authored-by: Tobias &lt;t.warneke@gmx.net&gt; 

[7c212](https://github.com/JSQLParser/JSqlParser/commit/7c21242e893abcd) manticore-projects *2021-06-02 12:35:03*

**Pmd clean up (#1215)**

* Add PMD Annotations in order to avoid useless exceptions for the Deparsers 
* Add Eclipse Formatter configuration 
* Fix typo 
* Replace Comments on empty methods with Class wide PMD Annotation 
* Do not enforce checkstyle formatting 

[53764](https://github.com/JSQLParser/JSqlParser/commit/537649bf28f641b) manticore-projects *2021-06-02 12:19:32*

**Add support for boolean 'XOR' operator (#1193)**

* Add support for boolean &#x27;XOR&#x27; operator 
* XorExpression added to the ReflectionModelTest 
* XorExpression case added to the SelectTest 
* XorExpression cases added for Validation 
* Additional tests added for code coverage. 
* Code style fixed. 
* Separate test case for XOR added. 
* Imports explicitly added to avoid namespace pollution. 
* Additional tests cases for precedence and associativity. 
* Co-authored-by: Szabó Miklós &lt;miklos.szabo@arh.hu&gt; 

[c7832](https://github.com/JSQLParser/JSqlParser/commit/c7832402dded4c0) Adaptive Recognition *2021-06-02 12:10:56*

**Update README.md**


[1feea](https://github.com/JSQLParser/JSqlParser/commit/1feea013d865f58) Tobias *2021-05-31 08:46:48*

**Implement WITH for DELETE, UPDATE and MERGE statements (#1217)**


[63ed1](https://github.com/JSQLParser/JSqlParser/commit/63ed1a2f42d14e9) manticore-projects *2021-05-31 08:44:08*

****


[9184c](https://github.com/JSQLParser/JSqlParser/commit/9184cda558e60a5) Tobias Warneke *2021-05-26 23:12:05*

****


[f83ed](https://github.com/JSQLParser/JSqlParser/commit/f83ed0a3a30f6d4) Tobias Warneke *2021-05-26 23:09:08*

****


[76bff](https://github.com/JSQLParser/JSqlParser/commit/76bff01b6b4450f) Tobias Warneke *2021-05-26 23:04:41*

**increases complex scanning range**


[13b88](https://github.com/JSQLParser/JSqlParser/commit/13b88f7f511107e) Tobias Warneke *2021-05-26 21:38:44*

**Allow Complex Parsing of Functions (#1200)**

* Allow Complex Parsing of Functions 
* Fixes issues #1190 #1103 
* Apply Complex Parsing to PrimaryExpression() 
* Fixes issue #1194 
* Increase Test Timeout to 2 seconds for slow CI Servers. 
* Appease Codazy 

[3a5da](https://github.com/JSQLParser/JSqlParser/commit/3a5da445ea9cb85) manticore-projects *2021-05-26 20:35:10*

**Add support for AT TIME ZONE expressions (#1196)**

* Add support for AT TIME ZONE expressions 
* adding tests 
* Fixing imports 

[a5204](https://github.com/JSQLParser/JSqlParser/commit/a5204f63ccb1ea8) Tomer Shay (Shimshi) *2021-05-25 23:03:31*

**fixes #1211**


[ed089](https://github.com/JSQLParser/JSqlParser/commit/ed089f1f800ece1) Tobias Warneke *2021-05-25 21:52:01*

**fixes #1212**


[b8ee7](https://github.com/JSQLParser/JSqlParser/commit/b8ee7526375c9d9) Tobias Warneke *2021-05-25 21:45:01*

****


[1c2ac](https://github.com/JSQLParser/JSqlParser/commit/1c2ac7a4e998e76) Tobias Warneke *2021-05-25 21:32:45*

****


[68695](https://github.com/JSQLParser/JSqlParser/commit/686958cf0bd758b) Tobias Warneke *2021-05-25 19:42:23*

**Fix Nested CASE WHEN performance, fixes issue #1162 (#1208)**

* Fix Nested CASE WHEN performance, fixes issue #1162 
* Apease Codazy 
* Apease Codazy 

[42610](https://github.com/JSQLParser/JSqlParser/commit/426102e4cf272ca) manticore-projects *2021-05-25 19:26:30*

**Add support for casts in json expressions (#1189)**


[86b61](https://github.com/JSQLParser/JSqlParser/commit/86b613c72428e0c) Tomer Shay (Shimshi) *2021-05-10 20:00:59*

**fixes #1185**


[2320b](https://github.com/JSQLParser/JSqlParser/commit/2320b1baa6e1dea) Tobias Warneke *2021-05-04 21:11:12*

****


[53745](https://github.com/JSQLParser/JSqlParser/commit/537452dda91bcc3) Tobias Warneke *2021-05-01 20:57:01*

****


[002f5](https://github.com/JSQLParser/JSqlParser/commit/002f5966c577366) Tobias Warneke *2021-05-01 19:57:10*

**supporting/fixing unique inside sql function such as count eg - SELECT count(UNIQUE col2) FROM mytable (#1184)**

* Co-authored-by: Adhikesavan &lt;radhikesavan@paypal.com&gt; 

[f18e9](https://github.com/JSQLParser/JSqlParser/commit/f18e92eaf4b3bc6) RajaSudharsan Adhikesavan *2021-05-01 19:28:05*

**Oracle compliant ALTER TABLE ADD/MODIFY deparser (#1163)**

* javadoc-fixes 
* fix check-style error : assignment to parameter not allowed 
* import for javadoc reference 
* javadoc - add description to parameter &quot;fqn&quot; (fix warning) 
* remove doclint&#x3D;none, but exclude package with exclude package with 
* generated sources (javacc/jjtree) from javadoc 
* Implement Oracle Hints for INSERT, UPDATE, MERGE, DELETE 
* Correct CreateIndex TailOptions 
* Add a Test Case for CreateIndex TailOptions 
* Add WHERE expression to MergeInsert 
* Add test case for MergeInsert WHERE expression 
* Fix Issue #1156: ALTER TABLE ADD FOREIGN KEY with schema reference 
* Add a specific test case 
* Fix Issue #1157: Oracle does not accept COLUMN keyword in ALTER TABLE ADD/MODIFY 
* Correct the test cases accepting a non existing COLUMN keyword 
* Add a specific test cases 
* Fix Issue #1164 UNIQUE after PRIMARY KEY 
* Add test case for UNIQUE after PRIMARY KEY 
* Switch of warnings for un-fixble method namings 
* Switch of warnings for un-fixble method namings 
* Activate PMD and define our own ruleset 
* Execute PMD before building/testing in order to fail early 
* Fix 63 PMD warnings 
* Activate rule &quot;PMD.CyclomaticComplexity&quot; in order to simulate the Codazy checks 
* Apply @SuppressWarnings({&quot;PMD.CyclomaticComplexity&quot;}) where this rule throws an unavoidable warning (especially for toString() and deparse()) 
* Activate rule , &quot;PMD.ExcessiveMethodLength&quot; in order to simulate the Codazy checks 
* Apply @SuppressWarnings({&quot;PMD.ExcessiveMethodLength&quot;}) where this rule throws an unavoidable warning (especially for toString() and deparse()) 
* Refactor an ENUM name 
* Refactor an ENUM name and reflect this also in the JavaCC Parser definition file 
* Co-authored-by: gitmotte &lt;www@synbee.at&gt; 

[83837](https://github.com/JSQLParser/JSqlParser/commit/838379f21be0d32) manticore-projects *2021-04-21 07:55:17*

**Pmd (#1165)**

* Implement Oracle Hints for INSERT, UPDATE, MERGE, DELETE 
* Correct CreateIndex TailOptions 
* Add a Test Case for CreateIndex TailOptions 
* Add WHERE expression to MergeInsert 
* Add test case for MergeInsert WHERE expression 
* Fix Issue #1156: ALTER TABLE ADD FOREIGN KEY with schema reference 
* Add a specific test case 
* Fix Issue #1157: Oracle does not accept COLUMN keyword in ALTER TABLE ADD/MODIFY 
* Correct the test cases accepting a non existing COLUMN keyword 
* Add a specific test cases 
* Fix Issue #1164 UNIQUE after PRIMARY KEY 
* Add test case for UNIQUE after PRIMARY KEY 
* Switch of warnings for un-fixble method namings 
* Switch of warnings for un-fixble method namings 
* Activate PMD and define our own ruleset 
* Execute PMD before building/testing in order to fail early 
* Fix 63 PMD warnings 
* Activate rule &quot;PMD.CyclomaticComplexity&quot; in order to simulate the Codazy checks 
* Apply @SuppressWarnings({&quot;PMD.CyclomaticComplexity&quot;}) where this rule throws an unavoidable warning (especially for toString() and deparse()) 
* Activate rule , &quot;PMD.ExcessiveMethodLength&quot; in order to simulate the Codazy checks 
* Apply @SuppressWarnings({&quot;PMD.ExcessiveMethodLength&quot;}) where this rule throws an unavoidable warning (especially for toString() and deparse()) 
* Refactor an ENUM name 
* Refactor an ENUM name and reflect this also in the JavaCC Parser definition file 

[08cfd](https://github.com/JSQLParser/JSqlParser/commit/08cfd29459044c6) manticore-projects *2021-04-20 08:01:48*

**function order by support (#1108)**


[d6ef7](https://github.com/JSQLParser/JSqlParser/commit/d6ef7b995134594) Роман Зотов *2021-04-20 04:16:03*

**fixes #1159**


[79e2f](https://github.com/JSQLParser/JSqlParser/commit/79e2f587ee11297) Tobias Warneke *2021-04-16 23:51:24*

**added improvements of pr to readme**


[0ef4a](https://github.com/JSQLParser/JSqlParser/commit/0ef4a5c8a105a44) Tobias Warneke *2021-04-16 23:02:51*

**Assorted fixes to the Java CC Parser definition (#1153)**

* Implement Oracle Hints for INSERT, UPDATE, MERGE, DELETE 
* Correct CreateIndex TailOptions 
* Add a Test Case for CreateIndex TailOptions 
* Add WHERE expression to MergeInsert 
* Add test case for MergeInsert WHERE expression 
* Fix Issue #1156: ALTER TABLE ADD FOREIGN KEY with schema reference 
* Add a specific test case 

[5ee6e](https://github.com/JSQLParser/JSqlParser/commit/5ee6ec9dd7a66bf) manticore-projects *2021-04-16 22:51:27*

****


[b880e](https://github.com/JSQLParser/JSqlParser/commit/b880e1663ca607f) Tobias Warneke *2021-04-16 22:49:29*

**fixes #1138**


[e95f6](https://github.com/JSQLParser/JSqlParser/commit/e95f6ce1c5ecca8) Tobias Warneke *2021-04-10 21:36:07*

**fixes #1138**


[cb7a0](https://github.com/JSQLParser/JSqlParser/commit/cb7a018a8c0cd9e) Tobias Warneke *2021-04-10 21:35:53*

**fixes #1137**


[9d676](https://github.com/JSQLParser/JSqlParser/commit/9d676b90c6c1e30) Tobias Warneke *2021-04-10 21:23:00*

**fixes #1136**


[ba9b8](https://github.com/JSQLParser/JSqlParser/commit/ba9b8d7a6f24274) Tobias Warneke *2021-04-10 21:09:19*

****


[f0bec](https://github.com/JSQLParser/JSqlParser/commit/f0bec22644f99e7) Tobias Warneke *2021-03-22 21:21:13*

**issue #1134 adressed**


[5d9f4](https://github.com/JSQLParser/JSqlParser/commit/5d9f4fdff2e6ce6) Tobias Warneke *2021-03-20 21:35:05*

**Add support for union_with_brackets_and_orderby (#1131)**


[c3a1a](https://github.com/JSQLParser/JSqlParser/commit/c3a1aa688442c1d) Tomer Shay (Shimshi) *2021-03-14 20:10:33*

**Add support for union without brackets and with limit (#1132)**

* add support for union without brackets and with limit 
* Fixing the last commit. 

[56c2d](https://github.com/JSQLParser/JSqlParser/commit/56c2dfe332b5ee8) Tomer Shay (Shimshi) *2021-03-14 20:09:12*

****


[d2f61](https://github.com/JSQLParser/JSqlParser/commit/d2f61d138c2c0e3) Tobias Warneke *2021-03-14 20:04:28*

**Add support for functions in an interval expression (#1099)**


[7f8b5](https://github.com/JSQLParser/JSqlParser/commit/7f8b58c1e32a919) Tomer Shay (Shimshi) *2021-03-14 19:55:40*

****


[bcc68](https://github.com/JSQLParser/JSqlParser/commit/bcc683ab8791b71) Tobias Warneke *2021-02-06 20:37:50*

****


[7deb6](https://github.com/JSQLParser/JSqlParser/commit/7deb6d9bde678d6) Tobias Warneke *2021-02-05 21:59:11*

**subArray support arr[1:3] (#1109)**


[cde50](https://github.com/JSQLParser/JSqlParser/commit/cde50692c131fff) Роман Зотов *2021-02-05 21:55:29*

**bug fix (#769)**

* Co-authored-by: Kunal Jha &lt;kjha@zalando-11116.corp.ad.zalando.net&gt; 

[7234d](https://github.com/JSQLParser/JSqlParser/commit/7234de1d65ccf1b) Kunal jha *2021-02-05 17:46:15*

****


[089fc](https://github.com/JSQLParser/JSqlParser/commit/089fc44eba26cb1) Tobias Warneke *2021-02-04 19:47:39*

**Array contructor support (#1105)**

* Array contructor support array[[1, 2], [id1, id2]] 
* ARRAYLITERAL-&gt;ARRAY_LITERAL 
* Fix empty array 
* Support ARRAY as DEFAULT value in CREATE TABLE 
* https://github.com/JSQLParser/JSqlParser/issues/970#issue-594819872 
* fix empty Array 

[43c28](https://github.com/JSQLParser/JSqlParser/commit/43c282deaa05555) Роман Зотов *2021-02-04 19:28:49*

****


[85193](https://github.com/JSQLParser/JSqlParser/commit/8519356a4939816) Tobias Warneke *2021-01-31 22:59:20*

**Partial support construct tuple as simple expression (#1107)**

* SELECT (1,2) 

[2065f](https://github.com/JSQLParser/JSqlParser/commit/2065fedb5b4c318) Роман Зотов *2021-01-31 22:58:08*

**support create table parameters without columns, parameter values any names (#1106)**

* CREATE TEMPORARY TABLE t1 WITH (APPENDONLY&#x3D;true,ORIENTATION&#x3D;column,COMPRESSTYPE&#x3D;zlib,OIDS&#x3D;FALSE) ON COMMIT DROP AS SELECT column FROM t2 

[f2e74](https://github.com/JSQLParser/JSqlParser/commit/f2e74f15cd63d87) Роман Зотов *2021-01-31 22:53:09*

**fixes #995**


[d7b46](https://github.com/JSQLParser/JSqlParser/commit/d7b468a651ab966) Tobias Warneke *2021-01-13 19:21:40*

**fixes #1100**


[ea31a](https://github.com/JSQLParser/JSqlParser/commit/ea31a0480cea00e) Tobias Warneke *2021-01-13 18:58:16*

**next correction of parenthesis around unions**


[5706f](https://github.com/JSQLParser/JSqlParser/commit/5706fb4e1133884) Tobias Warneke *2021-01-11 23:15:21*

****


[e2ff2](https://github.com/JSQLParser/JSqlParser/commit/e2ff225132bfedd) Tobias Warneke *2021-01-10 00:13:16*

**fixes #992**


[9fd11](https://github.com/JSQLParser/JSqlParser/commit/9fd11563fc86281) Tobias Warneke *2021-01-07 22:34:40*

**corrected patch for case as table name**


[f2638](https://github.com/JSQLParser/JSqlParser/commit/f2638ead2f189c2) Tobias Warneke *2021-01-07 21:57:52*

**Added support for the Case keyword in table names (#1093)**


[b85a2](https://github.com/JSQLParser/JSqlParser/commit/b85a2003bbd9a1a) Tomer Shay (Shimshi) *2021-01-07 21:43:11*

****


[9785b](https://github.com/JSQLParser/JSqlParser/commit/9785b09cd774e14) Tobias Warneke *2021-01-04 06:45:18*

**corrected some javadoc parameter**


[449b7](https://github.com/JSQLParser/JSqlParser/commit/449b74a67c50700) Tobias Warneke *2021-01-03 22:16:44*

**added missing pivot test files**


[e599b](https://github.com/JSQLParser/JSqlParser/commit/e599bd957b2a70f) Tobias Warneke *2021-01-03 21:07:06*

**fixes #282 - first refactoring to allow with clause as a start in insert and update**


[d4402](https://github.com/JSQLParser/JSqlParser/commit/d4402df3ef4978d) Tobias Warneke *2021-01-02 23:57:26*

**fixes #282 - first refactoring to allow with clause as a start in insert and update**


[e6d65](https://github.com/JSQLParser/JSqlParser/commit/e6d65ab3808b9cc) Tobias Warneke *2021-01-02 23:56:12*

**Update README.md**


[54708](https://github.com/JSQLParser/JSqlParser/commit/5470880139cdc94) Tobias *2021-01-02 08:58:01*

**fixes #887**


[74aab](https://github.com/JSQLParser/JSqlParser/commit/74aab7cc81f5665) Tobias Warneke *2021-01-02 00:04:43*

**fixes #1091 - added H2 casewhen function with conditional parameters**


[085e1](https://github.com/JSQLParser/JSqlParser/commit/085e120d4221f1a) Tobias Warneke *2021-01-01 23:49:30*

**fixes #1091 - added H2 casewhen function with conditional parameters**


[e5e7d](https://github.com/JSQLParser/JSqlParser/commit/e5e7d376fc641a9) Tobias Warneke *2021-01-01 23:46:03*

****


[1eaa1](https://github.com/JSQLParser/JSqlParser/commit/1eaa1ba88028f79) Tobias Warneke *2021-01-01 23:18:55*


## jsqlparser-4.0 (2021-01-01)

### Other changes

**fixes #961 - allow unsigned as type**


[7783e](https://github.com/JSQLParser/JSqlParser/commit/7783e05d024b006) Tobias Warneke *2020-12-31 01:49:42*

**fixes #961 - allow unsigned as type**


[67a33](https://github.com/JSQLParser/JSqlParser/commit/67a331e0052527a) Tobias Warneke *2020-12-31 01:46:19*

**fixes #1006 - included limit / offset test**


[b5514](https://github.com/JSQLParser/JSqlParser/commit/b55141b0bdb975d) Tobias Warneke *2020-12-31 00:59:36*

**fixes #1013 - refactored fromitem grammar to drastically improve performance**


[36d0b](https://github.com/JSQLParser/JSqlParser/commit/36d0b7420fe9225) Tobias Warneke *2020-12-31 00:47:09*

****


[05a6f](https://github.com/JSQLParser/JSqlParser/commit/05a6f4b3cb719e6) Tobias Warneke *2020-12-30 23:12:55*

****


[b2c60](https://github.com/JSQLParser/JSqlParser/commit/b2c6097ab0a713e) Tobias Warneke *2020-12-30 22:50:48*

**fixes #1088 - allowed CURRENT as jdbc named parameter name**


[4f925](https://github.com/JSQLParser/JSqlParser/commit/4f925c54cd89e12) Tobias Warneke *2020-12-30 22:12:28*

**fixes #1089 - just included test case**


[4183a](https://github.com/JSQLParser/JSqlParser/commit/4183ae0a55a9455) Tobias Warneke *2020-12-30 21:45:32*

****


[f484d](https://github.com/JSQLParser/JSqlParser/commit/f484de7c4813cf9) Tobias Warneke *2020-12-18 22:15:09*

****


[10a69](https://github.com/JSQLParser/JSqlParser/commit/10a69a2dd873053) Tobias Warneke *2020-12-18 22:13:16*

**fixes #1080**


[d9282](https://github.com/JSQLParser/JSqlParser/commit/d928268fb61c088) Tobias Warneke *2020-12-17 20:17:14*

**Update README.md**


[624f2](https://github.com/JSQLParser/JSqlParser/commit/624f247eaab9a54) Tobias *2020-12-16 10:08:46*

**fixes #926**


[97ab8](https://github.com/JSQLParser/JSqlParser/commit/97ab8e9b6cf4af8) Tobias Warneke *2020-12-11 22:13:14*

**tested**


[d9da6](https://github.com/JSQLParser/JSqlParser/commit/d9da64bd9ac032b) Tobias Warneke *2020-12-06 11:53:35*

**tested**


[875f7](https://github.com/JSQLParser/JSqlParser/commit/875f769e4ba353c) Tobias Warneke *2020-12-06 11:52:16*

**upgraded to javacc 7.0.10, this time the lookahead seems to be working**


[881e4](https://github.com/JSQLParser/JSqlParser/commit/881e457d7520aab) Tobias Warneke *2020-11-30 06:35:59*

**upgraded to javacc 7.0.10, this time the lookahead seems to be working**


[eae2e](https://github.com/JSQLParser/JSqlParser/commit/eae2e0d450c88c0) Tobias Warneke *2020-11-30 06:34:55*

**fixes #1065**


[e0b3a](https://github.com/JSQLParser/JSqlParser/commit/e0b3a180da5b1f8) Tobias Warneke *2020-11-22 19:39:48*

**support IN with value (#1065)**

* Co-authored-by: Jan Monterrubio &lt;Jan.Monterrubio@Cerner.com&gt; 
* Co-authored-by: Tobias &lt;t.warneke@gmx.net&gt; 

[8c7ee](https://github.com/JSQLParser/JSqlParser/commit/8c7ee289e78d07d) Jan Monterrubio *2020-11-22 19:29:27*

**fixes #1074**


[ece8a](https://github.com/JSQLParser/JSqlParser/commit/ece8a5a9e39abff) Tobias Warneke *2020-11-22 19:14:52*

**fixes #1075**


[b74f5](https://github.com/JSQLParser/JSqlParser/commit/b74f53228295d96) Tobias Warneke *2020-11-22 19:08:39*

****


[1008e](https://github.com/JSQLParser/JSqlParser/commit/1008ebcc5a806b9) Tobias Warneke *2020-11-06 22:01:54*

**Support CreateSynonym statement (#1064)**

* visual 
* add synonym support 
* add tests 
* exclude keyword 
* Co-authored-by: Jan Monterrubio &lt;Jan.Monterrubio@Cerner.com&gt; 

[17e26](https://github.com/JSQLParser/JSqlParser/commit/17e2633e46778c6) Jan Monterrubio *2020-11-06 21:45:14*

****


[d5258](https://github.com/JSQLParser/JSqlParser/commit/d5258232ea77690) Tobias Warneke *2020-11-06 21:34:08*

**Validation visitor framework (#1045)**

* * add with prefix for fluent setters. 
* https://github.com/JSQLParser/JSqlParser/issues/1004 
* add getters 
* * add with prefix for fluent setters. (revert to chaining setters, do 
* not break current api) 
* https://github.com/JSQLParser/JSqlParser/issues/1004 
* * add with prefix for fluent setters. (revert to chaining setters, do 
* not break current api) 
* https://github.com/JSQLParser/JSqlParser/issues/1004 
* use new methods within testcases 
* use new methods within testcases 
* use new methods within testcases 
* use new methods within testcases 
* use new methods within testcases 
* use new methods within testcases 
* use new methods within testcases 
* use new methods within testcases 
* remove create() methods - they do not add enough value to be justified 
* * use new methods within testcases 
* add some constructors 
* fix and add &quot;with&quot; / &quot;add&quot; methods 
* * use new methods within testcases 
* * use new methods within testcases 
* add some constructors 
* * renamed constant 
* use new methods within testcases 
* use new methods within testcases 
* use new methods within testcases 
* use new methods within testcases 
* * use new methods within testcases 
* add some with-methods 
* add getter/setter named after the field without abbrivation 
* * use new methods within testcases 
* remove empty implicit constructor 
* return the deparsed Statement - object 
* compare object tree 
* compare object tree 
* * fix ObjectTreeToStringStyle 
* compare object tree 
* remove casts not needed 
* * use new methods within testcases 
* add some &quot;set&quot; &quot;with&quot; &quot;add&quot; methods missing 
* * use new methods within testcases 
* add empty constructors and override with-/add-methods returning concrete 
* type 
* * add ReflectionModelTest 
* * use new methods within testcases 
* fix checkstyle errors 
* license header 
* remove test-classes from ReflectionModelTest 
* remove visitoradapter-classes from ReflectionModelTest 
* * add SelectDeParser(StringBuilder) 
* remove overriding setters/getters of buffer 
* #1007 
* push to synbee-contrib 
* org.synbee.commons.contrib:jsqlparser:3.2-0.0.6-SNAPSHOT 
* add ValidationUtil for simple validation of one or more statements 
* remove overrides of 
* getCause 
* printStackTrace variants 
* why add an additional cause ? 
* set cause.getMessage() the message within constructor 
* JSQLParserException(Throwable cause), othewise cause.toString() will be 
* set as default. 
* add ValidationVisitor showcase 
* https://github.com/JSQLParser/JSqlParser/issues/1005 
* add ValidationUtil for simple validation of one or more statements 
* remove overrides of 
* getCause 
* printStackTrace variants 
* why add an additional cause ? 
* set cause.getMessage() the message within constructor 
* JSQLParserException(Throwable cause), othewise cause.toString() will be 
* set as default. 
* visit(ShowTablesStatement) 
* copyright/license 
* add stubs (use deparsers as template) 
* Merge branch &#x27;master.validate&#x27; of 
* https://github.com/gitmotte/JSqlParser.git into master.validate 
* add ValidationVisitor showcase 
* https://github.com/JSQLParser/JSqlParser/issues/1005 
* add ValidationUtil for simple validation of one or more statements 
* remove overrides of 
* getCause 
* printStackTrace variants 
* why add an additional cause ? 
* set cause.getMessage() the message within constructor 
* JSQLParserException(Throwable cause), othewise cause.toString() will be 
* set as default. 
* visit(ShowTablesStatement) 
* add stubs (use deparsers as template) 
* Merge branch &#x27;master.validate&#x27; of 
* https://github.com/gitmotte/JSqlParser.git into master.validate 
* add tests for ValidationUtil 
* + implements OrderByVisitor 
* split Expressionvalidator which implements both ItemsListVisitor and 
* Expressionvisitor into Expressionvalidator and ItemListValidator 
* Merge branch &#x27;github.validate&#x27; 
* implement upsertvalidator 
* add copyright 
* validate through given ValidationCapability&#x27;s 
* * switch to new method forced by 
* ValidationCapability.validate(ValidationContext context, 
* Consumer&lt;String&gt; errorMessageConsumer); 
* add AllowedTypesValidation 
* add FeatureConfiguration 
* use FeatureConfiguration within parser 
* repair pom.xml 
* repair pom.xml 
* repair pom.xml 
* repair pom.xml 
* * make FeatureConfiguration not a singleton any more 
* CCJSqlParser extends AbstractJSqlParser&lt;CCJSqlParser&gt; 
* add FeaturesAllowed for testing against features allowed 
* implement some Validators 
* basic implementation of DatabaseMetaDataValidation / 
* JdbcDatabaseMetaDataCapability 
* moving classes to sub-packages 
* * moving classes to sub-packages 
* fixing some bugs 
* repair pom.xml 
* add and fix validations 
* add javadoc 
* * force definition of &#x60;&#x60;&#x60;public String getMessage(Feature feature)&#x60;&#x60;&#x60; 
* in FeatureSetValidation 
* allow all objects as feature-value - this may be needed by the parser, 
* if a none-boolean configuration is needed 
* impl. 
* SelectValidator.visit(PlainSelect) 
* OrderByValidator 
* add Version-enums 
* impl. 
* InsertValidator 
* multiple implementations of visit(SubSelect) -&gt; forward to 
* SelectValidator 
* add some known features to SqlServerVersion 
* refactoring enum-name should be upper case 
* add ansi sql enum 
* refactoring enum-name should be upper case 
* implement limitvalidator 
* + validateOffset 
* + validateFetch 
* + validate Pivot, UnPivot, PivotXml 
* + implement DropValidator 
* change testcase to image a more probably usecase 
* * add javadoc and 
* predefined sets for EXECUTE, ALTER, DROP 
* allow to combine FeatureSets 
* * implement executevalidator 
* implement ExpressionValidator 
* implement GrantValidator 
* javadoc and complete SELECT constant 
* use utility methods from AbstractValidator 
* more user friendly names 
* javadoc 
* add subtypes for ValidationException 
* ValidationParseException 
* DatabaseException 
* UnexpectedValidationException 
* and change Set&lt;String&gt; errors to Set&lt;ValidationException&gt; for collect. 
* javadoc &amp; rename exception 
* rename method 
* extract parsing task into package - private class for {@link 
* ValidationUtil} to parse the statements 
* within it&#x27;s own {@link ValidationCapability} 
* add null-check for parsedStatement 
* bugfix - do not collect duplicates 
* implement toString() for 
* ValidationError 
* ValidationException 
* add simple caching 
* + validateOptionalFromItem(s) 
* * implement GroupByValidator 
* implement merge-validator 
* renaming ItemListValidator -&gt; ItemsListValidator 
* + validateOptionalItemsList 
* + implement ReplaceValidator 
* + use validateOptionalColumns, validateOptionalExpression where possible 
* * remove validateOptionalColumns -&gt; switch to 
* validateOptionalExpressions 
* move validateOptionalOrderByElements to AbstractValidator 
* add validateOptional in AbstractValidator 
* add validateOptionalList in AbstractValidator 
* + SetStatementValidator 
* + ValuesStatementValidator 
* + UseStatementValidator 
* * implement UpdateValidator 
* * implement ShowStatementValidator/ShowColumnsStatementValidator 
* * implement UpdateValidator 
* * add Feature.jdbcParameter, Feature.jdbcNamedParameter, to all 
* featuresets 
* + Version.getFeaturesClone 
* add javadoc to Version-enum-constructors 
* + validateOptionalFeature 
* * implement DeleteValidator 
* ... 
* fix typo 
* small optimization 
* * move method getFeaturesClone to FeatureSet 
* implement join - validation 
* add copy(), add(Collection), remove(*) methods to FeaturesAllowed 
* * add join - features to sqlserver, h2 
* implementations 
* bugfix - merging the errors 
* copyright 
* https://github.com/JSQLParser/JSqlParser/issues/1022 
* add more fine granular control for setOperations 
* fix nullpointerexception 
* add more fine granular control for comments 
* add Features supported 
* * add javadoc 
* add features to *Version-files 
* extract methods isNotEmpty 
* check for isNotEmpty 
* * add features to *Version-files 
* always parse net.sf.jsqlparser.statement.Statements and validate the 
* list of included net.sf.jsqlparser.statement.Statement&#x27;s 
* add known mariadb features 
* new names-set for FeaturesAllowed 
* new names-set for FeaturesAllowed 
* new names-set for FeaturesAllowed 
* add ature.withItem, Feature.withItemRecursive to H2 
* Feature.setOperation, Feature.setOperationUnion, 
* Feature.setOperationIntersect, Feature.setOperationExcept, 
* for MariaDb 
* add features to SQLServer 
* Merge branch &#x27;master.orig&#x27; into github.validate 
* @Override() -&gt; @Override 
* fix typing error &quot;joinStaight&quot; &gt; joinStraight 
* rename Feature &quot;insertValues&quot; -&gt; &quot;values&quot; and use &quot;insertValues&quot; for 
* INSERT INTO ... VALUES 
* add javadoc 
* add Feature.selectGroupByGroupingSets to PostgresqlVersion 
* implement basic OracleVersion 
* add Feature.mySql* - also supported by mariadb 
* add some more finegraned control over &quot;drop&quot; Feature. 
* drop, 
* dropTable, 
* dropIndex, 
* dropView, 
* dropSchema, 
* dropSequence, 
* dropIfExists, 
* complete FeaturesAllowed groups INSERT/UPDATE/DELETE/MERGE/DML 
* add link to documentation 
* fix - duplicate use of feature &quot;function&quot; - the use of functions in 
* statements and &quot;createFunction&quot; as a ddl statement 
* TODO this feature seams very close to a jsqlparser-user usecase 
* * implement MySqlVersion 
* replace feature Feature.dropIfExists by features dropTableIfExists, 
* dropIndexIfExists, dropViewIfExists, dropSchemaIfExists, 
* dropSequenceIfExists 
* add methods FeatureSet.getNotContained FeatureSet.retainAll 
* remove HSQLDBVersion - do not support this variant 
* remove HSQLDBVersion - do not support this variant 
* add unit-test 
* + add unittests for 
* UpdateValidator 
* DeleteValidator 
* add stubs for all other Validator-classes 
* + ModifyableFeatureSet 
* add some utility-methods in ValidationTestAsserts 
* complete unit-tests for InsertValidator 
* remote Feature.insertReturningExpressionList for Oracle - 
* returning_clause requires INTO clause (only PL/SQL) 
* add some more select validation tests 
* add DropValidatorTests 
* add DropValidatorTests 
* add CreateTableValidatorTests 
* add CreateTableValidatorTests 
* add ExpressionValidatorTests 
* add OrderByValidatorTest 
* use isNotEmpty 
* implement GroupByValidatorTest 
* implement CreateSequenceValidatorTest 
* remove @Ignore - test is ok 
* implement CreateIndexValidatorTest 
* implement CreateViewValidatorTest 
* enable validation of Feature.commentOnView (#1024 is merged already) 
* change format of #toString() for better readability 
* * implement MergeValidatorTest 
* implement ReplaceValidatorTest 
* implement StatementValidatorTest 
* rename 
* ValidationUtil -&gt; Validation 
* ValidatorUtil -&gt; ValidationUtil 
* add testcases for ValidationUtil 
* add DatabaseMetaDataValidationTest 
* checkstyle fix 
* add copyright statement 
* add unit-tests for show tables, show column, show statements 
* * add ExecuteValidatorTest 
* as there is a difference between execute &lt;procedure&gt; and execute 
* [immediate] &lt;dynamic sql&gt; with USING expr, ... remove support for 
* execute on MYSQL, MARIADB, ORACLE 
* * add ExecuteValidatorTest for CALL fnName (mysql, mariadb, postgres) 
* add upsertvalidatortest 
* add GrantValidatorTest 
* add AlterSequenceValidatorTest 
* add AlterSequenceValidatorTest 
* add AlterViewValidatorTest 
* add AlterValidatorTest 
* replace !&#x3D; null by isNotEmpty on collections 
* fix formatting 
* add validate commit 
* add validate block 
* add DeclareStatementValidatorTest 
* let NamesLookup implement UnaryOperator&lt;String&gt; 
* let NamesLookup implement UnaryOperator&lt;String&gt; 
* add javadoc 
* add more DatabaseMetaDataValidationTest&#x27;s 
* extract JdbcDatabaseMetaDataCapability.splitAndValidateMinMax 
* add pivot/unpivot/pivotxml validation testcases 
* add testcase for Feature.tableFunction 
* add test for lateral joins and subjoins 
* add testValidationRowMovementOption 
* add values validator test 
* move tests to LimitValidatorTest 
* move tests to UseStatementValidatorTest 
* add tests for SET - statements 
* fix checkstyle error 
* new serialVersionUID 
* add validation for NamedObject not existing 
* need table/view reference to validate column names 
* fix typo 
* fix errormessage (Arrays.toString(types)) 
* add trigger, alias 
* return null, instead of throwing exception, if not found 
* extract NamesLookup to own file (jdk-bug enum inner classes) 
* fix name-check AlterOperation.ALTER 
* fix error message 
* remove methods not needed (they only delegate to ValidationContext) 
* add tests - validate metadata 
* fix compile error 
* fix columnExists check - depending on the statement the prefix is an 
* alias, a table/view or it has no prefix (need to lookup within all 
* related tables/views) 
* fix javadoc warnings 

[8c735](https://github.com/JSQLParser/JSqlParser/commit/8c735be5b179e51) gitmotte *2020-11-06 21:12:25*

**Support Create table LIKE (#1066)**

* fixes #413 
* add coverage 
* Co-authored-by: chyun &lt;chyun_wu@163.com&gt; 

[ac746](https://github.com/JSQLParser/JSqlParser/commit/ac7462286ae15b9) Chyun *2020-11-06 21:05:09*

**fixes #1068**


[f1cf0](https://github.com/JSQLParser/JSqlParser/commit/f1cf0abc11ed783) Tobias Warneke *2020-11-06 20:59:19*

**Bump junit from 4.12 to 4.13.1 (#1063)**

* Bumps [junit](https://github.com/junit-team/junit4) from 4.12 to 4.13.1. 
* - [Release notes](https://github.com/junit-team/junit4/releases) 
* - [Changelog](https://github.com/junit-team/junit4/blob/main/doc/ReleaseNotes4.12.md) 
* - [Commits](https://github.com/junit-team/junit4/compare/r4.12...r4.13.1) 
* Signed-off-by: dependabot[bot] &lt;support@github.com&gt; 
* Co-authored-by: dependabot[bot] &lt;49699333+dependabot[bot]@users.noreply.github.com&gt; 

[f9a11](https://github.com/JSQLParser/JSqlParser/commit/f9a115c582dd59b) dependabot[bot] *2020-10-13 12:26:17*

**fixes #1062**


[80e28](https://github.com/JSQLParser/JSqlParser/commit/80e2891e8a79402) wumpz *2020-10-11 19:59:03*

**corrected a test failure**


[bc0e5](https://github.com/JSQLParser/JSqlParser/commit/bc0e5b913fc4f61) wumpz *2020-10-06 21:07:00*

**support FILTER not only for window function (#1046)**

* support FILTER not only for window function 
* Fixed imports 

[f32fa](https://github.com/JSQLParser/JSqlParser/commit/f32fa6137d6161b) Роман Зотов *2020-10-05 19:45:36*

**fixes #1059**


[3e84a](https://github.com/JSQLParser/JSqlParser/commit/3e84a377b488960) wumpz *2020-10-04 20:43:42*

****


[6b35e](https://github.com/JSQLParser/JSqlParser/commit/6b35e2fc4f39083) wumpz *2020-10-04 20:17:54*

**fixes #1055 - added simple jdbc parameter to interval expression**


[68659](https://github.com/JSQLParser/JSqlParser/commit/686599b199d6d49) wumpz *2020-10-04 20:16:04*

**Retain original value in TimestampValue (#1057)**

* Co-authored-by: Enrico Olivelli &lt;enrico.olivelli@diennea.com&gt; 

[622f9](https://github.com/JSQLParser/JSqlParser/commit/622f9aebb3ebce7) Enrico Olivelli *2020-10-04 19:51:01*

**fixes #1053**


[45aa8](https://github.com/JSQLParser/JSqlParser/commit/45aa8f853a10779) wumpz *2020-10-04 19:42:23*

**Addons/fixes for Fluent API  (#1049)**

* fix unittests for setter/wither methods with primitive arguments 
* add missing withAscDescPresent 

[8165e](https://github.com/JSQLParser/JSqlParser/commit/8165e29cb081080) gitmotte *2020-10-04 19:20:43*

**fixes #1040**


[3f516](https://github.com/JSQLParser/JSqlParser/commit/3f5165122bc9824) wumpz *2020-09-27 21:12:11*

**xmlserialize support patch for optional order by part**


[3747f](https://github.com/JSQLParser/JSqlParser/commit/3747f1c00503b54) wumpz *2020-09-10 21:21:47*

**xmlserialize support patch for expressions**


[8c4e0](https://github.com/JSQLParser/JSqlParser/commit/8c4e0ca141656fd) wumpz *2020-09-08 20:35:36*

**Make UnPivot.getUnPivotInClause() return List<SelectExpressionItem> (#1039)**


[abedc](https://github.com/JSQLParser/JSqlParser/commit/abedce539515638) MoonFruit *2020-09-07 11:32:37*

**xmlserialize support**


[b580a](https://github.com/JSQLParser/JSqlParser/commit/b580a093c2edda6) wumpz *2020-09-05 22:00:43*

****


[536fb](https://github.com/JSQLParser/JSqlParser/commit/536fb0348ae985e) wumpz *2020-08-30 20:23:52*

**bugfix issue #1036: supporting DROP SEQUENCE (#1037)**


[e5cd7](https://github.com/JSQLParser/JSqlParser/commit/e5cd7c83e15f7f9) suiwenbo *2020-08-30 20:16:20*

**modified Condition production to be more performant**


[3d7f5](https://github.com/JSQLParser/JSqlParser/commit/3d7f55c48a8dfae) wumpz *2020-08-29 22:30:01*

**bugfix #720 #991: supporting SELECT "conditions" (#1032)**

* bugfix issue #1020: JSON type in MySQL not supported in v3.2 
* bugfix issue #720 #991: supporting SELECT &quot;CONDITIONS&quot; 

[9e26b](https://github.com/JSQLParser/JSqlParser/commit/9e26b76ddbc626f) suiwenbo *2020-08-25 22:29:41*

****


[81523](https://github.com/JSQLParser/JSqlParser/commit/815235606244b01) wumpz *2020-08-23 20:33:42*

**setting version to 4-SNAPSHOT**


[85286](https://github.com/JSQLParser/JSqlParser/commit/852860985832c4c) wumpz *2020-08-23 20:32:33*

**Fluent builder api #1004 (#1014)**

* https://github.com/JSQLParser/JSqlParser/issues/1004 
* create(...) methods 
* chaining - methods returning &quot;this&quot; 
* overwrite chaining - methods of abstract parents/interfaces for 
* returning concrete type 
* add&lt;Name&gt; methods on collection-fields with varargs-parameter 
* add public T get&lt;Name&gt;(Class&lt;T&gt;) - casting and returning an inner 
* interface-type 
* 1004 add chaining - methods returning &quot;this&quot; 
* #1004 add chaining - methods returning &quot;this&quot; 
* * add&lt;Name&gt; methods on collection-fields with varargs-parameter 
* add&lt;Name&gt; methods on collection-fields with collection-parameter 
* https://github.com/JSQLParser/JSqlParser/issues/1004 
* * add chaining - methods returning &quot;this&quot; 
* add&lt;Name&gt; methods on collection-fields with varargs-parameter 
* add&lt;Name&gt; methods on collection-fields with collection-parameter 
* https://github.com/JSQLParser/JSqlParser/issues/1004 
* * add public T get&lt;Name&gt;(Class&lt;T&gt;) - casting and returning the concrete 
* type 
* https://github.com/JSQLParser/JSqlParser/issues/1004 
* * add public T get&lt;Name&gt;(Class&lt;T&gt;) - casting and returning the concrete 
* type (swap Class&lt;? extends E&gt; for Class&lt;E&gt;) 
* https://github.com/JSQLParser/JSqlParser/issues/1004 
* * overwrite chaining - methods of abstract parents/interfaces for 
* returning concrete type 
* https://github.com/JSQLParser/JSqlParser/issues/1004 
* * add with prefix for fluent setters. 
* https://github.com/JSQLParser/JSqlParser/issues/1004 
* add getters 
* * add with prefix for fluent setters. (revert to chaining setters, do 
* not break current api) 
* https://github.com/JSQLParser/JSqlParser/issues/1004 
* * add with prefix for fluent setters. (revert to chaining setters, do 
* not break current api) 
* https://github.com/JSQLParser/JSqlParser/issues/1004 
* use new methods within testcases 
* use new methods within testcases 
* use new methods within testcases 
* use new methods within testcases 
* use new methods within testcases 
* use new methods within testcases 
* use new methods within testcases 
* use new methods within testcases 
* remove create() methods - they do not add enough value to be justified 
* * use new methods within testcases 
* add some constructors 
* fix and add &quot;with&quot; / &quot;add&quot; methods 
* * use new methods within testcases 
* * use new methods within testcases 
* add some constructors 
* * renamed constant 
* use new methods within testcases 
* use new methods within testcases 
* use new methods within testcases 
* use new methods within testcases 
* * use new methods within testcases 
* add some with-methods 
* add getter/setter named after the field without abbrivation 
* * use new methods within testcases 
* remove empty implicit constructor 
* return the deparsed Statement - object 
* compare object tree 
* compare object tree 
* * fix ObjectTreeToStringStyle 
* compare object tree 
* remove casts not needed 
* * use new methods within testcases 
* add some &quot;set&quot; &quot;with&quot; &quot;add&quot; methods missing 
* * use new methods within testcases 
* add empty constructors and override with-/add-methods returning concrete 
* type 
* * add ReflectionModelTest 
* * use new methods within testcases 
* fix checkstyle errors 
* license header 
* remove test-classes from ReflectionModelTest 
* remove visitoradapter-classes from ReflectionModelTest 
* remove duplicate import declaration (checkstyle error) 
* * fix RandomUtils to support used java.sql.* types 
* fix RandomUtils to support enums 
* fix RandomUtils to map objects by its interfaces and super-classes 
* filter method &quot;setASTNode&quot; - do not test setters (cannot randomly 
* create a SimpleNode) 
* add javadoc, stating that this is a marker interface 
* https://github.com/JSQLParser/JSqlParser/pull/1014#discussion_r454761902 
* revert formatting change 
* https://github.com/JSQLParser/JSqlParser/pull/1014#discussion_r454762463 
* change to EXEC_TYPE.EXECUTE just so the assertion didn&#x27;t change 
* https://github.com/JSQLParser/JSqlParser/pull/1014#discussion_r454763565 
* try to revert format changes 
* https://github.com/JSQLParser/JSqlParser/pull/1014#discussion_r454800430 
* try to revert format changes 
* https://github.com/JSQLParser/JSqlParser/pull/1014#discussion_r454800430 
* remove brackets on @Override() -&gt; @Override 
* add with-methods to new fields 

[6cff1](https://github.com/JSQLParser/JSqlParser/commit/6cff161dacc1e6f) gitmotte *2020-08-23 20:07:53*

****


[02d58](https://github.com/JSQLParser/JSqlParser/commit/02d5837c5ee8c92) wumpz *2020-08-09 21:26:57*

****


[574a6](https://github.com/JSQLParser/JSqlParser/commit/574a6b7fda44857) wumpz *2020-08-09 21:22:24*

**Support Foreign Key ON UPDATE CASCADE (#1025)**

* https://github.com/JSQLParser/JSqlParser/issues/985 
* add 2 unit-tests for given statements 
* https://github.com/JSQLParser/JSqlParser/issues/985 
* fix formating (line width) 
* https://github.com/JSQLParser/JSqlParser/issues/985 
* * fix nullpointerexceptions 
* add more unittest-assertions 
* https://github.com/JSQLParser/JSqlParser/issues/985 
* change order to match the same order as in ForeignKeyIndex 
* byAction should not throw an exception (is used by deprecated 
* string-setters) 
* add unit-tests for ReferentialAction within AlterExpression 
* fix toString (added bug on refactoring) 
* javadoc 
* test set from get on null-values too 
* refactoring: add and use ReferentialAction() to evaluate enum 
* https://github.com/JSQLParser/JSqlParser/issues/985 
* refactoring: fix parser that order of referential actions does not 
* matter 
* https://github.com/JSQLParser/JSqlParser/issues/985 
* add empty constructor 

[1e88d](https://github.com/JSQLParser/JSqlParser/commit/1e88dd57eb48ebf) gitmotte *2020-08-09 21:01:34*

**bugfix issue #1020: JSON type in MySQL not supported in v3.2 (#1028)**


[685f6](https://github.com/JSQLParser/JSqlParser/commit/685f6fe43fb35d7) suiwenbo *2020-08-09 20:59:52*

****


[cfe5a](https://github.com/JSQLParser/JSqlParser/commit/cfe5a2e725b555b) wumpz *2020-08-09 20:58:15*

**Add generated sources to classpath. (#804)**


[14fb8](https://github.com/JSQLParser/JSqlParser/commit/14fb80d947d9bf0) Matthieu Vergne *2020-08-09 20:53:38*

****


[008d9](https://github.com/JSQLParser/JSqlParser/commit/008d9ad7f28a7b9) wumpz *2020-08-09 20:51:34*

**COMMENT ON VIEW (#1024)**

* * implement COMMENT ON VIEW 
* testcase &quot;testCommentOnView&quot; 
* https://github.com/JSQLParser/JSqlParser/issues/1023 
* add more asserts 

[449f5](https://github.com/JSQLParser/JSqlParser/commit/449f55219fc39eb) gitmotte *2020-08-09 20:49:05*

**fixes #1026**


[4f888](https://github.com/JSQLParser/JSqlParser/commit/4f8882dd143469d) wumpz *2020-08-09 20:46:07*

**fixes #1026**


[4c323](https://github.com/JSQLParser/JSqlParser/commit/4c32302d386ec88) wumpz *2020-08-09 20:39:48*

**fixes #1027**


[a923e](https://github.com/JSQLParser/JSqlParser/commit/a923e7e00ef3518) wumpz *2020-08-09 20:31:26*

**fixes #1029**


[db6ac](https://github.com/JSQLParser/JSqlParser/commit/db6acef1c38e09e) wumpz *2020-08-09 19:55:55*

**fixes #732**


[57a7d](https://github.com/JSQLParser/JSqlParser/commit/57a7dcdf2ede8a0) wumpz *2020-07-15 21:07:09*

**variable assignment implemented**


[cdd0f](https://github.com/JSQLParser/JSqlParser/commit/cdd0fa244dadca1) wumpz *2020-07-13 22:03:12*

**allowed Jdbc named parameters within interval expressions**


[869a7](https://github.com/JSQLParser/JSqlParser/commit/869a7b2088ff54f) wumpz *2020-07-12 21:25:43*

**allowed Jdbc named parameters within interval expressions**


[3602c](https://github.com/JSQLParser/JSqlParser/commit/3602c5f6d0a1766) wumpz *2020-07-12 21:25:05*

**some house keeping**


[8bcf2](https://github.com/JSQLParser/JSqlParser/commit/8bcf2dc4f64cde3) wumpz *2020-07-11 20:39:37*

**fixes #1009**


[71d65](https://github.com/JSQLParser/JSqlParser/commit/71d6523f2150bc2) wumpz *2020-07-11 20:30:30*

**Add show tables support (#1015)**

* visual 
* implement show tables 
* Co-authored-by: Jan Monterrubio &lt;Jan.Monterrubio@Cerner.com&gt; 

[c0373](https://github.com/JSQLParser/JSqlParser/commit/c03733b3cfcb758) Jan Monterrubio *2020-07-11 16:43:42*

**let all deparsers extend AbstractDeParser (#1007)**

* let all deparsers extend AbstractDeParser 
* * add SelectDeParser(StringBuilder) 
* remove overriding setters/getters of buffer 
* #1007 

[2b790](https://github.com/JSQLParser/JSqlParser/commit/2b7909c3be31ca8) gitmotte *2020-07-11 16:40:11*

****


[ea88e](https://github.com/JSQLParser/JSqlParser/commit/ea88e1b2899176e) wumpz *2020-06-28 19:22:52*

****


[51e84](https://github.com/JSQLParser/JSqlParser/commit/51e8428d7dcf7dc) wumpz *2020-06-27 23:01:20*


## jsqlparser-3.2 (2020-06-28)

### Other changes

****


[cd742](https://github.com/JSQLParser/JSqlParser/commit/cd742d3104218ab) wumpz *2020-06-27 22:29:23*

**partial func support (#1000)**


[9df19](https://github.com/JSQLParser/JSqlParser/commit/9df19b9517112b8) Jan Monterrubio *2020-06-25 05:31:27*

****


[7a19a](https://github.com/JSQLParser/JSqlParser/commit/7a19a9b71c2a44d) wumpz *2020-06-23 21:56:47*

**Support options for Explain (#996)**

* visual 
* issue-995 
* support verbose 
* postgres explain 
* tests 
* no text 
* Co-authored-by: Jan Monterrubio &lt;Jan.Monterrubio@Cerner.com&gt; 

[13873](https://github.com/JSQLParser/JSqlParser/commit/1387354712285e4) Jan Monterrubio *2020-06-23 21:52:42*

****


[8c307](https://github.com/JSQLParser/JSqlParser/commit/8c3076efc9544cb) wumpz *2020-06-23 20:45:19*

**Support multiple lists for an IN clause (#997)**

* visual 
* wip 
* cleanup n test 
* polish 
* lookahead 
* Co-authored-by: Jan Monterrubio &lt;Jan.Monterrubio@Cerner.com&gt; 

[5de4a](https://github.com/JSQLParser/JSqlParser/commit/5de4ae597fbeda3) Jan Monterrubio *2020-06-20 22:10:38*

**fixes #999**


[ce8ee](https://github.com/JSQLParser/JSqlParser/commit/ce8eef8bb6c2526) wumpz *2020-06-20 22:06:17*

**fixes #999**


[325cc](https://github.com/JSQLParser/JSqlParser/commit/325ccb0fc2cf067) wumpz *2020-06-20 21:57:10*

**Support ALTER SEQUENCE  (#980)**

* support alter sequence 
* improve coverage 

[d34c8](https://github.com/JSQLParser/JSqlParser/commit/d34c885ba5a8c93) Jan Monterrubio *2020-05-23 10:16:07*

****


[779a7](https://github.com/JSQLParser/JSqlParser/commit/779a744a8ab80c7) wumpz *2020-05-23 10:12:52*

**fixes #984**


[38597](https://github.com/JSQLParser/JSqlParser/commit/38597f347c820a7) wumpz *2020-05-16 21:14:26*

**fixes #984**


[60ac1](https://github.com/JSQLParser/JSqlParser/commit/60ac16a98022455) wumpz *2020-05-16 21:00:28*

**tests for issue**


[82894](https://github.com/JSQLParser/JSqlParser/commit/8289406bbcbba45) wumpz *2020-05-14 21:16:52*

****


[d6bbc](https://github.com/JSQLParser/JSqlParser/commit/d6bbc3fa8a10d58) wumpz *2020-05-08 21:12:13*

****


[1ee7c](https://github.com/JSQLParser/JSqlParser/commit/1ee7c417bf0936e) wumpz *2020-05-08 20:19:13*

**fixes #981**


[d79b4](https://github.com/JSQLParser/JSqlParser/commit/d79b44db122652e) wumpz *2020-05-08 20:14:21*

**fixbuild (#978)**


[08b94](https://github.com/JSQLParser/JSqlParser/commit/08b9477ef28a377) Jan Monterrubio *2020-04-30 04:42:04*

**Implement row movement clause for table creation (#974)**

* visual 
* implement row movement 
* support row + AS 
* Co-authored-by: Jan Monterrubio &lt;Jan.Monterrubio@Cerner.com&gt; 

[79b5f](https://github.com/JSQLParser/JSqlParser/commit/79b5fe9c5681961) Jan Monterrubio *2020-04-28 07:02:51*

**Support CREATE SEQUENCE (#977)**

* wip 
* wip, some parsing 
* support sequence 
* implement feature 
* delete issue tests 
* compile it 

[a6a3c](https://github.com/JSQLParser/JSqlParser/commit/a6a3c616b8994f1) Jan Monterrubio *2020-04-28 07:01:10*

****


[ca76f](https://github.com/JSQLParser/JSqlParser/commit/ca76fed4be73522) wumpz *2020-04-19 19:50:28*

**fixes #962**


[3f918](https://github.com/JSQLParser/JSqlParser/commit/3f918501bda7fc4) wumpz *2020-04-18 21:53:14*

**implement feature (#972)**

* Co-authored-by: Jan Monterrubio &lt;Jan.Monterrubio@Cerner.com&gt; 

[aee39](https://github.com/JSQLParser/JSqlParser/commit/aee3947757eecf4) Jan Monterrubio *2020-04-17 21:01:22*

**test method names changed**


[2b564](https://github.com/JSQLParser/JSqlParser/commit/2b5647b5af32b74) zhumaliev-rv *2020-04-03 05:31:40*

**added Oracle GRANT statement**


[fa215](https://github.com/JSQLParser/JSqlParser/commit/fa21512ea2f2dd8) zhumaliev-rv *2020-04-02 12:03:43*

**fixes #855**


[f3ecd](https://github.com/JSQLParser/JSqlParser/commit/f3ecdcb1a8fe8b0) wumpz *2020-03-25 22:42:33*

**fixes #915**


[9ce74](https://github.com/JSQLParser/JSqlParser/commit/9ce74e234e09525) wumpz *2020-03-04 22:04:40*

**fixes #922**


[8abd6](https://github.com/JSQLParser/JSqlParser/commit/8abd6e732c926c2) wumpz *2020-03-01 00:00:26*

****


[65ad8](https://github.com/JSQLParser/JSqlParser/commit/65ad8f9b8b05835) wumpz *2020-02-29 23:22:26*

**fixes #864**


[5783b](https://github.com/JSQLParser/JSqlParser/commit/5783b65f169560a) wumpz *2020-02-15 21:31:55*

**fixes #701**


[8d43f](https://github.com/JSQLParser/JSqlParser/commit/8d43facbc33c803) wumpz *2020-02-15 20:43:38*

**fixes #945**


[ab405](https://github.com/JSQLParser/JSqlParser/commit/ab4054ce5c1917e) wumpz *2020-02-14 23:19:47*

**fixes #944**


[e1ff1](https://github.com/JSQLParser/JSqlParser/commit/e1ff1f09e9ad946) wumpz *2020-02-14 23:02:57*

**fixes #944**


[22117](https://github.com/JSQLParser/JSqlParser/commit/22117c40613778b) wumpz *2020-02-14 22:58:06*

**introduces sql server hints**


[92c74](https://github.com/JSQLParser/JSqlParser/commit/92c74bfb15b18ac) wumpz *2020-02-14 19:40:15*

**introduces sql server hints**


[6a414](https://github.com/JSQLParser/JSqlParser/commit/6a414aa703b9a5b) wumpz *2020-02-14 19:04:41*

**introduced view keyword**


[f12bb](https://github.com/JSQLParser/JSqlParser/commit/f12bb31a14973ac) wumpz *2020-02-12 23:54:37*

**fixes #909**


[9b998](https://github.com/JSQLParser/JSqlParser/commit/9b998d67699bf5c) wumpz *2020-02-02 21:51:56*

**fixes #930**


[4c4a5](https://github.com/JSQLParser/JSqlParser/commit/4c4a5361453c434) wumpz *2020-02-02 21:09:52*

**fixes #940**


[dc93a](https://github.com/JSQLParser/JSqlParser/commit/dc93a07e38341de) wumpz *2020-02-02 20:43:09*

**fixes #941  again :)**


[782dc](https://github.com/JSQLParser/JSqlParser/commit/782dce8d20ce99f) wumpz *2020-02-02 20:22:40*

**fixes #941**


[ce392](https://github.com/JSQLParser/JSqlParser/commit/ce392b3739dcfbb) wumpz *2020-02-01 23:49:17*

**fixes #924**


[d0cd8](https://github.com/JSQLParser/JSqlParser/commit/d0cd8f869bb1173) wumpz *2020-02-01 23:21:52*

**Update README.md**


[a03d2](https://github.com/JSQLParser/JSqlParser/commit/a03d235d91de445) Tobias *2020-02-01 00:34:26*

**updated some maven plugins**


[3ba29](https://github.com/JSQLParser/JSqlParser/commit/3ba29f1fdc76a7a) wumpz *2020-02-01 00:33:17*

**fixes #936**

* fixes #938 

[39e92](https://github.com/JSQLParser/JSqlParser/commit/39e920df15fefd7) wumpz *2020-02-01 00:08:17*

**fixes #936**


[abf44](https://github.com/JSQLParser/JSqlParser/commit/abf440d2be0fbc6) wumpz *2020-01-31 23:47:06*

**added keyword group to possible object names**


[430b3](https://github.com/JSQLParser/JSqlParser/commit/430b3ee8f506173) wumpz *2020-01-27 06:42:20*

**fixes #923**


[775a0](https://github.com/JSQLParser/JSqlParser/commit/775a09b0a763f55) wumpz *2020-01-25 23:09:35*

**fixes #923**


[57d50](https://github.com/JSQLParser/JSqlParser/commit/57d5044a1a1d6bc) wumpz *2020-01-25 23:09:18*

**started fixing #923**


[0f78d](https://github.com/JSQLParser/JSqlParser/commit/0f78dfdf5dfa6a7) wumpz *2020-01-23 23:19:41*

**fixes #932**


[3490e](https://github.com/JSQLParser/JSqlParser/commit/3490e61dbc6b9b0) wumpz *2020-01-23 21:57:59*

**fixes #918**


[3b89c](https://github.com/JSQLParser/JSqlParser/commit/3b89cd28ad893ae) wumpz *2020-01-23 21:26:58*

**fixes #921**


[f4b10](https://github.com/JSQLParser/JSqlParser/commit/f4b10cff44e90f4) wumpz *2020-01-21 07:27:10*

**fixes #921**


[a23d3](https://github.com/JSQLParser/JSqlParser/commit/a23d30bc9425b19) wumpz *2020-01-21 07:26:35*

**fixes #929**


[a3c95](https://github.com/JSQLParser/JSqlParser/commit/a3c95d4712852f2) wumpz *2020-01-21 07:11:38*

**fixes #928**


[0bae6](https://github.com/JSQLParser/JSqlParser/commit/0bae629dba2459c) wumpz *2020-01-21 06:58:58*

**fixes #927**


[62648](https://github.com/JSQLParser/JSqlParser/commit/6264801af1c0bef) wumpz *2020-01-21 06:49:23*

**fixes #917**


[8de0f](https://github.com/JSQLParser/JSqlParser/commit/8de0fd9971b6e07) wumpz *2020-01-05 22:08:03*

**rewind #910**


[9ca4f](https://github.com/JSQLParser/JSqlParser/commit/9ca4f3e63b26353) wumpz *2020-01-03 00:03:35*

**Adding support for simple expressions in INTERVAL expressions (#910)**


[ebac9](https://github.com/JSQLParser/JSqlParser/commit/ebac9dbcadb4df8) Tomer Shay (Shimshi) *2019-12-20 06:14:04*

**removed null check**


[aba6f](https://github.com/JSQLParser/JSqlParser/commit/aba6f3ae2148d51) wumpz *2019-12-18 14:01:17*

**Update README.md**


[6fd3c](https://github.com/JSQLParser/JSqlParser/commit/6fd3c9c0e6fa4b6) Tobias *2019-12-02 15:55:47*

****


[5be06](https://github.com/JSQLParser/JSqlParser/commit/5be0646d9004b6e) wumpz *2019-12-01 22:12:59*

****


[82d8f](https://github.com/JSQLParser/JSqlParser/commit/82d8f59db9f1c33) wumpz *2019-12-01 22:12:54*

**Update README.md**


[f7ae7](https://github.com/JSQLParser/JSqlParser/commit/f7ae75ace8ecb98) Tobias *2019-11-27 20:26:12*

**fixes #899**

* switched to assertj from hamcrest 

[9707e](https://github.com/JSQLParser/JSqlParser/commit/9707e4f0aacff16) wumpz *2019-11-23 23:18:56*

**Adding support for casting to SIGNED (#900)**


[73b3d](https://github.com/JSQLParser/JSqlParser/commit/73b3d44f16a57c9) Tomer Shay (Shimshi) *2019-11-20 09:39:47*

**Support parsing SELECT FOR UPDATE NOWAIT - Refer to documents on https://docs.oracle.com/cd/E17952_01/mysql-8.0-en/innodb-locking-reads.html#innodb-locking-reads-nowait-skip-locked (#896)**


[596e6](https://github.com/JSQLParser/JSqlParser/commit/596e631ff985c10) Yoon Kyong Sik *2019-11-16 10:07:59*

**added some doc to CCJSqlParserUtil**


[5242a](https://github.com/JSQLParser/JSqlParser/commit/5242a18d20a7c2a) wumpz *2019-11-13 08:31:40*

**Adding support for STRAIGHT_JOIN in the select clause (#861)**

* Adding support for straight_join in the select clause 
* Renaming the field name to reflect that this is a MySQL hint 

[3cdea](https://github.com/JSQLParser/JSqlParser/commit/3cdea6bd3d9ce21) Tomer Shay (Shimshi) *2019-11-09 20:30:54*

**Update README.md**


[a0077](https://github.com/JSQLParser/JSqlParser/commit/a0077a1e3b4c0c1) Tobias *2019-11-08 07:53:42*

****


[47a94](https://github.com/JSQLParser/JSqlParser/commit/47a944eb4571b02) wumpz *2019-11-06 22:36:04*


## jsqlparser-3.1 (2019-11-06)

### Other changes

**fixes #344**


[43862](https://github.com/JSQLParser/JSqlParser/commit/43862ddf607493d) wumpz *2019-11-06 22:23:56*

**fixes #344**


[782de](https://github.com/JSQLParser/JSqlParser/commit/782de006989e787) wumpz *2019-11-06 22:23:06*

**Adding support for complex expressions in the ORDER BY clause (#890)**


[678ac](https://github.com/JSQLParser/JSqlParser/commit/678ac96b5948175) Tomer Shay (Shimshi) *2019-10-31 07:19:03*

**fixes #884**


[1d8a9](https://github.com/JSQLParser/JSqlParser/commit/1d8a9479ff3fc6f) wumpz *2019-10-26 21:38:48*

**fixes #880**


[7746b](https://github.com/JSQLParser/JSqlParser/commit/7746bbb7ffa8fb4) wumpz *2019-10-26 21:25:47*

****


[552bf](https://github.com/JSQLParser/JSqlParser/commit/552bf605ec075ec) wumpz *2019-10-26 20:41:45*

**Added support for Oracle UNPIVOT keyword. (#882)**

* Added support for Oracle UNPIVOT keyword. 
* Back to original version number. 
* Updated imports. 
* Added missing import. 

[bcc27](https://github.com/JSQLParser/JSqlParser/commit/bcc271870ad767f) Pascal Mulder *2019-10-26 20:35:43*

**fixes #878**


[c2836](https://github.com/JSQLParser/JSqlParser/commit/c2836f6a276879e) wumpz *2019-10-26 20:27:03*

**fixes #869**


[5b781](https://github.com/JSQLParser/JSqlParser/commit/5b78153ad5b8eb1) wumpz *2019-10-18 23:02:59*

**fixes #876**


[144e6](https://github.com/JSQLParser/JSqlParser/commit/144e60b43fd24ab) wumpz *2019-10-18 21:19:50*

**fixes #866**


[25e1d](https://github.com/JSQLParser/JSqlParser/commit/25e1dcc3fc83440) wumpz *2019-10-16 22:44:27*

**fixes #862**


[51c92](https://github.com/JSQLParser/JSqlParser/commit/51c92d89389f780) wumpz *2019-10-16 21:40:42*

**tests #874**


[1ecfc](https://github.com/JSQLParser/JSqlParser/commit/1ecfcbc4abfd054) wumpz *2019-10-16 20:46:13*

**fixes #865**


[8a965](https://github.com/JSQLParser/JSqlParser/commit/8a965554e1005ca) wumpz *2019-10-14 21:12:05*

**fixes #867**


[22229](https://github.com/JSQLParser/JSqlParser/commit/22229459fb65b6f) wumpz *2019-10-09 08:40:11*

**fixes #867**


[6ef7e](https://github.com/JSQLParser/JSqlParser/commit/6ef7ebbad314090) wumpz *2019-10-09 08:38:23*

**fixes #847**


[2147a](https://github.com/JSQLParser/JSqlParser/commit/2147a21b0caee90) wumpz *2019-10-05 22:41:29*

**allowing start as keyword for column and tablenames**


[f9bba](https://github.com/JSQLParser/JSqlParser/commit/f9bba25386300ca) wumpz *2019-10-05 22:39:45*

**allowing start as keyword for column and tablenames**


[cc6a4](https://github.com/JSQLParser/JSqlParser/commit/cc6a4c5cba1e523) wumpz *2019-10-05 22:37:57*

**fixes #859**


[8e61a](https://github.com/JSQLParser/JSqlParser/commit/8e61a1884297af9) wumpz *2019-10-01 06:14:39*

**Update README.md**


[c6441](https://github.com/JSQLParser/JSqlParser/commit/c6441e05b5d3c59) Tobias *2019-09-30 23:04:05*

**Fixes linkage of SubSelect to Node**


[55783](https://github.com/JSQLParser/JSqlParser/commit/557831dc6621141) PGrafkin *2019-09-22 19:02:33*

**fixes #845**


[d6b4e](https://github.com/JSQLParser/JSqlParser/commit/d6b4e4d7bec895a) wumpz *2019-09-20 15:01:04*

****


[56ddc](https://github.com/JSQLParser/JSqlParser/commit/56ddc2d8fe4e898) wumpz *2019-09-20 14:41:27*

**fixes #849**


[62a03](https://github.com/JSQLParser/JSqlParser/commit/62a0341a35690fe) wumpz *2019-09-20 14:39:58*

**fixes #848**


[1d2c2](https://github.com/JSQLParser/JSqlParser/commit/1d2c261721d0d61) wumpz *2019-09-20 13:05:40*

****


[484ea](https://github.com/JSQLParser/JSqlParser/commit/484ea9dd02a9c4c) wumpz *2019-09-20 13:05:14*

**fixes #850**


[1cd3c](https://github.com/JSQLParser/JSqlParser/commit/1cd3c27f32aed64) wumpz *2019-09-20 12:26:44*

**Update README.md**


[6191a](https://github.com/JSQLParser/JSqlParser/commit/6191ae0646e1e40) Tobias *2019-08-29 21:32:25*


## jsqlparser-3.0 (2019-08-29)

### Other changes

**fixes #842**


[516ea](https://github.com/JSQLParser/JSqlParser/commit/516ea8a3a6988b0) wumpz *2019-08-26 22:12:59*

**fixes #750 - duplicate**


[dd7ed](https://github.com/JSQLParser/JSqlParser/commit/dd7eda0c4c6ab8c) wumpz *2019-08-19 08:07:41*

****


[55974](https://github.com/JSQLParser/JSqlParser/commit/55974c31d955565) wumpz *2019-08-14 15:23:23*

****


[c481c](https://github.com/JSQLParser/JSqlParser/commit/c481ce03c35ea0d) wumpz *2019-08-13 19:44:50*

**fixes #677**


[695d5](https://github.com/JSQLParser/JSqlParser/commit/695d532571c02c1) wumpz *2019-08-13 19:33:14*

**fixes #378**


[7a133](https://github.com/JSQLParser/JSqlParser/commit/7a133446d0c7fe4) wumpz *2019-08-13 19:26:13*

**fixes #377**


[9b6c4](https://github.com/JSQLParser/JSqlParser/commit/9b6c46c376f4591) wumpz *2019-08-13 19:24:23*

**fixes #489**


[27139](https://github.com/JSQLParser/JSqlParser/commit/27139a034edda7b) wumpz *2019-08-13 19:21:27*

**fixes #648**

* fixes #638 

[74e02](https://github.com/JSQLParser/JSqlParser/commit/74e02267404da4e) wumpz *2019-08-13 19:19:12*

****


[36907](https://github.com/JSQLParser/JSqlParser/commit/36907e4e1295fc1) wumpz *2019-08-11 22:25:40*

****


[48dbd](https://github.com/JSQLParser/JSqlParser/commit/48dbd58c6ad38bc) wumpz *2019-08-11 20:54:19*

****


[089e6](https://github.com/JSQLParser/JSqlParser/commit/089e6b4667ca921) wumpz *2019-08-11 20:32:24*

****


[ca821](https://github.com/JSQLParser/JSqlParser/commit/ca821e5277c8ffd) wumpz *2019-08-11 20:31:58*

****


[b3cfd](https://github.com/JSQLParser/JSqlParser/commit/b3cfdac1a31a8de) wumpz *2019-08-11 08:36:52*

****


[d86cb](https://github.com/JSQLParser/JSqlParser/commit/d86cb90b0f0ff5b) wumpz *2019-08-11 08:08:22*

****


[68d01](https://github.com/JSQLParser/JSqlParser/commit/68d01212af90b3d) wumpz *2019-08-11 08:06:16*

**fixes #838**


[1d1b6](https://github.com/JSQLParser/JSqlParser/commit/1d1b62507670a9b) wumpz *2019-08-09 21:22:53*

**fixes #826**


[7567d](https://github.com/JSQLParser/JSqlParser/commit/7567d25fdabf631) wumpz *2019-08-09 20:59:36*

**fixes #826**


[01296](https://github.com/JSQLParser/JSqlParser/commit/01296c302471a9c) wumpz *2019-08-09 20:49:18*

****


[30619](https://github.com/JSQLParser/JSqlParser/commit/30619d8f214d876) wumpz *2019-08-09 14:07:15*

**Delete ISSUE_TEMPLATE.md**


[9bea1](https://github.com/JSQLParser/JSqlParser/commit/9bea1e3850e2078) Tobias *2019-08-09 06:31:24*

**Update issue templates**


[1182e](https://github.com/JSQLParser/JSqlParser/commit/1182e8b792beac4) Tobias *2019-08-09 06:31:05*

**fixes #828**


[5139f](https://github.com/JSQLParser/JSqlParser/commit/5139fb279960013) wumpz *2019-08-08 21:16:23*

**fixes #828**


[8d0de](https://github.com/JSQLParser/JSqlParser/commit/8d0dec177748bdf) wumpz *2019-08-08 21:11:19*

****


[c766e](https://github.com/JSQLParser/JSqlParser/commit/c766ebce151708c) wumpz *2019-08-07 23:24:17*

****


[1a732](https://github.com/JSQLParser/JSqlParser/commit/1a732025a27872d) wumpz *2019-08-07 23:22:23*

****


[5c530](https://github.com/JSQLParser/JSqlParser/commit/5c5303eb8997f03) wumpz *2019-08-07 18:20:34*

**Updated test**


[47457](https://github.com/JSQLParser/JSqlParser/commit/47457ed95a8e688) Tomer S *2019-08-05 20:14:23*

**Fix issue of missing comma between joins in subjoin**


[92db2](https://github.com/JSQLParser/JSqlParser/commit/92db2d81fbed39d) Tomer S *2019-08-05 19:43:18*

**Fix issue of missing comma between joins in subjoin**


[449c4](https://github.com/JSQLParser/JSqlParser/commit/449c4e89c2a9c6a) Tomer S *2019-08-05 19:32:41*

****


[e586f](https://github.com/JSQLParser/JSqlParser/commit/e586fa31e251221) t.warneke@gmx.net *2019-08-03 23:27:55*

**Update latest version(1.4 => 2.1)**


[5768e](https://github.com/JSQLParser/JSqlParser/commit/5768ea92f63c2ab) yidasanqian *2019-08-02 09:29:48*

**added linkast to table**


[9c5f5](https://github.com/JSQLParser/JSqlParser/commit/9c5f52f3dddb10c) wumpz *2019-08-01 06:44:58*

**The duration part in INTERVAL expressions can contain a column and not only a constant - now supporting that use case**


[5492f](https://github.com/JSQLParser/JSqlParser/commit/5492f5105a75781) Tomer S *2019-07-26 17:15:25*

****


[1314c](https://github.com/JSQLParser/JSqlParser/commit/1314cd0e7f05543) wumpz *2019-07-22 22:00:36*

****


[2b944](https://github.com/JSQLParser/JSqlParser/commit/2b944807b6e9058) wumpz *2019-07-22 21:17:54*

****


[86693](https://github.com/JSQLParser/JSqlParser/commit/86693e0bf34fbf9) wumpz *2019-07-21 21:33:59*

**proof of correct parsing for #829**


[12ff2](https://github.com/JSQLParser/JSqlParser/commit/12ff225b0afd16f) wumpz *2019-07-21 21:27:36*

**fixes #830**


[a416b](https://github.com/JSQLParser/JSqlParser/commit/a416b96e7576dd0) wumpz *2019-07-21 21:11:51*

**fixes #830**


[5fc7c](https://github.com/JSQLParser/JSqlParser/commit/5fc7ce8f6cfeef6) wumpz *2019-07-21 21:08:22*

****


[914ee](https://github.com/JSQLParser/JSqlParser/commit/914ee73b6de95ae) wumpz *2019-07-21 20:42:54*

****


[0b404](https://github.com/JSQLParser/JSqlParser/commit/0b404539ab6e985) wumpz *2019-07-21 20:42:38*

**Update README.md**


[216bd](https://github.com/JSQLParser/JSqlParser/commit/216bd3733cacfee) Tobias *2019-07-19 06:23:59*

**Update README.md**


[7077d](https://github.com/JSQLParser/JSqlParser/commit/7077d2148bf9e06) Tobias *2019-07-18 05:58:47*

**Update README.md**


[6148a](https://github.com/JSQLParser/JSqlParser/commit/6148a895113ed5b) Tobias *2019-07-18 05:57:14*

**Update README.md**


[f28b6](https://github.com/JSQLParser/JSqlParser/commit/f28b6252683d2d4) Tobias *2019-07-18 05:56:14*

****


[34014](https://github.com/JSQLParser/JSqlParser/commit/34014c2b9ac862f) wumpz *2019-07-17 22:46:55*

**allow jdk 11 build**


[73be3](https://github.com/JSQLParser/JSqlParser/commit/73be39017363a4e) wumpz *2019-07-17 22:36:28*

**allow jdk 11 build**


[2cf8e](https://github.com/JSQLParser/JSqlParser/commit/2cf8e15b8987458) wumpz *2019-07-17 22:35:51*

**fixes limit as name for jdbc named parameters**


[7034c](https://github.com/JSQLParser/JSqlParser/commit/7034cb10c6abe11) wumpz *2019-07-17 22:03:55*

**Update README.md**


[bf942](https://github.com/JSQLParser/JSqlParser/commit/bf942f92098f5cc) Tobias *2019-07-12 20:37:33*

**Update FUNDING.yml**


[448da](https://github.com/JSQLParser/JSqlParser/commit/448da80f820a270) Tobias *2019-07-12 20:33:32*

**Create FUNDING.yml**


[73c82](https://github.com/JSQLParser/JSqlParser/commit/73c82ad776db592) Tobias *2019-07-12 20:32:32*

****


[55f20](https://github.com/JSQLParser/JSqlParser/commit/55f20e07faa13ee) wumpz *2019-07-10 23:12:27*

****


[27552](https://github.com/JSQLParser/JSqlParser/commit/275522f53782871) wumpz *2019-07-10 21:43:51*

**moved to java 8**


[bfb8e](https://github.com/JSQLParser/JSqlParser/commit/bfb8e274f247350) wumpz *2019-07-09 22:41:18*

****


[39c6a](https://github.com/JSQLParser/JSqlParser/commit/39c6a4885f7bd20) wumpz *2019-07-09 22:31:35*

****


[6001f](https://github.com/JSQLParser/JSqlParser/commit/6001fdecada77fa) wumpz *2019-07-09 21:15:42*

****


[cfc3f](https://github.com/JSQLParser/JSqlParser/commit/cfc3f64850410bc) wumpz *2019-07-09 21:12:01*

**Support default mode in full text search**


[ecb54](https://github.com/JSQLParser/JSqlParser/commit/ecb5464ccb6bab4) Tomer S *2019-07-07 18:04:00*

****


[0022e](https://github.com/JSQLParser/JSqlParser/commit/0022ef4889f39de) wumpz *2019-07-07 12:11:44*

****


[694d0](https://github.com/JSQLParser/JSqlParser/commit/694d06ccf707494) wumpz *2019-07-04 21:51:34*

**Add support for full text search (MATCH..AGAINST)**


[6750a](https://github.com/JSQLParser/JSqlParser/commit/6750a5360084439) Tomer S *2019-07-04 21:23:25*

****


[c88f5](https://github.com/JSQLParser/JSqlParser/commit/c88f5ba08f4ee41) wumpz *2019-07-04 20:51:01*

**Adding support for IS [NOT] TRUE/FALSE expressions**


[00839](https://github.com/JSQLParser/JSqlParser/commit/0083971851df6d4) Tomer S *2019-07-02 21:39:16*

**Adding support for the DIV operator**


[c8bbc](https://github.com/JSQLParser/JSqlParser/commit/c8bbc0f7ecf198e) Tomer S *2019-07-02 19:28:47*

**fixes #200 - was already fixed, introduced test case**


[4d100](https://github.com/JSQLParser/JSqlParser/commit/4d100a7c011abda) wumpz *2019-07-02 12:43:46*

**fixes #259 - was already fixed, introduced test case**


[500ee](https://github.com/JSQLParser/JSqlParser/commit/500ee6e5010fe79) wumpz *2019-07-02 12:40:33*

**fixes #262 - was already fixed, introduced test case**


[dbdfb](https://github.com/JSQLParser/JSqlParser/commit/dbdfb4ea45121c9) wumpz *2019-07-02 12:36:31*

**fixes #113**


[cd16a](https://github.com/JSQLParser/JSqlParser/commit/cd16a6d911fe8b2) wumpz *2019-07-02 12:28:02*

****


[424c8](https://github.com/JSQLParser/JSqlParser/commit/424c81ce91887d5) wumpz *2019-06-30 19:55:27*

****


[d6949](https://github.com/JSQLParser/JSqlParser/commit/d6949999cd77c97) wumpz *2019-06-30 17:08:23*

**Add support for STRAIGHT_JOIN**


[89089](https://github.com/JSQLParser/JSqlParser/commit/890898806d2b6b3) Tomer S *2019-04-04 17:15:48*


## jsqlparser-2.1 (2019-06-30)

### Other changes

**fixes #812**


[38aad](https://github.com/JSQLParser/JSqlParser/commit/38aadee9b2a941b) wumpz *2019-06-25 23:07:07*

**Update README.md**


[4763d](https://github.com/JSQLParser/JSqlParser/commit/4763d455148bb75) Tobias *2019-06-25 12:28:29*

**Update README.md**


[334b5](https://github.com/JSQLParser/JSqlParser/commit/334b5498859733d) Tobias *2019-06-25 12:27:52*

**Support KSQL's WINDOW**

* Add support for KSQL&#x27;s WINDOW (HOPPING, TUMBLING and SESSION window) 

[ef911](https://github.com/JSQLParser/JSqlParser/commit/ef9119806146f25) Suyash Garg *2019-06-21 12:00:06*

**downgraded javacc version to allow java 7 build**


[6e7b9](https://github.com/JSQLParser/JSqlParser/commit/6e7b976d65d7eb6) wumpz *2019-06-19 07:33:53*

**downgraded checkstyle version to allow java 7 build**


[a9c29](https://github.com/JSQLParser/JSqlParser/commit/a9c29ffceab9e58) wumpz *2019-06-19 07:29:55*

**Update README.md**


[b5915](https://github.com/JSQLParser/JSqlParser/commit/b59151eae9d2ac5) Tobias *2019-06-16 22:11:11*

****


[afcc0](https://github.com/JSQLParser/JSqlParser/commit/afcc0a9b2063ade) wumpz *2019-06-16 21:49:09*

****


[1d203](https://github.com/JSQLParser/JSqlParser/commit/1d203850be1679c) wumpz *2019-06-16 21:06:13*

**fixes #789**


[cdf80](https://github.com/JSQLParser/JSqlParser/commit/cdf805f4028b801) wumpz *2019-06-16 21:02:47*

**fixes #450**


[83dba](https://github.com/JSQLParser/JSqlParser/commit/83dbac2d9841d21) wumpz *2019-06-13 22:11:29*

**support postgresql create index syntax**


[9d74c](https://github.com/JSQLParser/JSqlParser/commit/9d74c6da03976ea) theodore johnson *2019-06-13 22:02:11*

**fixes #705**


[9ce65](https://github.com/JSQLParser/JSqlParser/commit/9ce65cde0f25ae1) wumpz *2019-06-13 21:38:14*

**site update**


[89f20](https://github.com/JSQLParser/JSqlParser/commit/89f202a3062b30a) wumpz *2019-05-29 13:40:18*

**fixes #798**


[dd806](https://github.com/JSQLParser/JSqlParser/commit/dd806991c4283d0) wumpz *2019-05-24 21:09:13*

**fixes #796**


[aecc4](https://github.com/JSQLParser/JSqlParser/commit/aecc41442a8dfd3) wumpz *2019-05-18 21:00:13*

**fixes #785**


[f59f2](https://github.com/JSQLParser/JSqlParser/commit/f59f2b5c9b8e33e) wumpz *2019-05-04 22:54:28*

**Fix #786 (#787)**


[f2aba](https://github.com/JSQLParser/JSqlParser/commit/f2aba0b4ef018a1) Ryan J Murphy *2019-04-22 22:28:38*

****


[44ff9](https://github.com/JSQLParser/JSqlParser/commit/44ff9ed6bd0d39d) wumpz *2019-04-22 22:28:10*

**fixes #773 added nextval as a valid object name**


[94a2a](https://github.com/JSQLParser/JSqlParser/commit/94a2a40fa7f93a2) wumpz *2019-04-17 08:15:50*

****


[85a3e](https://github.com/JSQLParser/JSqlParser/commit/85a3e69fed2db1a) wumpz *2019-04-17 07:05:47*

****


[8d4b3](https://github.com/JSQLParser/JSqlParser/commit/8d4b32a28e3eac1) wumpz *2019-04-17 07:00:01*

****


[703a7](https://github.com/JSQLParser/JSqlParser/commit/703a7459a529343) wumpz *2019-04-17 06:43:37*

**recreated "old" javadocs (without improving it)**


[74e2a](https://github.com/JSQLParser/JSqlParser/commit/74e2a4b4498088e) wumpz *2019-04-17 06:19:27*

**recreated "old" javadocs (without improving it)**


[4fed7](https://github.com/JSQLParser/JSqlParser/commit/4fed7536c8036ed) wumpz *2019-04-16 07:59:20*

**JavaDoc for Column#getTable (#782)**


[4f500](https://github.com/JSQLParser/JSqlParser/commit/4f500a48572234a) Andrea Arcuri *2019-04-15 12:01:37*

****


[e2168](https://github.com/JSQLParser/JSqlParser/commit/e2168407d929c77) wumpz *2019-04-13 23:57:14*

**fixes #777**


[35a1c](https://github.com/JSQLParser/JSqlParser/commit/35a1c97f3609007) wumpz *2019-04-12 22:30:00*

**tests #775**

* removed some not flags from some classes 

[8dda4](https://github.com/JSQLParser/JSqlParser/commit/8dda4a60a8e558d) wumpz *2019-04-08 21:36:40*

**tests #754**


[97797](https://github.com/JSQLParser/JSqlParser/commit/97797425f635621) wumpz *2019-03-31 21:22:51*

**fixes #770**


[86ea6](https://github.com/JSQLParser/JSqlParser/commit/86ea6016636fe5b) wumpz *2019-03-28 22:19:33*

****


[f958f](https://github.com/JSQLParser/JSqlParser/commit/f958fa741dea3d0) wumpz *2019-03-20 22:41:16*

****


[b1da6](https://github.com/JSQLParser/JSqlParser/commit/b1da6e5225a2269) wumpz *2019-03-20 22:37:59*

**fixes #766**


[1bb5c](https://github.com/JSQLParser/JSqlParser/commit/1bb5c3d5823560d) wumpz *2019-03-20 22:05:30*

**fixes #755 - corrected error introduced due to corrected ExpressionDeParser.**


[0002c](https://github.com/JSQLParser/JSqlParser/commit/0002cb717106722) wumpz *2019-03-20 09:57:29*

**fixes #755**


[a6905](https://github.com/JSQLParser/JSqlParser/commit/a690558d1d8f19d) wumpz *2019-03-20 09:38:36*

****


[aff50](https://github.com/JSQLParser/JSqlParser/commit/aff505390efcb9c) wumpz *2019-03-20 06:46:44*

****


[7da90](https://github.com/JSQLParser/JSqlParser/commit/7da901adcb0370d) wumpz *2019-03-20 06:45:31*

****


[18a3c](https://github.com/JSQLParser/JSqlParser/commit/18a3c4acb6f84bb) wumpz *2019-03-20 06:35:17*

**activated new checkstyle plugin only if used java is at least 1.8**


[1b4e9](https://github.com/JSQLParser/JSqlParser/commit/1b4e9957ecd9daf) wumpz *2019-03-17 23:00:12*

****


[6ceb4](https://github.com/JSQLParser/JSqlParser/commit/6ceb4062d3ccb2c) wumpz *2019-03-16 22:56:17*

**upgraded checkstyle due to security alert**


[cb172](https://github.com/JSQLParser/JSqlParser/commit/cb1726e478d39e6) wumpz *2019-03-16 22:40:36*

**Fixed typos in README.md (#760)**


[67dce](https://github.com/JSQLParser/JSqlParser/commit/67dce2f40b4e484) alterdego *2019-03-14 12:20:26*

**update README.md (#762)**

* update latest version(1.4) 

[13d6a](https://github.com/JSQLParser/JSqlParser/commit/13d6a9fe183a5ab) r548 *2019-03-14 12:19:58*

****


[46323](https://github.com/JSQLParser/JSqlParser/commit/46323b4df4119e6) wumpz *2019-03-12 07:56:12*


## jsqlparser-2.0 (2019-03-16)

### Other changes

****


[526b9](https://github.com/JSQLParser/JSqlParser/commit/526b90b8d353a01) wumpz *2019-03-16 22:16:01*

**corrected test**


[4ad79](https://github.com/JSQLParser/JSqlParser/commit/4ad79d05b5306c2) wumpz *2019-03-04 18:46:57*

**fixes #17**


[1ef39](https://github.com/JSQLParser/JSqlParser/commit/1ef39301666c3e3) wumpz *2019-03-04 00:26:21*

**refactored group by expressions into separate class, first step to support grouping sets**


[82f3d](https://github.com/JSQLParser/JSqlParser/commit/82f3da8ce946d70) wumpz *2019-03-03 22:25:31*

****


[749ad](https://github.com/JSQLParser/JSqlParser/commit/749ad556d917a01) wumpz *2019-02-26 23:09:43*

**Fixes 649 to add support for HOUR, MINUTE, SECOND date literals and support for identifiers as the interval parameter. (#756)**


[cbcf0](https://github.com/JSQLParser/JSqlParser/commit/cbcf0a73d516bfc) thebiguno *2019-02-25 22:23:16*

****


[c85e7](https://github.com/JSQLParser/JSqlParser/commit/c85e79b3cd75119) wumpz *2019-02-24 22:17:37*

****


[0f9bb](https://github.com/JSQLParser/JSqlParser/commit/0f9bb4e6272f71b) wumpz *2019-02-24 19:59:06*

**fixes #649**

* and implemented ! for not and extended not expression 

[10e8e](https://github.com/JSQLParser/JSqlParser/commit/10e8e2568eb7711) wumpz *2019-02-23 23:32:38*

****


[15297](https://github.com/JSQLParser/JSqlParser/commit/15297868572dba9) wumpz *2019-02-20 23:30:16*

****


[12c05](https://github.com/JSQLParser/JSqlParser/commit/12c056451444678) wumpz *2019-02-20 23:20:35*

****


[14f92](https://github.com/JSQLParser/JSqlParser/commit/14f92b13cc4503f) wumpz *2019-02-20 23:05:20*

**fixes #164**


[8c057](https://github.com/JSQLParser/JSqlParser/commit/8c057ab735f0249) wumpz *2019-02-20 22:41:50*

**fixes #169**


[e1193](https://github.com/JSQLParser/JSqlParser/commit/e1193a63a6551ba) wumpz *2019-02-20 21:43:45*

**fixes #479**


[0b5f5](https://github.com/JSQLParser/JSqlParser/commit/0b5f586cf3f9250) wumpz *2019-02-19 22:29:58*

**fixes #479**


[b029b](https://github.com/JSQLParser/JSqlParser/commit/b029bb5860b3ed3) wumpz *2019-02-19 22:28:06*

**Update README.md**


[fa162](https://github.com/JSQLParser/JSqlParser/commit/fa1624165e80895) Tobias *2019-02-19 17:31:13*

**Added support for DROP INDEX, ADD UNIQUE INDEX, ALGORITHM and USING (#752)**

* Merge recent changes in the master from the master (#1) 
* changed license header to represent the projects dual license 
* changed license header to represent the projects dual license 
* changed license header to represent the projects dual license 
* changed license header to represent the projects dual license 
* Added support for comment(s) for column definitions in CREATE TABLE s… (#743) 
* Added support for comment(s) for column definitions in CREATE TABLE statements 
* Added support for comment(s) for column definitions in CREATE TABLE statements #2 
* To increase code coverage 
* To increase code coverage #2 
* Added support for &#x27;ALTER TABLE CHANGE COLUMN&#x27; (#741) 
* Added support for &#x27;ALTER TABLE CHANGE COLUMN oldName newName columnDefinition&#x27;. Please see https://dev.mysql.com/doc/refman/8.0/en/alter-table.html for reference. 
* Returned import ordering to avoid conflicts 
* Improved the tests somewhat 
* Now also test the getOptionalSpecifier() for both cases (null and not-null) 
* Expanded tests for ALTER TABLE ... CHANGE 
* implemented optimize for, fixes #348 
* implemented optimize for, fixes #348 
* Support for simple informix outer joins. (#745) 
* added support for simple informix outer joins 
* added some test code 
* added support for simple informix outer joins 
* added some test code 
* more testing for better code coverage 
* added support for simple informix outer joins 
* added some test code 
* more testing for better code coverage 
* fixes #747 
* fixes #733 
* fixes #707 
* Update README.md 
* Update README.md 
* Fix handles the following cases: 1) DROP INDEX 2) ADD UNIQUE INDEX 3) ALGORITHM 4) USING &lt;index type&gt; 

[2830c](https://github.com/JSQLParser/JSqlParser/commit/2830c17ea226635) Prateek Gupta *2019-02-19 00:44:35*

**fixes #753**


[3209a](https://github.com/JSQLParser/JSqlParser/commit/3209a16c55c1976) wumpz *2019-02-19 00:35:14*

**Update README.md**


[4f74f](https://github.com/JSQLParser/JSqlParser/commit/4f74f6d110166a8) Tobias *2019-02-15 21:12:05*

**Update README.md**


[f9609](https://github.com/JSQLParser/JSqlParser/commit/f960991759ed604) Tobias *2019-02-15 21:07:41*

**fixes #707**


[a1c4f](https://github.com/JSQLParser/JSqlParser/commit/a1c4f4a7ddda62e) wumpz *2019-02-14 22:50:16*

****


[6c413](https://github.com/JSQLParser/JSqlParser/commit/6c413b404b1bd27) wumpz *2019-02-11 23:07:46*

**fixes #733**


[6da69](https://github.com/JSQLParser/JSqlParser/commit/6da696b496eb794) wumpz *2019-02-10 23:11:54*

**fixes #747**


[d3553](https://github.com/JSQLParser/JSqlParser/commit/d3553bc7f8c3b12) wumpz *2019-02-10 23:00:02*

****


[73305](https://github.com/JSQLParser/JSqlParser/commit/73305fe6a6efec1) wumpz *2019-02-10 22:55:01*

****


[f79f5](https://github.com/JSQLParser/JSqlParser/commit/f79f58d9f8f5c73) wumpz *2019-02-10 22:38:19*

****


[154cf](https://github.com/JSQLParser/JSqlParser/commit/154cf371d775b9b) wumpz *2019-02-10 21:54:45*

****


[80153](https://github.com/JSQLParser/JSqlParser/commit/80153dfdf60f9b4) wumpz *2019-02-10 21:14:10*

****


[04151](https://github.com/JSQLParser/JSqlParser/commit/0415140ecf58894) wumpz *2019-02-08 07:13:18*

**Support for simple informix outer joins. (#745)**

* added support for simple informix outer joins 
* added some test code 
* added support for simple informix outer joins 
* added some test code 
* more testing for better code coverage 
* added support for simple informix outer joins 
* added some test code 
* more testing for better code coverage 

[53e24](https://github.com/JSQLParser/JSqlParser/commit/53e247bffdae557) Kurt Schwitters *2019-02-08 05:52:51*

****


[1a3d2](https://github.com/JSQLParser/JSqlParser/commit/1a3d289238eec35) wumpz *2019-02-07 23:30:55*

****


[9ec7d](https://github.com/JSQLParser/JSqlParser/commit/9ec7d8572ca0424) wumpz *2019-02-07 23:30:00*

****


[63477](https://github.com/JSQLParser/JSqlParser/commit/6347776ad571d92) wumpz *2019-02-07 23:25:29*

**implemented optimize for, fixes #348**


[ff23d](https://github.com/JSQLParser/JSqlParser/commit/ff23dde74433355) wumpz *2019-02-07 23:21:22*

**implemented optimize for, fixes #348**


[4b0b5](https://github.com/JSQLParser/JSqlParser/commit/4b0b5cb044bc973) wumpz *2019-02-07 23:19:46*

**Added support for 'ALTER TABLE CHANGE COLUMN' (#741)**

* Added support for &#x27;ALTER TABLE CHANGE COLUMN oldName newName columnDefinition&#x27;. Please see https://dev.mysql.com/doc/refman/8.0/en/alter-table.html for reference. 
* Returned import ordering to avoid conflicts 
* Improved the tests somewhat 
* Now also test the getOptionalSpecifier() for both cases (null and not-null) 
* Expanded tests for ALTER TABLE ... CHANGE 

[bfb80](https://github.com/JSQLParser/JSqlParser/commit/bfb8023318c31b2) Simon *2019-02-07 14:49:28*

**Added support for comment(s) for column definitions in CREATE TABLE s… (#743)**

* Added support for comment(s) for column definitions in CREATE TABLE statements 
* Added support for comment(s) for column definitions in CREATE TABLE statements #2 
* To increase code coverage 
* To increase code coverage #2 

[07b86](https://github.com/JSQLParser/JSqlParser/commit/07b86761d0b4aee) Prateek Gupta *2019-02-07 07:09:26*

**changed license header to represent the projects dual license**


[b9023](https://github.com/JSQLParser/JSqlParser/commit/b90234f0fc39d34) wumpz *2019-02-06 20:53:15*

**changed license header to represent the projects dual license**


[2adec](https://github.com/JSQLParser/JSqlParser/commit/2adec6ad552f770) wumpz *2019-02-06 20:48:58*

**changed license header to represent the projects dual license**


[4ad99](https://github.com/JSQLParser/JSqlParser/commit/4ad996f778f043b) wumpz *2019-02-06 20:47:19*

**changed license header to represent the projects dual license**


[373c6](https://github.com/JSQLParser/JSqlParser/commit/373c6cb59734091) wumpz *2019-02-05 23:04:45*

**integrated some additional AST - Nodes for InExpression and SimpleExpressionList**


[cb0e0](https://github.com/JSQLParser/JSqlParser/commit/cb0e0b79564258a) wumpz *2019-02-04 01:17:35*

****


[4e0a7](https://github.com/JSQLParser/JSqlParser/commit/4e0a7326a9d9e9e) wumpz *2019-02-03 23:20:03*

**case else corrected to allow conditions here as well**


[4f7a1](https://github.com/JSQLParser/JSqlParser/commit/4f7a1537e7ba368) wumpz *2019-02-03 22:53:22*

**refactored outer not from sqlconditions, regularconditions to condition**


[aae36](https://github.com/JSQLParser/JSqlParser/commit/aae36da486303f8) wumpz *2019-02-03 21:07:08*

**refactored outer not from sqlconditions, regularconditions to condition**


[1956b](https://github.com/JSQLParser/JSqlParser/commit/1956b84395fcab9) wumpz *2019-02-03 20:57:41*

**strange not problem**


[ba3bf](https://github.com/JSQLParser/JSqlParser/commit/ba3bf2241c1021b) wumpz *2019-02-03 19:57:00*

**named exec procedure parameters**


[82b28](https://github.com/JSQLParser/JSqlParser/commit/82b287dae64ac43) wumpz *2019-02-01 23:51:28*

**finished multi value set**


[479ee](https://github.com/JSQLParser/JSqlParser/commit/479ee39d6efbb6d) wumpz *2019-01-27 00:37:33*

**finished multi value set**


[ba4b0](https://github.com/JSQLParser/JSqlParser/commit/ba4b0ccee2cf20a) wumpz *2019-01-27 00:35:17*

**started multivalue set**


[d991b](https://github.com/JSQLParser/JSqlParser/commit/d991bbe377418a1) wumpz *2019-01-26 00:44:59*

**corrected typing error. Both licenses could not be applied at the same time.**


[7cd18](https://github.com/JSQLParser/JSqlParser/commit/7cd189c326d3d84) wumpz *2019-01-23 23:22:12*

**allow top keyword as column / table / alias name**

* implemented tests 

[9e81e](https://github.com/JSQLParser/JSqlParser/commit/9e81e1592200952) wumpz *2019-01-23 23:00:12*

**allow top keyword as column / table / alias name**

* implemented tests 

[ed95e](https://github.com/JSQLParser/JSqlParser/commit/ed95e877802f46e) wumpz *2019-01-23 22:58:39*

**implemented _utf8**


[fe28e](https://github.com/JSQLParser/JSqlParser/commit/fe28e88d948dc7e) wumpz *2019-01-23 21:36:42*

**implemented explain select**


[e5c6f](https://github.com/JSQLParser/JSqlParser/commit/e5c6ff6fdf3d4b2) wumpz *2019-01-22 23:28:33*

**implemented explain select**


[fb45d](https://github.com/JSQLParser/JSqlParser/commit/fb45dd7c77ccff0) wumpz *2019-01-22 23:06:34*

**Add support for casting to signed integer (#734)**


[5b00a](https://github.com/JSQLParser/JSqlParser/commit/5b00ace4d49b731) tomershay *2019-01-21 22:58:57*

**corrected stackoverflow while tables extraction**

* updated readme 

[04db1](https://github.com/JSQLParser/JSqlParser/commit/04db124b85dea22) wumpz *2019-01-20 22:33:51*

**implemented DescribeStatement, corrected TableNamesFinder, corrected corresponding interfaces and adapters, implemented tests.**


[1c411](https://github.com/JSQLParser/JSqlParser/commit/1c411f490e91f16) wumpz *2019-01-20 22:24:47*

**started describe**

* some cleanup 

[25fa3](https://github.com/JSQLParser/JSqlParser/commit/25fa31153a9a2d0) wumpz *2019-01-20 21:48:12*


## jsqlparser-1.4 (2019-01-09)

### Other changes

**Support Alter Table Add Unique Constraint (#708)**


[a54ea](https://github.com/JSQLParser/JSqlParser/commit/a54ea143e7eeffe) Robert Scholte *2018-12-30 23:37:29*

**corrected some failing tests**

* included a regression test for oracle files 

[aa932](https://github.com/JSQLParser/JSqlParser/commit/aa932c4a28d8021) wumpz *2018-12-30 23:06:59*

**Add 'SHOW COLUMNS FROM table' (#728)**


[f1724](https://github.com/JSQLParser/JSqlParser/commit/f1724a468577935) Ohad Shai *2018-12-28 11:45:53*

**Support Alter Table Drop Constraint If Exists (#709)**

* Support Alter Table Drop Constraint If Exists 
* #709 add constraintIfExists flag 

[08fed](https://github.com/JSQLParser/JSqlParser/commit/08fedf752a3f99d) Robert Scholte *2018-12-13 07:14:10*

**Support Alter Table Alter Column Type (#724)**


[b3263](https://github.com/JSQLParser/JSqlParser/commit/b3263c8cbae9296) Robert Scholte *2018-12-13 07:13:12*

**Support KSQL's WITHIN (#722)**

* Implements WITHIN for KSQL windowed joins 
* Clean up 
* Improve test 
* Implements WITHIN ( before TimeUnit, after TimeUnit ) for KSQL 
* Also restricts TimeUnit to units accepted by KSQL 
* WITHIN should come before ON 

[ae665](https://github.com/JSQLParser/JSqlParser/commit/ae665e60655f45f) Lionel Montrieux *2018-12-11 21:53:07*

**Add support for truncate table (#719)**


[11be7](https://github.com/JSQLParser/JSqlParser/commit/11be71561824c36) Bartosz Firyn *2018-12-06 06:41:30*

**fixes #718**


[c0801](https://github.com/JSQLParser/JSqlParser/commit/c0801a742c57ea0) wumpz *2018-12-04 15:23:42*

**added test for issue #716**


[f06b1](https://github.com/JSQLParser/JSqlParser/commit/f06b19769b2eb2a) wumpz *2018-11-22 21:11:12*

**REPLACE VIEW as a synonym to ALTER VIEW (#711)**


[58d39](https://github.com/JSQLParser/JSqlParser/commit/58d3965914376bf) theodore johnson *2018-11-19 14:50:36*

**Update README.md**


[7b6cf](https://github.com/JSQLParser/JSqlParser/commit/7b6cff1222e1314) Tobias *2018-10-26 19:39:58*

**Update README.md**


[78ebf](https://github.com/JSQLParser/JSqlParser/commit/78ebf2093a98fb5) Tobias *2018-10-26 19:38:42*

****


[22c04](https://github.com/JSQLParser/JSqlParser/commit/22c04947e3834e6) wumpz *2018-10-26 11:12:37*

**some cleaning up for pr #702**


[b7754](https://github.com/JSQLParser/JSqlParser/commit/b7754e8003adec6) wumpz *2018-10-25 12:40:09*

**support named parameters (#702)**


[eacb1](https://github.com/JSQLParser/JSqlParser/commit/eacb161fe848237) theodore johnson *2018-10-25 10:52:41*

**Added Oracle COMMENT statement (#685)**


[e79cc](https://github.com/JSQLParser/JSqlParser/commit/e79ccc61b5bc658) hishidama *2018-10-25 10:40:33*

**fix JSQLParser/JSqlParser #679 (#703)**


[0e35e](https://github.com/JSQLParser/JSqlParser/commit/0e35e224766ebb7) softommy *2018-10-25 10:35:19*

**fixes #694**


[48544](https://github.com/JSQLParser/JSqlParser/commit/48544387cf2e3b5) wumpz *2018-10-18 20:50:58*

**fixes #675**


[d95e0](https://github.com/JSQLParser/JSqlParser/commit/d95e034f4c1b079) wumpz *2018-10-12 22:04:57*

****


[5aa63](https://github.com/JSQLParser/JSqlParser/commit/5aa6304559a5863) wumpz *2018-10-12 21:26:26*

****


[295c1](https://github.com/JSQLParser/JSqlParser/commit/295c1147e4ef513) wumpz *2018-10-10 21:02:12*

****


[a838f](https://github.com/JSQLParser/JSqlParser/commit/a838fec1051db27) wumpz *2018-10-06 23:43:29*

**fixes #561**


[f5982](https://github.com/JSQLParser/JSqlParser/commit/f598213f071d205) wumpz *2018-10-05 20:17:59*

**fixes #561**


[e170c](https://github.com/JSQLParser/JSqlParser/commit/e170c4f17dc9ad7) wumpz *2018-10-05 20:15:58*

**integrated test for values query**


[96499](https://github.com/JSQLParser/JSqlParser/commit/964991c4207c3bf) wumpz *2018-10-04 23:19:12*

**integrated values statement**


[c0327](https://github.com/JSQLParser/JSqlParser/commit/c032744b9e083e0) wumpz *2018-10-04 22:29:11*

**fixes some lgtm alerts**


[de8ad](https://github.com/JSQLParser/JSqlParser/commit/de8ad10a791705e) wumpz *2018-10-04 08:14:00*

**fixes #684 (second)**


[2742a](https://github.com/JSQLParser/JSqlParser/commit/2742a889c4c4f0b) wumpz *2018-10-03 19:40:47*

**Update README.md**


[77026](https://github.com/JSQLParser/JSqlParser/commit/770264da8955512) Tobias *2018-10-02 22:17:03*


## jsqlparser-1.3 (2018-10-02)

### Other changes

**fixes #684**


[a8b3e](https://github.com/JSQLParser/JSqlParser/commit/a8b3e71a88735dc) wumpz *2018-10-02 21:59:16*

**fixes #682**


[c2833](https://github.com/JSQLParser/JSqlParser/commit/c28331102e5754e) wumpz *2018-09-20 05:48:34*

**Add LGTM.com code quality badges (#680)**


[3f620](https://github.com/JSQLParser/JSqlParser/commit/3f620c9e2ddeeb4) Xavier RENE-CORAIL *2018-09-15 21:17:16*

**fixes #670**

* added testcase, corrected deparser 

[455c5](https://github.com/JSQLParser/JSqlParser/commit/455c5f50671eed9) wumpz *2018-09-10 15:38:54*

**#670 (#671)**


[3950b](https://github.com/JSQLParser/JSqlParser/commit/3950b1962f32043) mgierw *2018-09-10 15:21:40*

**testcase for #670**


[e6b9a](https://github.com/JSQLParser/JSqlParser/commit/e6b9afdb205bf2b) wumpz *2018-09-10 15:13:58*

**fixes #676**


[1786e](https://github.com/JSQLParser/JSqlParser/commit/1786e215474e8da) wumpz *2018-09-08 23:44:37*

**fixes #676**


[e32c5](https://github.com/JSQLParser/JSqlParser/commit/e32c502f91ed04a) wumpz *2018-09-08 22:12:46*

****


[26b2c](https://github.com/JSQLParser/JSqlParser/commit/26b2c11df4c4ac5) wumpz *2018-09-07 21:59:47*

****


[0b03c](https://github.com/JSQLParser/JSqlParser/commit/0b03c858b897713) wumpz *2018-09-07 21:48:03*

**Update README.md (#667)**

* Fix a couple typos/grammar issues 

[a822f](https://github.com/JSQLParser/JSqlParser/commit/a822fead1b21793) Kai Presler-Marshall *2018-08-29 13:11:41*

**fixes #665**


[b806e](https://github.com/JSQLParser/JSqlParser/commit/b806e9c1897a0f3) wumpz *2018-08-27 06:51:39*

**fixes #367**


[14791](https://github.com/JSQLParser/JSqlParser/commit/1479149af6a0c5c) wumpz *2018-08-25 23:12:32*

**fixes #367**


[f6abb](https://github.com/JSQLParser/JSqlParser/commit/f6abb9e7edec3f1) wumpz *2018-08-25 23:10:45*

**fixes #367**


[01bcb](https://github.com/JSQLParser/JSqlParser/commit/01bcbd5255a0ebd) wumpz *2018-08-25 23:07:14*

****


[1a90b](https://github.com/JSQLParser/JSqlParser/commit/1a90b16e215ac80) wumpz *2018-08-23 22:11:35*

****


[bfbfc](https://github.com/JSQLParser/JSqlParser/commit/bfbfc76eb006201) wumpz *2018-08-23 20:26:56*

**make the CreateTable deparser use the accept/visit schema instead of the toString path (#663)**


[68194](https://github.com/JSQLParser/JSqlParser/commit/68194bfd6bf57d1) theodore johnson *2018-08-23 04:21:10*

**fixes #661**


[44ea8](https://github.com/JSQLParser/JSqlParser/commit/44ea8bd33e01f62) wumpz *2018-08-17 06:22:30*

**Parse Cloud Spanner raw string and byte prefixes (#659)**

* #656 parse cloud spanner raw string and byte literals 
* #656 fixed raw byte string prefix 
* #656 fixed test case 
* fixed reported codacy issue 

[a57db](https://github.com/JSQLParser/JSqlParser/commit/a57db5d031a5229) Knut Olav Løite *2018-08-14 08:12:26*

****


[78ff2](https://github.com/JSQLParser/JSqlParser/commit/78ff2ad4bc7496b) wumpz *2018-08-14 08:08:17*

****


[1d138](https://github.com/JSQLParser/JSqlParser/commit/1d13897883c1448) wumpz *2018-07-26 19:58:18*

****


[62bfd](https://github.com/JSQLParser/JSqlParser/commit/62bfda765247f41) wumpz *2018-07-26 09:03:10*

**fixes #643**


[dc779](https://github.com/JSQLParser/JSqlParser/commit/dc779cae57b993e) wumpz *2018-07-23 14:57:23*

**add test, fix style**


[98f87](https://github.com/JSQLParser/JSqlParser/commit/98f873e244046ff) theodore johnson *2018-07-16 21:35:35*

**added a test**


[cfa7e](https://github.com/JSQLParser/JSqlParser/commit/cfa7e8986c98701) theodore johnson *2018-07-16 20:09:34*

**support teradata sortcut for Select**


[53c90](https://github.com/JSQLParser/JSqlParser/commit/53c90e9988aec49) theodore johnson *2018-07-11 18:46:52*

**fix parsing and rendering of Truncate**


[f126d](https://github.com/JSQLParser/JSqlParser/commit/f126d1575bc2ea5) theodore johnson *2018-07-11 17:56:14*

**fixes #273**


[6cd54](https://github.com/JSQLParser/JSqlParser/commit/6cd5481af400e20) wumpz *2018-07-06 08:44:03*

**fixes #273**


[12b4a](https://github.com/JSQLParser/JSqlParser/commit/12b4a1ecaa1090c) wumpz *2018-07-06 08:31:55*

**fixes #573**


[3a5a6](https://github.com/JSQLParser/JSqlParser/commit/3a5a625d15d7b30) wumpz *2018-07-06 07:01:12*

**add support for && operator**


[b59b4](https://github.com/JSQLParser/JSqlParser/commit/b59b48ec2e7e88a) Tomer S *2018-07-03 14:19:34*

**Extract "orderBy" and "Partition" classes from AnalyticExpression.**


[8da6e](https://github.com/JSQLParser/JSqlParser/commit/8da6e2e538ceea9) Assaf *2018-06-20 06:41:56*

**Update README.md**


[aff65](https://github.com/JSQLParser/JSqlParser/commit/aff651624e6fb48) Tobias *2018-06-18 06:22:15*

**fixes #608**


[d529c](https://github.com/JSQLParser/JSqlParser/commit/d529c82d24cbaba) wumpz *2018-06-17 09:57:15*

**fixes #608**


[3f5e3](https://github.com/JSQLParser/JSqlParser/commit/3f5e3c9805bd2fe) wumpz *2018-06-17 09:48:18*

**fixes #163**


[6d954](https://github.com/JSQLParser/JSqlParser/commit/6d95472726574cc) wumpz *2018-06-17 09:39:36*

**fixes #163**


[1db3f](https://github.com/JSQLParser/JSqlParser/commit/1db3ff5f9b5837b) wumpz *2018-06-17 09:33:54*

**first try dotted**


[2fab8](https://github.com/JSQLParser/JSqlParser/commit/2fab86081a56673) wumpz *2018-06-16 21:07:22*

**fixes #620**


[645d0](https://github.com/JSQLParser/JSqlParser/commit/645d06e0a31206b) wumpz *2018-06-08 23:22:16*

**fixes #612**


[36ee6](https://github.com/JSQLParser/JSqlParser/commit/36ee6c54c21965a) wumpz *2018-06-08 22:51:12*

**fixes #612**


[60473](https://github.com/JSQLParser/JSqlParser/commit/60473edf4efbd85) wumpz *2018-06-08 22:50:02*

**Update README.md**


[c5a0b](https://github.com/JSQLParser/JSqlParser/commit/c5a0b02b5f5c077) Tobias *2018-05-16 06:18:32*

**Add javadoc badge**


[9c2c9](https://github.com/JSQLParser/JSqlParser/commit/9c2c96772ba9b5c) Jeremy Lin *2018-05-15 20:32:26*

**add maven-central badge (#616)**


[4d67d](https://github.com/JSQLParser/JSqlParser/commit/4d67d10bbf9a033) Benedikt Waldvogel *2018-05-14 09:13:39*

**fixes #614**


[32c4d](https://github.com/JSQLParser/JSqlParser/commit/32c4d8a04244fd5) wumpz *2018-05-12 22:51:33*

****


[094cc](https://github.com/JSQLParser/JSqlParser/commit/094cc8082e40442) wumpz *2018-05-02 21:16:26*

**introduced junit annotations**


[d7c1e](https://github.com/JSQLParser/JSqlParser/commit/d7c1e438c8f0032) wumpz *2018-05-02 21:13:11*

**fixes #611**


[35cf1](https://github.com/JSQLParser/JSqlParser/commit/35cf1d8b1b80d29) wumpz *2018-05-02 21:08:22*

**fixes #610**


[5a60e](https://github.com/JSQLParser/JSqlParser/commit/5a60e1b57b7de4b) wumpz *2018-05-02 20:48:08*

**fixes #610**


[c5c26](https://github.com/JSQLParser/JSqlParser/commit/c5c26ca4d18092b) wumpz *2018-05-02 20:45:56*

**Update README.md**


[49b28](https://github.com/JSQLParser/JSqlParser/commit/49b2800bf7533e9) Tobias *2018-05-01 21:58:41*

**Adding support for MySQL's SQL_NO_CACHE flag**


[403e7](https://github.com/JSQLParser/JSqlParser/commit/403e722e6ee12b4) Tomer S *2018-03-25 09:03:23*


## jsqlparser-1.2 (2018-05-01)

### Other changes

**fixes #605**


[6e309](https://github.com/JSQLParser/JSqlParser/commit/6e3099c50352966) wumpz *2018-04-22 21:49:56*

**fixes #604**


[c8913](https://github.com/JSQLParser/JSqlParser/commit/c89139cfecb8e8d) wumpz *2018-04-20 22:38:48*

**moved some jjt options from pom.xml to JSqlParserCC.jjt**


[e9a6c](https://github.com/JSQLParser/JSqlParser/commit/e9a6cd3b79ed501) wumpz *2018-04-19 13:29:22*

**fixes #603**


[32930](https://github.com/JSQLParser/JSqlParser/commit/3293023e059de4d) wumpz *2018-04-19 08:35:31*

**fixes #600**


[e80ea](https://github.com/JSQLParser/JSqlParser/commit/e80ea6803591783) wumpz *2018-04-13 07:55:00*

**change standard to jdk 8 with java 1.7 file compliance**


[c28a5](https://github.com/JSQLParser/JSqlParser/commit/c28a549ef9895b4) wumpz *2018-04-13 07:46:19*

**fixes #593**


[65276](https://github.com/JSQLParser/JSqlParser/commit/652766264a2bb61) wumpz *2018-04-08 22:37:28*

**fixes #597**


[ac34e](https://github.com/JSQLParser/JSqlParser/commit/ac34efb0279a6af) wumpz *2018-04-08 21:17:12*

****


[4f63b](https://github.com/JSQLParser/JSqlParser/commit/4f63b1c085634f2) wumpz *2018-04-02 23:42:44*

**fixes #592**


[1b5d2](https://github.com/JSQLParser/JSqlParser/commit/1b5d24dcf7d6fc8) wumpz *2018-04-02 22:37:47*

**- allow parenthesis around from item**

* - allow whitespace between bars from concat 

[2cea3](https://github.com/JSQLParser/JSqlParser/commit/2cea3ee94d83447) wumpz *2018-03-29 22:25:48*

**- allow parenthesis around from item**

* - allow whitespace between bars from concat 

[dabb0](https://github.com/JSQLParser/JSqlParser/commit/dabb03f0094e4c7) wumpz *2018-03-29 22:24:33*

****


[7d366](https://github.com/JSQLParser/JSqlParser/commit/7d36615ee5b5837) wumpz *2018-03-23 07:26:30*

**fixes #588**


[a04cc](https://github.com/JSQLParser/JSqlParser/commit/a04ccb1c05204e5) wumpz *2018-03-17 00:40:36*

**fixes #588**


[801cd](https://github.com/JSQLParser/JSqlParser/commit/801cd84c8dbb33f) wumpz *2018-03-17 00:40:06*

**JSQLPARSER-584: adds support for MySQL (a,b,...)OP(c,d,...) expression (#585)**

* JSQLPARSER-584: adds support for MySQL (a,b,...)OP(c,d,...) expression 
* JSQLPARSER-584: adds some tests and rename MySQLValueListExpression to ValueListExpression 

[2c272](https://github.com/JSQLParser/JSqlParser/commit/2c272f440995b2c) Adrien Lesur *2018-03-05 23:08:55*

**checked #266**


[72059](https://github.com/JSQLParser/JSqlParser/commit/7205906f58be21c) wumpz *2018-02-14 08:43:51*

**fixes #583**


[76ed9](https://github.com/JSQLParser/JSqlParser/commit/76ed995ebef6bd1) wumpz *2018-02-14 08:37:38*

**fixes #583**


[3768b](https://github.com/JSQLParser/JSqlParser/commit/3768b8d10789c70) wumpz *2018-02-14 08:36:26*

**tested issue 582**


[84459](https://github.com/JSQLParser/JSqlParser/commit/84459536a13fa4b) wumpz *2018-02-09 07:18:06*

**Create ISSUE_TEMPLATE.md**


[d5ec2](https://github.com/JSQLParser/JSqlParser/commit/d5ec2fe18e8f63b) Tobias *2018-02-08 07:12:19*

**removed unneeded dependencies**


[445b1](https://github.com/JSQLParser/JSqlParser/commit/445b1a517e651de) wumpz *2018-02-07 10:46:12*

****


[feaea](https://github.com/JSQLParser/JSqlParser/commit/feaeaeb2c63b88c) wumpz *2018-02-02 12:46:17*

****


[18d01](https://github.com/JSQLParser/JSqlParser/commit/18d01811c592581) wumpz *2018-02-02 12:45:51*

**Fix issue #563: subjoin allows only one inner join, this should be a … (#564)**

* Fix issue #563: subjoin allows only one inner join, this should be a list 
* Fix failing Oracle tests because of confusion between subjoin and subselect. 

[8456a](https://github.com/JSQLParser/JSqlParser/commit/8456acf7f228a46) Frits Jalvingh *2018-02-02 10:40:01*

**fixes #320 (#576)**

* fixes #320 

[e6451](https://github.com/JSQLParser/JSqlParser/commit/e645193140f4271) Taner Mansur *2018-02-01 14:55:37*

****


[21e8b](https://github.com/JSQLParser/JSqlParser/commit/21e8bc4c549ea75) wumpz *2018-02-01 14:54:12*

**fixes #566,#438,#267**


[bf951](https://github.com/JSQLParser/JSqlParser/commit/bf9515418475372) wumpz *2018-01-30 14:10:17*

**fixes #566,#438,#267**


[ccbe9](https://github.com/JSQLParser/JSqlParser/commit/ccbe9ad0be27d78) wumpz *2018-01-30 14:09:12*

**tests #572**


[5d609](https://github.com/JSQLParser/JSqlParser/commit/5d609ef208c6803) wumpz *2018-01-29 15:43:04*

**corrected generics type**


[ed1a2](https://github.com/JSQLParser/JSqlParser/commit/ed1a297094cd85c) wumpz *2018-01-22 07:34:55*

**fixes #567**


[0d4ae](https://github.com/JSQLParser/JSqlParser/commit/0d4aed3aadce638) wumpz *2018-01-12 07:36:54*

**removed "removed" project bewa**


[dec98](https://github.com/JSQLParser/JSqlParser/commit/dec98caa839b0d6) wumpz *2018-01-08 07:44:24*

**fixes #338**


[0e571](https://github.com/JSQLParser/JSqlParser/commit/0e571d14aed151c) wumpz *2018-01-05 23:29:00*

**fixes #545**


[ab7e2](https://github.com/JSQLParser/JSqlParser/commit/ab7e29bba316f0d) wumpz *2018-01-05 23:16:29*

**fixes #462**


[83844](https://github.com/JSQLParser/JSqlParser/commit/838449cb5163f19) wumpz *2017-12-28 00:50:29*

**fixes #462**


[bc005](https://github.com/JSQLParser/JSqlParser/commit/bc005f89fd7d091) wumpz *2017-12-28 00:47:37*

**Update README.md**


[5871c](https://github.com/JSQLParser/JSqlParser/commit/5871c573f69696d) Tobias *2017-12-27 20:04:29*

**Update README.md**


[dd6bb](https://github.com/JSQLParser/JSqlParser/commit/dd6bbef26420dd0) Tobias *2017-12-07 10:25:11*

**fixes #554**


[4ce98](https://github.com/JSQLParser/JSqlParser/commit/4ce98e492ce9d17) wumpz *2017-12-07 08:55:52*

****


[ee723](https://github.com/JSQLParser/JSqlParser/commit/ee723d50b08a54d) wumpz *2017-12-04 08:36:46*

**fixes #543**


[bdc20](https://github.com/JSQLParser/JSqlParser/commit/bdc20ea7688c8c4) wumpz *2017-12-04 08:35:07*

**fixes #551**


[a25fb](https://github.com/JSQLParser/JSqlParser/commit/a25fb7cb3ba02c1) wumpz *2017-12-04 08:14:06*

**made some modifications to rlike fix**


[f0ffe](https://github.com/JSQLParser/JSqlParser/commit/f0ffe4c53edbcaa) wumpz *2017-11-16 22:29:53*

**Added support for RLIKE expressions (#544)**

* RLIKE is a synonym of REGEXP, therefore should be treated the same. 

[8a950](https://github.com/JSQLParser/JSqlParser/commit/8a950b3e09dce06) sh-tomer *2017-11-16 22:10:51*

**jdk 1.7**


[8d4f2](https://github.com/JSQLParser/JSqlParser/commit/8d4f25c49c3975d) wumpz *2017-11-07 09:43:35*

**modern template included**


[9ea8e](https://github.com/JSQLParser/JSqlParser/commit/9ea8e4c3f4e3ab7) wumpz *2017-11-07 07:31:58*

**fixes #540,#526**


[8bc23](https://github.com/JSQLParser/JSqlParser/commit/8bc236e25ddbe8d) wumpz *2017-11-03 07:21:40*

**fixes #540,#526**


[ab868](https://github.com/JSQLParser/JSqlParser/commit/ab868a627a6b72f) wumpz *2017-11-03 07:18:39*

**corrected a lookahead issue**


[a2f81](https://github.com/JSQLParser/JSqlParser/commit/a2f81f3a299145a) wumpz *2017-10-31 22:37:41*

**Add ability to support "NOT LIKE ..." expressions (#539)**

* The parser is able to parse expressions such as &quot;a NOT LIKE &#x27;%pattern%&#x27;&quot;, but is not able to parse expressions where the not is before the entire expression. For example: &quot;NOT a LIKE &#x27;%pattern%&#x27;. 
* When parsing the latter, the error is: 
* Caused by: net.sf.jsqlparser.parser.ParseException: Encountered &quot; &quot;LIKE&quot; &quot;LIKE &quot;&quot; at line 1, column 32. 
* Was expecting one of: ... 
* The reason this is important is both because these syntaxes are both valid, and also because the deparser uses the second method. 
* Therefore, if you parse a query with the first type of expression, then deparse it and parse again, you&#x27;ll get the same error. 

[daea3](https://github.com/JSQLParser/JSqlParser/commit/daea33e73aa05c6) sh-tomer *2017-10-31 20:27:50*

****


[452ba](https://github.com/JSQLParser/JSqlParser/commit/452baffb2af8c21) wumpz *2017-10-29 23:20:57*

****


[14523](https://github.com/JSQLParser/JSqlParser/commit/1452360f9cca7aa) wumpz *2017-10-29 23:08:39*

****


[7c90a](https://github.com/JSQLParser/JSqlParser/commit/7c90a24bdf72994) wumpz *2017-10-29 22:59:21*

**Linking structures to their AST nodes to have access to their positions (#534)**

* Linking several structures to their AST nodes to have access to their positions 
* This far there were only 3 types of structures linked to their AST nodes. Now adding some more expressions and literals to their AST node to have access to their token&#x27;s position in the query. 
* Added missing parts in JSQqlParserCC.jjt for AST linking to work 
* Added missing parts in JSQqlParserCC.jjt to make sure all relevant code is created to generate and link AST nodes to the relevant structures. 

[514f2](https://github.com/JSQLParser/JSqlParser/commit/514f2588af97345) sh-tomer *2017-10-29 12:39:56*

**add debug note (#531)**

* added a link to the visualize parsing section to have a visible debug mode (so users that create an issue can try to get us better output) 

[b1abc](https://github.com/JSQLParser/JSqlParser/commit/b1abc6ff39e9c2a) Jan Monterrubio *2017-10-25 06:21:26*

**fixes #525 (#530)**

* fixes #525 
* Simply unit test. 

[1a1a1](https://github.com/JSQLParser/JSqlParser/commit/1a1a1aa53866787) Linyu Chen *2017-10-24 05:30:39*

**simplified tests for SQL_CALC_FOUND_ROWS**


[e23d4](https://github.com/JSQLParser/JSqlParser/commit/e23d4bc09e5f6e4) wumpz *2017-10-20 07:32:58*

**Implements #509 (#504)**

* Supporting MySql hit SQL_CALC_FOUND_ROWS for selecting row count. 
* Supporting MySql hit SQL_CALC_FOUND_ROWS for selecting row count. - refactoring 
* Supporting MySql hit SQL_CALC_FOUND_ROWS for selecting row count. - missing copyright.ˆ 
* Supporting MySql hit SQL_CALC_FOUND_ROWS for selecting row count. - Modify field type to boolean for prevent memory consumption by creating object and try assertSqlCanBeParsedAndDeparsed on unit test. 

[3e163](https://github.com/JSQLParser/JSqlParser/commit/3e16345815e45b5) Yoon Kyong Sik *2017-10-20 07:27:48*

**fixes #522**


[45ac8](https://github.com/JSQLParser/JSqlParser/commit/45ac8c8a2bcff54) wumpz *2017-10-12 21:27:57*

**fixes #522**


[2c69c](https://github.com/JSQLParser/JSqlParser/commit/2c69cc65f8bfd32) wumpz *2017-10-12 21:22:26*

**fixes #510**


[f64ad](https://github.com/JSQLParser/JSqlParser/commit/f64ad89eec4ea53) wumpz *2017-10-09 23:51:46*

****


[1a771](https://github.com/JSQLParser/JSqlParser/commit/1a77106df128893) wumpz *2017-10-06 08:58:01*

**fixes #508 including precedence**


[8037a](https://github.com/JSQLParser/JSqlParser/commit/8037af621f32f0a) wumpz *2017-10-06 08:36:57*

**fixes #519**

* fixes #520 

[27217](https://github.com/JSQLParser/JSqlParser/commit/272177a37b9ee81) wumpz *2017-10-06 08:24:04*

**transformed primary expression and sign parsing**


[51fcd](https://github.com/JSQLParser/JSqlParser/commit/51fcdea9f92c1ce) wumpz *2017-10-06 07:11:58*

**corrected token definition order**


[64ce9](https://github.com/JSQLParser/JSqlParser/commit/64ce9ff2986d492) wumpz *2017-10-06 07:01:35*

**waiting for https://github.com/javacc/javacc/issues/28**


[704e6](https://github.com/JSQLParser/JSqlParser/commit/704e6c84fb66150) wumpz *2017-10-02 09:14:01*

**Fix test case**


[b2bb2](https://github.com/JSQLParser/JSqlParser/commit/b2bb2431c4dfae0) Nathaniel Camomot *2017-09-27 08:10:08*

**Fix for some cases where TablNamesFinder can't find tables**


[395c3](https://github.com/JSQLParser/JSqlParser/commit/395c3b0049cd09f) Nathaniel Camomot *2017-09-27 06:21:21*

**removed oraclejdk7 travis build due to https://github.com/travis-ci/travis-ci/issues/7964**


[812f9](https://github.com/JSQLParser/JSqlParser/commit/812f98fff951c4e) wumpz *2017-09-24 09:55:31*

****


[96b5d](https://github.com/JSQLParser/JSqlParser/commit/96b5d9f26ec7310) wumpz *2017-09-24 09:48:35*

****


[b847e](https://github.com/JSQLParser/JSqlParser/commit/b847e85c0dba19f) wumpz *2017-09-23 22:40:30*

**Create LICENSE_LGPLV21**


[d2c87](https://github.com/JSQLParser/JSqlParser/commit/d2c87dac67436af) Tobias *2017-09-23 22:29:45*

**Create LICENSE_APACHEV2**


[f9c1a](https://github.com/JSQLParser/JSqlParser/commit/f9c1a9f7fb91703) Tobias *2017-09-23 22:29:09*

**fixes #515**


[c4b36](https://github.com/JSQLParser/JSqlParser/commit/c4b360e14a06201) wumpz *2017-09-23 22:20:51*

**fixes #515**


[1afe7](https://github.com/JSQLParser/JSqlParser/commit/1afe7e6a9931f7e) wumpz *2017-09-23 22:18:35*

**fixes #515**


[8388f](https://github.com/JSQLParser/JSqlParser/commit/8388f1e48355b4f) wumpz *2017-09-23 22:17:04*

**Update README.md**


[3f734](https://github.com/JSQLParser/JSqlParser/commit/3f734f130da0a66) Tobias *2017-09-23 21:38:51*

**fixes #514**


[8a459](https://github.com/JSQLParser/JSqlParser/commit/8a459ce9894ec36) wumpz *2017-09-23 21:32:10*

**fixes #514**


[3e846](https://github.com/JSQLParser/JSqlParser/commit/3e84605a7a87948) wumpz *2017-09-23 21:29:52*

**merge of within_group and analytic**


[26fab](https://github.com/JSQLParser/JSqlParser/commit/26faba8040636e8) wumpz *2017-09-23 21:24:18*

****


[c5a47](https://github.com/JSQLParser/JSqlParser/commit/c5a471f926c1d85) wumpz *2017-09-22 12:29:44*

**#fixes 480**


[10352](https://github.com/JSQLParser/JSqlParser/commit/10352328d3d52f3) wumpz *2017-09-12 21:59:08*

**#fixes 480**


[e60fa](https://github.com/JSQLParser/JSqlParser/commit/e60fa8cd305d154) wumpz *2017-09-12 21:58:39*

**fixes #512**


[07a14](https://github.com/JSQLParser/JSqlParser/commit/07a14dc892d6003) wumpz *2017-09-07 22:20:01*

**fixes #505**


[ef55f](https://github.com/JSQLParser/JSqlParser/commit/ef55ffba8b596aa) wumpz *2017-08-28 08:13:14*

**fixes #502**


[6a440](https://github.com/JSQLParser/JSqlParser/commit/6a440ba583955b1) wumpz *2017-08-26 14:18:07*

**fixes #502**


[41ea8](https://github.com/JSQLParser/JSqlParser/commit/41ea83d2757adee) wumpz *2017-08-26 14:14:13*

**fixes #484**


[1a6f9](https://github.com/JSQLParser/JSqlParser/commit/1a6f9143ef491dd) wumpz *2017-08-23 20:41:06*

**fixes #484**


[8f9b6](https://github.com/JSQLParser/JSqlParser/commit/8f9b62705ae094e) wumpz *2017-08-23 20:40:26*

**replace support multiple values**


[93598](https://github.com/JSQLParser/JSqlParser/commit/93598bbe86aaa92) wanghai *2017-08-21 09:15:27*

**fixed #491**


[147ec](https://github.com/JSQLParser/JSqlParser/commit/147ec4887639700) wumpz *2017-08-16 15:47:01*

**fixed #491**


[24f23](https://github.com/JSQLParser/JSqlParser/commit/24f232c08edb86c) wumpz *2017-08-16 15:45:55*

**checked issue #482**


[71692](https://github.com/JSQLParser/JSqlParser/commit/71692c5c6f85f0f) wumpz *2017-08-07 13:33:12*

**fixes #485**


[64bc5](https://github.com/JSQLParser/JSqlParser/commit/64bc5e05d8086b4) wumpz *2017-08-03 05:57:10*

**fix issue #424 (INSERT with SET) (#481)**

* update insert with set language 
* update insert with set 
* update insert with set 
* update insert test 
* add removed lines 

[ca653](https://github.com/JSQLParser/JSqlParser/commit/ca6538a04dd7969) messfish *2017-07-28 06:23:33*

**fixes #456**


[9c2cc](https://github.com/JSQLParser/JSqlParser/commit/9c2cc2c823b3d1e) wumpz *2017-07-27 11:14:26*

**introduced partial / nonpartial parse for CCJSqlParserUtil methods**


[6b452](https://github.com/JSQLParser/JSqlParser/commit/6b452183f084652) wumpz *2017-07-27 05:47:13*

**fixes #473**


[0aa22](https://github.com/JSQLParser/JSqlParser/commit/0aa229d3df996f6) wumpz *2017-07-18 06:24:20*

**Update README.md**


[58c42](https://github.com/JSQLParser/JSqlParser/commit/58c42bcfbf717ee) Tobias *2017-06-29 21:37:30*

**Update README.md**


[d235c](https://github.com/JSQLParser/JSqlParser/commit/d235ca3971a9a6e) Tobias *2017-06-29 21:34:47*


## jsqlparser-1.1 (2017-06-29)

### Other changes

**fixes #468**


[6cb45](https://github.com/JSQLParser/JSqlParser/commit/6cb459d747901ea) wumpz *2017-06-28 08:50:50*

****


[fb64e](https://github.com/JSQLParser/JSqlParser/commit/fb64e3295e91bca) wumpz *2017-06-11 21:50:53*

**Add Upsert Grammer (#460)**

* Add files via upload 
* Add files via upload 
* Add files via upload 
* Add files via upload 
* Add files via upload 
* Add test for de parser 
* Add files via upload 
* Add files via upload 
* Add files via upload 
* Add files via upload 
* Add files via upload 
* Add files via upload 
* Add files via upload 
* Add files via upload 
* Add files via upload 

[aaeb8](https://github.com/JSQLParser/JSqlParser/commit/aaeb8dfeb0c2a4e) messfish *2017-06-11 18:48:53*

**introduced linking between Function and ASTNode**


[3e75c](https://github.com/JSQLParser/JSqlParser/commit/3e75c68c4bf769e) wumpz *2017-06-09 22:34:23*

**introduced linking between Function and ASTNode**


[e96f4](https://github.com/JSQLParser/JSqlParser/commit/e96f48534ca3103) wumpz *2017-06-09 22:32:16*

**fixes #457**


[2ae5e](https://github.com/JSQLParser/JSqlParser/commit/2ae5e76d69b1cbf) wumpz *2017-05-26 21:34:35*

****


[e4ef6](https://github.com/JSQLParser/JSqlParser/commit/e4ef6e231282b62) wumpz *2017-05-23 07:32:52*

**Fix issue #442 (#451)**

* Fix issue #442 for delete statements 
* Fix issue #442 for insert statements 
* Mock only when necessary 
* E.g., interfaces, behavior needs to be verified, etc. 
* Prefer the first style of testing 
* As discussed in issue #442 
* Fix issue #442 for replace statements 
* Improve readability of issue #442 tests 
* Inject SelectDeParser as well 
* As discussed in issue #442. 
* Fix issue #442 for select statements 
* Fix issue #442 for update statements 
* Fix issue #442 for execute statements 
* Fix issue #442 for set statements 
* Fix PR code review issue 
* https://www.codacy.com/app/wumpz/JSqlParser/file/6682733346/issues/source?bid&#x3D;4162857&amp;fileBranchId&#x3D;4580866#l48 
* Skip PMD check for asserts in tests using Mockito 
* As agreed upon in the discussion in PR #451. 
* Use correct PMD check name 

[9d680](https://github.com/JSQLParser/JSqlParser/commit/9d680a6ec4b9f07) chrycheng *2017-05-22 07:13:41*

**fixes #430**


[585cb](https://github.com/JSQLParser/JSqlParser/commit/585cbbd1377632b) wumpz *2017-05-16 22:04:19*

**Fix issue #446 (#447)**

* Test current behavior of ExecuteDeParser 
* Fix issue #446 

[275fb](https://github.com/JSQLParser/JSqlParser/commit/275fbbe87fa0ada) chrycheng *2017-05-16 20:46:29*

**fixes #449**


[fe6da](https://github.com/JSQLParser/JSqlParser/commit/fe6dafea19fa236) wumpz *2017-05-16 14:12:50*

**introduced test for 437**


[3e740](https://github.com/JSQLParser/JSqlParser/commit/3e740100498aa22) wumpz *2017-05-12 19:43:14*

**test issue 445**


[fce59](https://github.com/JSQLParser/JSqlParser/commit/fce593dcf85c43e) wumpz *2017-05-11 08:37:49*

****


[1fbf6](https://github.com/JSQLParser/JSqlParser/commit/1fbf6c5bde81b41) wumpz *2017-05-08 12:32:25*

**updated readme**


[1dabc](https://github.com/JSQLParser/JSqlParser/commit/1dabcbc56f9b158) wumpz *2017-05-07 20:37:31*

**some minor changes**


[050b8](https://github.com/JSQLParser/JSqlParser/commit/050b8ba6434630d) wumpz *2017-05-07 20:35:15*

**conversion to CNF (#434)**

* Add files via upload 
* Create foo 
* All the files needed for the CNF conversion 
* Delete foo 
* Create foo 
* Test cases for the CNF conversion 
* Delete foo 
* Add files via upload 
* Add files via upload 
* change some public methods to private 
* Delete CNFConverter.java 
* Delete CloneHelper.java 
* Delete MultiAndExpression.java 
* Delete MultiOrExpression.java 
* Delete MultipleExpression.java 
* Create foo 
* Add files via upload 
* Delete CNFTest.java 
* Delete StepLastHelper.java 
* Create foo 
* Add files via upload 
* Delete foo 
* Delete foo 
* Add files via upload 
* Add files via upload 
* Delete CNFConverter.java 
* Delete CloneHelper.java 
* Delete MultiAndExpression.java 
* Delete MultiOrExpression.java 
* Delete MultipleExpression.java 
* Create foo 
* Add files via upload 
* Delete foo 
* Delete CNFTest.java 
* Create foo 
* Add files via upload 
* Delete foo 
* Add files via upload 
* Add files via upload 

[afe10](https://github.com/JSQLParser/JSqlParser/commit/afe1011bdd64696) messfish *2017-05-07 20:21:00*

****


[56497](https://github.com/JSQLParser/JSqlParser/commit/56497af552c53a4) wumpz *2017-05-02 06:06:33*

**corrected bug within RelObjectNameExt processing**


[7b2b0](https://github.com/JSQLParser/JSqlParser/commit/7b2b0f649b1702a) wumpz *2017-05-02 06:00:44*

**Update README.md**


[2c0f2](https://github.com/JSQLParser/JSqlParser/commit/2c0f2ff0f6c7814) Tobias *2017-04-28 07:37:09*

**Update README.md**


[80c98](https://github.com/JSQLParser/JSqlParser/commit/80c98055bc969a0) Tobias *2017-04-28 07:35:42*

****


[59310](https://github.com/JSQLParser/JSqlParser/commit/5931045d807559e) wumpz *2017-04-24 13:25:21*

**Introduce support for mysql index hints (fixing issue #374) (#429)**

* Introduce support for mysql index hints (fixing issue #374) 
* Fix checkstyle errors 
* -Converted indent tabs to spaces 
* -Added missing {} on single-line if statement 

[6db15](https://github.com/JSQLParser/JSqlParser/commit/6db15d2c6218d06) Joey Mart *2017-04-18 06:22:25*

**#425 ADD CONSTRAINT also support state such as DEFERRABLE, VALIDATE... (#426)**


[f1113](https://github.com/JSQLParser/JSqlParser/commit/f11133c0bdd7a6b) Christophe Moine *2017-04-17 23:40:45*

**Addressing #427 (#428)**

* updating readme with Maven requirements 
* removing ticks 

[0fddc](https://github.com/JSQLParser/JSqlParser/commit/0fddc73e2eee1b2) AnEmortalKid *2017-04-17 23:39:05*

**going back to checkstyle 6.x due to java7 incompatibilities**


[564ac](https://github.com/JSQLParser/JSqlParser/commit/564acc288fa0294) wumpz *2017-04-17 23:27:10*

**Update README.md**


[30b09](https://github.com/JSQLParser/JSqlParser/commit/30b0924742068f0) Tobias *2017-04-17 23:09:06*

**Update README.md**


[2b5c8](https://github.com/JSQLParser/JSqlParser/commit/2b5c84552963ec3) Tobias *2017-04-17 23:05:07*

**checkstyle source check included. configuration done**


[12377](https://github.com/JSQLParser/JSqlParser/commit/12377c784a3fa53) wumpz *2017-04-17 23:01:42*

**checkstyle source check included. configuration done**


[1bdf5](https://github.com/JSQLParser/JSqlParser/commit/1bdf5b9515b5af3) wumpz *2017-04-17 22:57:12*

**improved StatementVistorAdaptor to process all statements.**


[27119](https://github.com/JSQLParser/JSqlParser/commit/2711900eb3ebf83) wumpz *2017-04-11 07:47:22*

**removed linefeed check, due to multiple git checkout configurations regarding linefeeds**


[aab56](https://github.com/JSQLParser/JSqlParser/commit/aab569bf4c6376f) wumpz *2017-03-31 08:04:24*

****


[2f5a1](https://github.com/JSQLParser/JSqlParser/commit/2f5a1eacf3e9f5a) wumpz *2017-03-29 06:07:20*

**corrected source files regarding checkstyle errors**


[0d6fa](https://github.com/JSQLParser/JSqlParser/commit/0d6faeb8b411e53) wumpz *2017-03-28 09:46:55*

**introduced some more checkstyle rules**


[c7a86](https://github.com/JSQLParser/JSqlParser/commit/c7a8601b84695e4) wumpz *2017-03-28 09:30:00*

**removed problematic profile "check.sources" activation, excluded generated-sources, included test-sources**


[e2cc2](https://github.com/JSQLParser/JSqlParser/commit/e2cc2210c364f6c) wumpz *2017-03-27 11:48:50*

**removed problematic profile "check.sources" activation, excluded generated-sources, included test-sources**


[e941f](https://github.com/JSQLParser/JSqlParser/commit/e941fd96a2caade) wumpz *2017-03-27 11:45:39*

**removed problematic profile "check.sources" activation, excluded generated-sources, included test-sources**


[dd23f](https://github.com/JSQLParser/JSqlParser/commit/dd23f27516db63b) wumpz *2017-03-27 11:14:44*

**removed auto activation due to travis problems, included test sources**


[759e7](https://github.com/JSQLParser/JSqlParser/commit/759e751b85c5a16) wumpz *2017-03-27 10:53:46*

**checkstyle**


[62505](https://github.com/JSQLParser/JSqlParser/commit/625054295a7700b) wumpz *2017-03-27 09:16:11*

**Update README.md**


[da9ac](https://github.com/JSQLParser/JSqlParser/commit/da9acf59e308d2b) Tobias *2017-03-25 22:21:59*


## jsqlparser-1.0 (2017-03-25)

### Other changes

****


[32e8f](https://github.com/JSQLParser/JSqlParser/commit/32e8fa02ecfadc4) wumpz *2017-03-25 22:00:13*

****


[a31b2](https://github.com/JSQLParser/JSqlParser/commit/a31b219baadb001) wumpz *2017-03-25 21:52:19*

****


[f731f](https://github.com/JSQLParser/JSqlParser/commit/f731fa4cf726036) wumpz *2017-03-25 21:47:42*

**reformating hole sourcecode**


[4146f](https://github.com/JSQLParser/JSqlParser/commit/4146f869cb01151) wumpz *2017-03-22 07:36:17*

**Fix #407 by enhancing grammar (#410)**

* Fix #407 by enhancing grammar 
* Change LF and tabs 

[5d901](https://github.com/JSQLParser/JSqlParser/commit/5d9018657df6b22) Christophe Moine *2017-03-22 07:36:14*

**removed dependencies**


[dd1b3](https://github.com/JSQLParser/JSqlParser/commit/dd1b334e3bc4675) wumpz *2017-03-20 10:22:55*

**fixes #406**


[06619](https://github.com/JSQLParser/JSqlParser/commit/066199cfeb97ea4) wumpz *2017-03-17 11:55:11*

**update to JavaCC 7.0.2**


[c12f7](https://github.com/JSQLParser/JSqlParser/commit/c12f7ac457d92c3) wumpz *2017-03-15 10:28:17*

**Update README.md**


[6312f](https://github.com/JSQLParser/JSqlParser/commit/6312f3aaf9aa46e) Tobias *2017-03-15 08:55:03*

**update readme**


[03746](https://github.com/JSQLParser/JSqlParser/commit/03746a8ddf0c14e) wumpz *2017-03-15 08:45:24*

**update readme**


[5bbfe](https://github.com/JSQLParser/JSqlParser/commit/5bbfeb76bf7deab) wumpz *2017-03-15 08:44:22*

****


[6e72b](https://github.com/JSQLParser/JSqlParser/commit/6e72b99ee4b0ba0) wumpz *2017-03-14 11:41:26*

**corrected merge conflict**


[1211d](https://github.com/JSQLParser/JSqlParser/commit/1211dcf1209f3a4) wumpz *2017-03-14 11:39:30*

**corrected case when expressions**


[12cc4](https://github.com/JSQLParser/JSqlParser/commit/12cc4059ddef291) wumpz *2017-03-14 11:12:53*

**rewrote some lookaheads**


[2a440](https://github.com/JSQLParser/JSqlParser/commit/2a4405a6a0821cc) wumpz *2017-03-14 10:33:21*

**replace some junit pre 4.x artifacts**


[7dd85](https://github.com/JSQLParser/JSqlParser/commit/7dd857e5000f09a) wumpz *2017-03-14 09:55:42*

**first rewrite of SelectBody**


[49127](https://github.com/JSQLParser/JSqlParser/commit/49127be715d029a) wumpz *2017-03-14 09:43:25*

****


[5dcef](https://github.com/JSQLParser/JSqlParser/commit/5dcefcefd07755a) wumpz *2017-03-10 22:13:02*

**Support FOR UPDATE WAIT (#405)**

* Adding FOR UPDATE WAIT support 
* removing final peppered everywhere 
* updating formatting, fixing codacy test names 
* updating asserts to use static import 
* reverting changes 
* reverting line feeds 
* adding tests and deparser code back without formatting 

[45b39](https://github.com/JSQLParser/JSqlParser/commit/45b392f4b93714c) AnEmortalKid *2017-03-10 22:06:53*

**add support for LIMIT with only one row count JDBC parameter (#404)**

* small but powerfull change 👍 

[42318](https://github.com/JSQLParser/JSqlParser/commit/42318531bb72279) zhushaoping *2017-03-03 07:06:08*

**merged**


[c3eed](https://github.com/JSQLParser/JSqlParser/commit/c3eed13096c0019) wumpz *2017-03-03 07:03:08*

**fixes #401**


[1315d](https://github.com/JSQLParser/JSqlParser/commit/1315d035dbc5008) wumpz *2017-03-01 08:20:55*

**fixes #402**


[6fb15](https://github.com/JSQLParser/JSqlParser/commit/6fb15a104debe95) wumpz *2017-03-01 07:55:25*

**release 0.9.7**


[46720](https://github.com/JSQLParser/JSqlParser/commit/46720ef029cf8aa) wumpz *2017-02-26 23:13:50*


## jsqlparser-0.9.7 (2017-02-26)

### Other changes

**updated readme**


[e2dc3](https://github.com/JSQLParser/JSqlParser/commit/e2dc36897178155) wumpz *2017-02-21 08:49:56*

**commented an issue test**


[4fe4c](https://github.com/JSQLParser/JSqlParser/commit/4fe4ce206296ea1) wumpz *2017-02-21 08:46:38*

**set statemet with optional equals**


[ec6ce](https://github.com/JSQLParser/JSqlParser/commit/ec6cef2e803d87a) wumpz *2017-02-11 23:57:32*

**fixes #393**


[320f6](https://github.com/JSQLParser/JSqlParser/commit/320f64a9a4aa687) wumpz *2017-02-10 09:34:47*

****


[42f32](https://github.com/JSQLParser/JSqlParser/commit/42f3242cc13fac6) wumpz *2017-02-08 08:17:40*

**removed unused imports**


[c66cc](https://github.com/JSQLParser/JSqlParser/commit/c66cc6973753c3a) wumpz *2017-02-08 08:15:51*

**minor code improvements**


[e3fd2](https://github.com/JSQLParser/JSqlParser/commit/e3fd2c6df3444ae) wumpz *2017-02-08 08:13:58*

**Update README.md**


[3d05e](https://github.com/JSQLParser/JSqlParser/commit/3d05e16c6c09a05) Tobias *2017-02-07 18:09:26*

**fixes #390**


[546b7](https://github.com/JSQLParser/JSqlParser/commit/546b71d84eb8b45) wumpz *2017-02-01 15:27:48*

****


[d4396](https://github.com/JSQLParser/JSqlParser/commit/d439643bbf1cd02) wumpz *2017-01-27 20:53:20*

**fixes #389**


[71c32](https://github.com/JSQLParser/JSqlParser/commit/71c32d905fbd4cb) wumpz *2017-01-27 20:39:25*

**included LOOKAHEAD**


[6776e](https://github.com/JSQLParser/JSqlParser/commit/6776eb5fd0224e4) wumpz *2017-01-20 23:12:17*

**updated readme**


[b5f6e](https://github.com/JSQLParser/JSqlParser/commit/b5f6e9efac23dca) wumpz *2017-01-20 22:10:59*

**updated readme**


[1abd2](https://github.com/JSQLParser/JSqlParser/commit/1abd224a3e2ff0f) wumpz *2017-01-20 22:00:07*

**Increase test coverage on AlterExpression.java**

* getOperation 
* getFkColumns 
* getFkSourceTable 
* getFkSourceColumns 
* getConstraintName 
* tried getPkColumns but it does not behave as I expected. 
* placed TODO in AlterTest.testAlterTablePK for this 
* getIndex().getColumnNames 

[5a799](https://github.com/JSQLParser/JSqlParser/commit/5a79964714b13f7) jthomas *2017-01-19 19:43:24*

**Enhance AlterExpression grammar:**

* 1. optional &quot;COLUMN&quot; keyword in ADD alter operation 
* 2. new alter operation: MODIFY 
* 3. add column specs to alter table column definitions 

[24177](https://github.com/JSQLParser/JSqlParser/commit/241779b26973b47) jthomas *2017-01-19 17:32:21*

**support to add jdbc name parameter after LIMIT and TOP keywords**


[e25e2](https://github.com/JSQLParser/JSqlParser/commit/e25e247dd5e9131) zhushaoping *2017-01-13 02:55:52*

**fixes #288**


[091c5](https://github.com/JSQLParser/JSqlParser/commit/091c5dd5ab5ce93) zhushaoping *2017-01-12 07:09:57*

**tests for issue 379 included**


[f7c27](https://github.com/JSQLParser/JSqlParser/commit/f7c27ad6492a636) wumpz *2017-01-04 07:22:19*

**tests for issue 379 included**


[14a9b](https://github.com/JSQLParser/JSqlParser/commit/14a9b3e372b664a) wumpz *2017-01-04 06:49:43*

**updated readme**


[1899c](https://github.com/JSQLParser/JSqlParser/commit/1899cbffb915992) wumpz *2017-01-02 13:32:56*

**fixes #375**

* fixes #371 

[957f3](https://github.com/JSQLParser/JSqlParser/commit/957f39c496a7fac) wumpz *2017-01-02 13:30:09*

**fixed NPE in ExpressionVisitorAdapter when SubSelect doesn't has withItemsList**


[28979](https://github.com/JSQLParser/JSqlParser/commit/2897935037560db) Donghang Lin *2016-12-21 07:14:48*

****


[5bc1f](https://github.com/JSQLParser/JSqlParser/commit/5bc1fc3669d52f0) wumpz *2016-12-06 22:08:00*

**fixed #363**


[66e44](https://github.com/JSQLParser/JSqlParser/commit/66e44c956be7481) wumpz *2016-12-01 06:52:45*

**integrated some tests**


[5937b](https://github.com/JSQLParser/JSqlParser/commit/5937bace81bd20c) wumpz *2016-11-14 11:47:43*

**corrected fix #311 and fix #332, introduced unaliased fqn of column**


[88804](https://github.com/JSQLParser/JSqlParser/commit/888041d1971257e) wumpz *2016-09-26 12:33:24*

**fixes #341**


[b3faf](https://github.com/JSQLParser/JSqlParser/commit/b3faf8eb8680484) wumpz *2016-09-20 21:53:32*

**Update README.md**


[98ded](https://github.com/JSQLParser/JSqlParser/commit/98ded4dfd487b7e) Tobias *2016-09-17 14:34:21*

**corrected some lookaheads**


[7c157](https://github.com/JSQLParser/JSqlParser/commit/7c1572febc89ae1) wumpz *2016-09-16 11:52:08*

**Updated header and removed unused methods.**


[2bfc9](https://github.com/JSQLParser/JSqlParser/commit/2bfc9ab124086b9) Peter Borissow *2016-09-15 15:28:59*

**Added json parsing tests.**


[7c740](https://github.com/JSQLParser/JSqlParser/commit/7c740854852d725) Peter Borissow *2016-09-15 14:59:01*

**Added support for PostgreSQL JSON Functions and Operators.**


[a5504](https://github.com/JSQLParser/JSqlParser/commit/a5504819c1d93f0) Peter Borissow *2016-09-14 02:28:01*

**Add support for more Postgres Create Table options**


[04c56](https://github.com/JSQLParser/JSqlParser/commit/04c56f283d24b44) Rob Story *2016-09-09 21:02:57*

**fixes #334**


[21a0d](https://github.com/JSQLParser/JSqlParser/commit/21a0df961b4dc70) wumpz *2016-09-01 07:45:04*

****


[8e229](https://github.com/JSQLParser/JSqlParser/commit/8e22953b3bdcf85) wumpz *2016-09-01 06:50:59*

****


[e6e7e](https://github.com/JSQLParser/JSqlParser/commit/e6e7ea7bd407e30) wumpz *2016-09-01 06:46:49*

**Support additional Postgres column types for alter table statements**


[42181](https://github.com/JSQLParser/JSqlParser/commit/42181b00cace324) Rob Story *2016-08-30 14:22:55*

**fixes #330**


[03a34](https://github.com/JSQLParser/JSqlParser/commit/03a344d221abc9b) wumpz *2016-08-29 07:13:00*

**fixes #329**


[3df49](https://github.com/JSQLParser/JSqlParser/commit/3df492bb5cadaee) wumpz *2016-08-28 21:06:08*

**updated readme**


[2acae](https://github.com/JSQLParser/JSqlParser/commit/2acaee03959f798) wumpz *2016-08-23 20:02:35*


## jsqlparser-0.9.6 (2016-08-23)

### Other changes

**#modify net.sf.jsqlparser.statement.insert.Insert   Method:toString() if itemsList and useSelectBrackets together not null will error**

* #modify PlainSelect.getStringList change sql append to StringBuilder 

[471b9](https://github.com/JSQLParser/JSqlParser/commit/471b94495623ee2) zheng.liu@baifendian.com *2016-08-15 06:22:23*

**corrected some lookaheads parsing delete statements**


[57890](https://github.com/JSQLParser/JSqlParser/commit/578909f24136b04) wumpz *2016-08-14 12:59:27*

**some cleanup**


[d34eb](https://github.com/JSQLParser/JSqlParser/commit/d34ebcab6ac939f) wumpz *2016-08-13 22:20:27*

**Update Alter and add AlterExpression class for multiple ADD/DROP expressions in a single ALTER statement**

* Update .jjt file to break out the AlterExpression into its own class and for Alter to compose multiple AlterExpressions 
* Update AlterTest for new AlterExpressions and test for multiple ADD/DROP statements in a single ALTER 

[e8d1c](https://github.com/JSQLParser/JSqlParser/commit/e8d1cf2cb1db16c) Rob Story *2016-08-11 19:24:41*

**Support for parse delete from table using join to another table like:**

* DELETE posts 
* FROM posts 
* INNER JOIN projects ON projects.project_id &#x3D; posts.project_id 
* WHERE projects.client_id &#x3D; :client_id 
* This necessitated some changes to the DeleteTest class, 
* specifically: 
* JSqlParserCC.jjt - changes on grammar of Delete statements. 
* Delete toString and DeleteDeParser. 

[b6eb5](https://github.com/JSQLParser/JSqlParser/commit/b6eb57b61f7b205) Lucas Oliveira *2016-08-06 16:28:11*

**fix bug of TablesNamesFinder when SubSlect has withItemsList, add corresponding test case**


[276dd](https://github.com/JSQLParser/JSqlParser/commit/276ddc8a4dd8bf7) Xin Quan *2016-08-04 01:44:08*

**fixes #309**


[37f06](https://github.com/JSQLParser/JSqlParser/commit/37f061014e21f01) wumpz *2016-08-03 22:02:45*

**fixes #309**


[acd16](https://github.com/JSQLParser/JSqlParser/commit/acd16e59b8d8378) wumpz *2016-08-03 22:02:04*

**fixes #303**


[043a5](https://github.com/JSQLParser/JSqlParser/commit/043a5cb59727cda) wumpz *2016-08-03 21:31:11*

**fixes #303**


[76982](https://github.com/JSQLParser/JSqlParser/commit/76982f7b00b54f7) wumpz *2016-08-03 21:30:04*

**intruced new test for oracle join syntax**


[cb8dd](https://github.com/JSQLParser/JSqlParser/commit/cb8ddd71254a640) wumpz *2016-08-03 21:05:38*

**fixes #246**


[822bb](https://github.com/JSQLParser/JSqlParser/commit/822bbbe78cc8481) wumpz *2016-07-31 20:56:56*

**fixes #247**


[42636](https://github.com/JSQLParser/JSqlParser/commit/4263651d43d4c60) wumpz *2016-07-28 20:13:41*

**test for issue 265**


[59f7e](https://github.com/JSQLParser/JSqlParser/commit/59f7ec2a2a432de) wumpz *2016-07-24 22:28:08*

**fixes #292**


[f0b35](https://github.com/JSQLParser/JSqlParser/commit/f0b350d13756b1f) wumpz *2016-07-24 22:13:37*

**fixes #299**


[d2ad3](https://github.com/JSQLParser/JSqlParser/commit/d2ad3dfe936c8ce) wumpz *2016-07-24 22:05:14*

**fixes #311**


[dfd2f](https://github.com/JSQLParser/JSqlParser/commit/dfd2fe55f0ad2bb) wumpz *2016-07-22 07:25:01*

****


[84bf0](https://github.com/JSQLParser/JSqlParser/commit/84bf0650e8f7d55) wumpz *2016-07-21 14:08:19*

**first try of error recovery for statement and statements**


[a7247](https://github.com/JSQLParser/JSqlParser/commit/a7247eb48369472) wumpz *2016-07-21 07:33:49*

**some refactoring**


[1e307](https://github.com/JSQLParser/JSqlParser/commit/1e30760dce107d9) wumpz *2016-07-18 16:50:28*

**The previous pull request broke the build.  Besides being a keyword, 'double' is also a function name.  Add K_DOUBLE to the RelObjectNameExt() function to pick up this distinction.**


[8bfc1](https://github.com/JSQLParser/JSqlParser/commit/8bfc1583f12049c) Tom Moore *2016-07-11 19:14:06*

**Add a double precision cast type**


[60ad1](https://github.com/JSQLParser/JSqlParser/commit/60ad18ed1a10328) Tom Moore *2016-07-11 17:59:53*

**removed one lookahead and improved parenthesis parsing**


[84a88](https://github.com/JSQLParser/JSqlParser/commit/84a883614af908a) wumpz *2016-06-30 18:41:06*

**removed one lookahead and improved parenthesis parsing**


[51b39](https://github.com/JSQLParser/JSqlParser/commit/51b39c55aaaa58c) wumpz *2016-06-30 18:37:23*

**fixes #296**

* refactored getTableList method of TableNamesFinder 

[3ccca](https://github.com/JSQLParser/JSqlParser/commit/3ccca01bbc85778) wumpz *2016-06-28 12:16:39*

**fixes #295**


[b877d](https://github.com/JSQLParser/JSqlParser/commit/b877da0a425c01f) wumpz *2016-06-28 08:19:36*

**fixes #293**


[c1d0b](https://github.com/JSQLParser/JSqlParser/commit/c1d0b2f5283d8bb) wumpz *2016-06-28 00:11:15*

**introduced OSGi metadata**


[89daf](https://github.com/JSQLParser/JSqlParser/commit/89dafaedeb69d6a) wumpz *2016-06-23 21:58:22*

**fixes #291**


[63fa2](https://github.com/JSQLParser/JSqlParser/commit/63fa2f66b537731) wumpz *2016-06-23 21:40:57*

**fixes #291**


[da42b](https://github.com/JSQLParser/JSqlParser/commit/da42b1dce108dd9) wumpz *2016-06-23 21:37:51*

**fixes #278**


[12341](https://github.com/JSQLParser/JSqlParser/commit/1234127a47199e6) wumpz *2016-06-21 23:02:11*

**fixes #278**


[faf8c](https://github.com/JSQLParser/JSqlParser/commit/faf8c6adea9bb28) wumpz *2016-06-21 22:58:40*

**fixes #287**


[b1bea](https://github.com/JSQLParser/JSqlParser/commit/b1bea0273c5557a) wumpz *2016-06-21 20:50:02*

**fixes #270**


[37c83](https://github.com/JSQLParser/JSqlParser/commit/37c8313e46e45a7) wumpz *2016-06-21 20:12:55*

****


[d3d66](https://github.com/JSQLParser/JSqlParser/commit/d3d66f819662698) wumpz *2016-06-20 07:08:58*

****


[0d1d1](https://github.com/JSQLParser/JSqlParser/commit/0d1d1c066d77ded) wumpz *2016-06-19 21:15:55*

**fixes #284**


[8e7ba](https://github.com/JSQLParser/JSqlParser/commit/8e7ba38a39c6f85) wumpz *2016-06-19 20:09:07*

**Implemented table check constraint for named constraints.**

* 1. Added named constraint to create table. 
* 2. Added check constraint to alter table statement. 
* 3. Added CheckConstraint type. 
* Tests: 
* 4. Added create table test. 
* 5. Added alter table test. 

[401d2](https://github.com/JSQLParser/JSqlParser/commit/401d279ef7e6ef9) Megan Woods *2016-06-16 14:56:01*

****


[04fc3](https://github.com/JSQLParser/JSqlParser/commit/04fc3aa9b53c8a9) wumpz *2016-06-15 20:50:52*

****


[bddbe](https://github.com/JSQLParser/JSqlParser/commit/bddbe588a759dd0) wumpz *2016-06-15 20:45:51*

**Implemented:**

* 1. UPDATE .. RETURNING col, col as Alias 
* 2. UPDATE .. RETURNING * 
* Tested: 
* 3. UPDATE .. ORDER BY .. LIMIT .. RETURNING 
* 4. UPDATE .. RETURNING 
* Item 4 represents the PostgreSQL UPDATE .. RETURNING Syntax without ORDER BY and LIMIT. 
* See: https://www.postgresql.org/docs/9.5/static/sql-update.html 

[f4526](https://github.com/JSQLParser/JSqlParser/commit/f452638f0f04f99) Megan Woods *2016-06-15 09:12:14*

****


[40aba](https://github.com/JSQLParser/JSqlParser/commit/40aba736f79c495) wumpz *2016-06-12 11:12:57*

**-- added scalar time functions of ANSI SQL**


[f51df](https://github.com/JSQLParser/JSqlParser/commit/f51df4b5f7d51e2) ChrissW-R1 *2016-06-08 12:57:26*

****


[24c87](https://github.com/JSQLParser/JSqlParser/commit/24c874afad3110f) wumpz *2016-06-06 21:50:44*

**updated readme**


[3bc31](https://github.com/JSQLParser/JSqlParser/commit/3bc316df743ab09) wumpz *2016-05-24 09:45:34*

**Add a test-case for Hive's LEFT SEMI JOIN**


[e56fb](https://github.com/JSQLParser/JSqlParser/commit/e56fbe984cfab98) Vu Nhan *2016-05-19 04:53:22*

**Add support for Hive's LEFT SEMI JOIN**


[fd03a](https://github.com/JSQLParser/JSqlParser/commit/fd03ad4f6bdd06a) Vu Nhan *2016-05-19 04:05:43*

**fixes #243**


[bb978](https://github.com/JSQLParser/JSqlParser/commit/bb978c45b4d744d) wumpz *2016-05-16 20:49:58*

**fixes #243**


[780ba](https://github.com/JSQLParser/JSqlParser/commit/780ba125a5807b2) wumpz *2016-05-16 20:47:32*

**fixes #261**


[10e68](https://github.com/JSQLParser/JSqlParser/commit/10e68b5c3c58296) wumpz *2016-05-16 20:03:07*

**updated readme**


[77ba3](https://github.com/JSQLParser/JSqlParser/commit/77ba380cfb639c4) wumpz *2016-05-06 06:06:33*

****


[57145](https://github.com/JSQLParser/JSqlParser/commit/5714516d314f31d) wumpz *2016-04-28 22:15:18*

**Added ability to have operators like '>=' or '<=' separated by a space.**

* This includes: 
* Modifying the JJT syntax to support the &#x27;space in the middle&#x27; versions 
* of operators (any quantity of whitespace is supported). 
* Modifying the various operators to inherit from a new 
* &#x27;ComparisonOperator&#x27; class, which handles the (previously NotEqualsTo- 
* only) logic for capturing the form of the operator. 
* Giving each of the various operators a constructor that accepts the 
* syntax used. 
* Modifying TestUtils to strip comments out before comparing SQL text 
* (necessary because condition07.sql is now passing, and has a comment). 
* Updating SpecialOracleTest to indicate 130 tests passing now 
* (condition7.sql now passes). 
* Adding a new test specifically for operators into SpecialOracleTest. 
* NOTE: Because the &quot;! &#x3D;&quot; form of the &#x27;not equals&#x27; operator means something 
* different in PostgresSQL (factorial of previous argument + equals), we do 
* NOT include that case. 

[9886b](https://github.com/JSQLParser/JSqlParser/commit/9886b02975d1749) Dave Lindquist *2016-04-28 13:28:58*

**Corrected "MERGE INTO" parsing for more complicated statements.**

* Specifically: 
* Changed &quot;Condition&quot; to &quot;Expression&quot; for the &quot;ON&quot; clause -- this is 
* needed to handle &quot;ON&quot; clauses that have &quot;a &#x3D; y AND b &#x3D; z&quot; or other 
* more complicated expressions (basically the same as the &quot;ON&quot; clause 
* in a SELECT query). 
* Also changed the &quot;WHERE&quot; and &quot;DELETE WHERE&quot; clauses in the same 
* fashion (&#x27;Condition&#x27; becomes &#x27;Expression&#x27;), as they too support 
* multiple conditions. 
* Corrected the toString on the MergeUpdate clause, which was missing a 
* comma between the fields. 
* Added a new, more complicated MERGE INTO statement to the MergeTest 
* class. 

[7efd5](https://github.com/JSQLParser/JSqlParser/commit/7efd58f7704a0ff) Dave Lindquist *2016-04-28 13:19:34*

**added for update test**


[d54b8](https://github.com/JSQLParser/JSqlParser/commit/d54b82ba62f1133) wumpz *2016-04-27 14:23:55*

**fixes #253**


[75be4](https://github.com/JSQLParser/JSqlParser/commit/75be42659dc3a76) wumpz *2016-04-26 06:11:13*

**fixed  #245**


[01287](https://github.com/JSQLParser/JSqlParser/commit/012874f905adabe) wumpz *2016-04-15 22:01:56*

**fixes #244**


[2204a](https://github.com/JSQLParser/JSqlParser/commit/2204a5fc85fb99d) wumpz *2016-04-15 20:10:32*

**Update README.md**


[ac4a5](https://github.com/JSQLParser/JSqlParser/commit/ac4a56151b46d0b) Tobias *2016-04-14 19:30:09*

**fixes #240**

* fixes #241 

[7f8b5](https://github.com/JSQLParser/JSqlParser/commit/7f8b59b31e44521) wumpz *2016-04-05 06:25:57*

**small modifications, reduces some semantic lookaheads**


[7f5b6](https://github.com/JSQLParser/JSqlParser/commit/7f5b61e60bf037b) wumpz *2016-03-29 11:34:17*

**fixed #228**


[3dfae](https://github.com/JSQLParser/JSqlParser/commit/3dfae9c62615711) wumpz *2016-03-17 08:40:24*

**fixed #228**


[8f9b2](https://github.com/JSQLParser/JSqlParser/commit/8f9b2b905c00c68) wumpz *2016-03-17 08:38:35*

**fixed #232 without correction of order of update and insert**


[7e2e7](https://github.com/JSQLParser/JSqlParser/commit/7e2e7200ce48672) wumpz *2016-03-17 08:28:47*

**fixed some whitespace differences between deparser and toString regarding NOT expression**


[f4b25](https://github.com/JSQLParser/JSqlParser/commit/f4b25599a8919d4) wumpz *2016-03-17 08:16:24*

**Update README.md**


[12009](https://github.com/JSQLParser/JSqlParser/commit/120096068fa4b3d) Tobias *2016-03-17 07:51:11*

**update release info**


[eccd6](https://github.com/JSQLParser/JSqlParser/commit/eccd66f01f924f8) wumpz *2016-03-14 00:27:18*

**Fixing uncaught exception**


[4fad4](https://github.com/JSQLParser/JSqlParser/commit/4fad4af810aef8d) emopers *2016-01-12 19:30:52*


## jsqlparser-0.9.5 (2016-03-13)

### Other changes

**no message**


[ffcfe](https://github.com/JSQLParser/JSqlParser/commit/ffcfe41096bca29) wumpz *2016-03-13 23:55:09*

****


[3c863](https://github.com/JSQLParser/JSqlParser/commit/3c8635280dbd959) wumpz *2016-03-13 23:35:37*

**introduced boolean values within conditions**


[36a62](https://github.com/JSQLParser/JSqlParser/commit/36a62e9de7ec65e) wumpz *2016-03-10 21:19:22*

**introduced boolean values within conditions**


[a8333](https://github.com/JSQLParser/JSqlParser/commit/a8333bf9310355b) wumpz *2016-03-10 21:16:28*

**introduced boolean values within conditions**


[68e5b](https://github.com/JSQLParser/JSqlParser/commit/68e5b53e834f02f) wumpz *2016-03-10 21:11:01*

**fixes #230**


[19915](https://github.com/JSQLParser/JSqlParser/commit/1991507179224f0) wumpz *2016-03-07 23:17:40*

**multiple code improvements: squid:S1905, squid:S00122, squid:S1155, squid:S00105**


[905b2](https://github.com/JSQLParser/JSqlParser/commit/905b28d34771d15) George Kankava *2016-03-03 11:40:31*

**fixed #226**


[67b17](https://github.com/JSQLParser/JSqlParser/commit/67b178b533128ae) wumpz *2016-02-27 13:54:58*

**fixes #223**


[04e5c](https://github.com/JSQLParser/JSqlParser/commit/04e5c8b354f149d) wumpz *2016-02-11 21:23:51*

**reduces a bunch of dynamic lookaheads to fixed ones**


[4c764](https://github.com/JSQLParser/JSqlParser/commit/4c764f54da7d820) wumpz *2016-02-11 20:51:02*

**integrated changes of #225**


[eb5d7](https://github.com/JSQLParser/JSqlParser/commit/eb5d7a29737c6c5) wumpz *2016-02-11 20:41:23*

**Multiple code improvements fix 1: squid:S1199, squid:S1066, squid:S1854, squid:S1165**


[af5d3](https://github.com/JSQLParser/JSqlParser/commit/af5d3cddf3564cf) George Kankava *2016-02-10 16:11:10*

**Update README.md**


[e455c](https://github.com/JSQLParser/JSqlParser/commit/e455ce024f53779) Tobias *2016-02-10 06:43:04*

**improved parsing performance**


[c78aa](https://github.com/JSQLParser/JSqlParser/commit/c78aa03d1af2cfe) wumpz *2016-02-09 22:30:20*

**fixes #221**


[579f0](https://github.com/JSQLParser/JSqlParser/commit/579f0bc159aa053) wumpz *2016-02-04 22:10:43*

**fixes #221**


[4a12d](https://github.com/JSQLParser/JSqlParser/commit/4a12dc10014ec51) wumpz *2016-02-04 22:07:12*

**cleaned up some lookaheads**


[0cc80](https://github.com/JSQLParser/JSqlParser/commit/0cc809ea7de5238) wumpz *2016-02-04 08:58:52*

****


[36a4a](https://github.com/JSQLParser/JSqlParser/commit/36a4a0f9918ef9e) wumpz *2016-02-02 06:45:45*

**fixes #217**


[de61c](https://github.com/JSQLParser/JSqlParser/commit/de61c0b92a25e03) wumpz *2016-02-02 06:21:59*

**Added reference options foreign keys support (ON UPDATE/DELETE NO ACTION/CASCADE) and Full text indexes (FULLTEXT idx(text1))**


[c2956](https://github.com/JSQLParser/JSqlParser/commit/c29565bcaed42b1) pabloa *2016-02-01 21:57:27*

**multiple code improvements 1**


[e6bec](https://github.com/JSQLParser/JSqlParser/commit/e6becde1df91db5) George Kankava *2016-02-01 07:35:08*

**Support of mysql create statements with timestamp column with ON UPDATE. Example: CREATE TABLE test (applied timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)**


[9dcaa](https://github.com/JSQLParser/JSqlParser/commit/9dcaaee63e4b2b3) pabloa98@gmail.com *2016-01-24 08:37:38*

**fixes #151**


[f5b51](https://github.com/JSQLParser/JSqlParser/commit/f5b515be9351c77) wumpz *2016-01-06 09:12:54*

**corrected lookahead for tablefunctions**


[8036e](https://github.com/JSQLParser/JSqlParser/commit/8036edd80628410) wumpz *2015-12-09 22:01:02*

**TableFunction extends FunctionItem**


[17371](https://github.com/JSQLParser/JSqlParser/commit/17371ae38e0384e) tfedkiv *2015-12-08 08:39:07*

**added TableFunction alias suppurt**

* added TableFunction unit tests 

[88edd](https://github.com/JSQLParser/JSqlParser/commit/88eddaf50dada22) tfedkiv *2015-12-07 15:27:02*

**fixed verion**


[a5a5f](https://github.com/JSQLParser/JSqlParser/commit/a5a5f9ee5b0d28d) ftaras *2015-12-07 14:25:07*

**added support of SELECT FROM table function (h2)**


[0403f](https://github.com/JSQLParser/JSqlParser/commit/0403fdda2918311) tfedkiv *2015-12-07 14:23:39*

**replaced size() with isEmpty()**


[1d634](https://github.com/JSQLParser/JSqlParser/commit/1d6348126dceec7) wumpz *2015-12-06 22:03:20*

**jdk 8 build included into travis**


[830be](https://github.com/JSQLParser/JSqlParser/commit/830be49c4de1311) wumpz *2015-11-27 22:12:36*

**increased version of maven-javadoc-plugin**


[1b842](https://github.com/JSQLParser/JSqlParser/commit/1b842f891e8b370) wumpz *2015-11-26 07:34:47*

****


[01d25](https://github.com/JSQLParser/JSqlParser/commit/01d25deaae72ba2) wumpz *2015-11-26 07:24:43*

**Issue 198: add profile to ensure doclint is turned off with Java 8+. Without this, the project won't build on Java 8. (You can run mvn test successfully, but not mvn package.)**


[bb3ef](https://github.com/JSQLParser/JSqlParser/commit/bb3ef6188982b9b) James Heather *2015-11-25 13:43:57*

**fixes #193**


[5d7bd](https://github.com/JSQLParser/JSqlParser/commit/5d7bdb9b254bc19) wumpz *2015-11-25 06:37:36*

**fixes #195**


[ab1ad](https://github.com/JSQLParser/JSqlParser/commit/ab1ad25b6862e08) wumpz *2015-11-25 06:22:12*

**Issue 195:**

* Add support for ORDER BY and LIMIT in UPDATE and DELETE statements (as supported by MySQL). 
* LimitDeparser and OrderByDeParser have been pulled out into separate classes to avoid code duplication from SelectDeParser. 

[a3133](https://github.com/JSQLParser/JSqlParser/commit/a31333a65141e27) James Heather *2015-11-24 14:57:04*

**corrects parsing error**


[975cd](https://github.com/JSQLParser/JSqlParser/commit/975cddfb85bd0b3) wumpz *2015-11-20 22:44:01*

****


[f1d21](https://github.com/JSQLParser/JSqlParser/commit/f1d213636786e0a) wumpz *2015-11-20 14:27:19*

**fixes #194**


[f1c98](https://github.com/JSQLParser/JSqlParser/commit/f1c9835bd34d3f1) wumpz *2015-11-18 21:17:49*

**support INSERT [LOW_PRIORITY | DELAYED | HIGH_PRIORITY] [IGNORE] ...**


[00c18](https://github.com/JSQLParser/JSqlParser/commit/00c18698acb0316) wanghai *2015-11-18 09:10:31*

**corrected lookahead**


[a97e9](https://github.com/JSQLParser/JSqlParser/commit/a97e9c94fef03c6) wumpz *2015-11-17 22:24:57*

**fixes #192, fixes #191**


[2fddf](https://github.com/JSQLParser/JSqlParser/commit/2fddf8c8bf4072c) wumpz *2015-11-14 23:56:53*

**replaceDeParser parser itemList**


[3c605](https://github.com/JSQLParser/JSqlParser/commit/3c6057b1f8d6311) wanghai *2015-11-13 08:45:21*

**support insert ... on duplicate key update...**


[7d7fa](https://github.com/JSQLParser/JSqlParser/commit/7d7fae8a31abc3c) wanghai *2015-11-13 08:43:57*

**fixes #181, added drop deparser**


[44091](https://github.com/JSQLParser/JSqlParser/commit/44091c622316769) wumpz *2015-11-09 22:33:56*

**alter table support improved**


[ad0dd](https://github.com/JSQLParser/JSqlParser/commit/ad0dd23e8951e27) wumpz *2015-10-27 22:37:56*

**fixes #180**


[0bb23](https://github.com/JSQLParser/JSqlParser/commit/0bb2358eaff8956) wumpz *2015-10-21 06:44:00*

**#182**


[a3695](https://github.com/JSQLParser/JSqlParser/commit/a369537d0d3f51c) wumpz *2015-10-21 05:56:19*

**Support for alter table drop column/constraint**

* Fix for Issue #184 

[58dd2](https://github.com/JSQLParser/JSqlParser/commit/58dd28aaf9b9273) schweighart *2015-10-20 21:34:51*

**updated readme**


[e570a](https://github.com/JSQLParser/JSqlParser/commit/e570a919f24d385) wumpz *2015-10-19 13:16:54*

**Improved required coverage**


[41cbd](https://github.com/JSQLParser/JSqlParser/commit/41cbd7f96ab9139) Rapševičius Valdas *2015-10-14 11:05:36*

**Refactored Oracle Hint tests, added set selects**


[7b4fe](https://github.com/JSQLParser/JSqlParser/commit/7b4feb00cd4d312) Rapševičius Valdas *2015-10-13 22:37:29*

**Added OracleHint class, grammar and model support, tests**


[f7f8d](https://github.com/JSQLParser/JSqlParser/commit/f7f8d03bfac9e09) Rapševičius Valdas *2015-10-13 22:06:16*

**resolved choice conflict**


[b6f71](https://github.com/JSQLParser/JSqlParser/commit/b6f71e619c1a24d) wumpz *2015-10-08 11:11:42*

**fixes #178, merged upstream**


[11dbd](https://github.com/JSQLParser/JSqlParser/commit/11dbda3b999d82b) Gabor Bota *2015-10-08 09:20:15*

**added restrict and set null for alter statement**


[36379](https://github.com/JSQLParser/JSqlParser/commit/36379b2f646326c) wumpz *2015-10-07 21:38:53*

**fixes #178**


[cb267](https://github.com/JSQLParser/JSqlParser/commit/cb2674e2b94ee95) Gabor Bota *2015-10-07 13:00:53*

**fixes #174**


[da1e0](https://github.com/JSQLParser/JSqlParser/commit/da1e074b308d74a) wumpz *2015-10-06 21:18:27*

**fixes #174**


[8d419](https://github.com/JSQLParser/JSqlParser/commit/8d419d31bb8be29) wumpz *2015-10-06 21:15:40*

**simple merge implementation**


[bc4bc](https://github.com/JSQLParser/JSqlParser/commit/bc4bc9e52192b21) wumpz *2015-10-01 22:52:36*

**simple merge implementation**


[af5a0](https://github.com/JSQLParser/JSqlParser/commit/af5a09078134f6c) wumpz *2015-10-01 22:44:50*

**fixes  #176**


[c6e93](https://github.com/JSQLParser/JSqlParser/commit/c6e9389599ed161) wumpz *2015-10-01 21:50:13*

**fixes  #176**


[fb4d4](https://github.com/JSQLParser/JSqlParser/commit/fb4d43b5a41d66b) wumpz *2015-10-01 21:45:44*

**fixes  #177**


[9999c](https://github.com/JSQLParser/JSqlParser/commit/9999c50ef3908a3) wumpz *2015-10-01 21:11:43*

****


[1fb42](https://github.com/JSQLParser/JSqlParser/commit/1fb426e08300adf) wumpz *2015-10-01 20:24:27*

**merge impl started**


[8d8c0](https://github.com/JSQLParser/JSqlParser/commit/8d8c0e4ce70d944) wumpz *2015-09-24 22:23:11*

**fixes #172**


[c690f](https://github.com/JSQLParser/JSqlParser/commit/c690f7f1efa5815) wumpz *2015-09-22 05:33:16*

****


[ae2c8](https://github.com/JSQLParser/JSqlParser/commit/ae2c87f5a19d9b0) wumpz *2015-09-16 18:55:58*

****


[03a4f](https://github.com/JSQLParser/JSqlParser/commit/03a4fc7d339cbb0) wumpz *2015-09-16 05:53:18*

**fixes #167**


[57f30](https://github.com/JSQLParser/JSqlParser/commit/57f3099b869cc43) wumpz *2015-09-16 05:52:27*

**fixes #167**


[71d9f](https://github.com/JSQLParser/JSqlParser/commit/71d9fd97fd92fbc) wumpz *2015-09-16 05:49:45*

**fixes #77**


[0e51d](https://github.com/JSQLParser/JSqlParser/commit/0e51dacc2a0df3a) wumpz *2015-09-16 05:46:49*

**fixes #170**


[cf9bf](https://github.com/JSQLParser/JSqlParser/commit/cf9bf8453791662) wumpz *2015-09-13 21:26:48*


## jsqlparser-0.9.4 (2015-09-13)

### Other changes

****


[e9024](https://github.com/JSQLParser/JSqlParser/commit/e9024106aa5d994) wumpz *2015-09-07 19:26:26*

**fixes #165**


[28101](https://github.com/JSQLParser/JSqlParser/commit/28101309a3502db) wumpz *2015-09-03 20:03:48*

**fixes #165**


[bf06e](https://github.com/JSQLParser/JSqlParser/commit/bf06e6ccc269efa) wumpz *2015-09-03 20:02:25*

**fixes #166**


[c244c](https://github.com/JSQLParser/JSqlParser/commit/c244ccb5cd00d0d) wumpz *2015-09-03 14:02:17*

**fixed #162**


[46381](https://github.com/JSQLParser/JSqlParser/commit/463817b435ffdd5) wumpz *2015-08-07 20:48:55*

**fixed #162**


[e3b73](https://github.com/JSQLParser/JSqlParser/commit/e3b73a35afbcb74) wumpz *2015-08-07 20:48:00*

**fixed #160**


[432c0](https://github.com/JSQLParser/JSqlParser/commit/432c0ef9a7d462d) wumpz *2015-08-07 20:24:40*

**no message**


[e91e0](https://github.com/JSQLParser/JSqlParser/commit/e91e074d8c773a6) wumpz *2015-08-05 20:44:25*

**Add support for variable support to "SELECT SKIP <ROWCOUNT> FIRST <ROWCOUNT>..." construct**

* The grammar for the construct in informix [1] mentions the possibility, that &lt;ROWCOUNT&gt; can be either 
* an integer or a host variable or local SPL variable storing the value of max. The case for plain integers and 
* jdbc variables is covered by the first commit While this commit adds support for constructs using SPL 
* variables. SPL variables must follow identifier rules [2][3]. 
* [1] http://www-01.ibm.com/support/knowledgecenter/SSGU8G_12.1.0/com.ibm.sqls.doc/ids_sqs_0156.htm 
* [2] http://www-01.ibm.com/support/knowledgecenter/SSGU8G_12.1.0/com.ibm.sqls.doc/ids_sqs_1306.htm?lang&#x3D;de 
* [3] http://www-01.ibm.com/support/knowledgecenter/SSGU8G_12.1.0/com.ibm.sqls.doc/ids_sqs_1660.htm%23ids_sqs_1660?lang&#x3D;de 

[9e77b](https://github.com/JSQLParser/JSqlParser/commit/9e77b6bd55ce242) Matthias Bläsing *2015-08-02 14:03:23*

**simplified lookahead**


[a44ac](https://github.com/JSQLParser/JSqlParser/commit/a44acb7a785394b) wumpz *2015-08-01 21:42:46*

**simplified lookahead**


[0fc8e](https://github.com/JSQLParser/JSqlParser/commit/0fc8e29a3a5df3d) wumpz *2015-08-01 21:33:31*

**Update README.md**


[b3d76](https://github.com/JSQLParser/JSqlParser/commit/b3d76e7847c6f90) Tobias *2015-07-31 05:27:21*

****


[6f2a1](https://github.com/JSQLParser/JSqlParser/commit/6f2a1323d03ae00) wumpz *2015-07-30 21:35:13*

**added another testcase for #154**


[e889c](https://github.com/JSQLParser/JSqlParser/commit/e889cf345a84b8d) wumpz *2015-07-29 10:50:10*

**Implement support for "SELECT SKIP <ROWCOUNT> FIRST <ROWCOUNT>..." construct**


[4a33d](https://github.com/JSQLParser/JSqlParser/commit/4a33d8c380260d2) Matthias Bläsing *2015-07-25 19:16:23*

**test for #154 included**


[001d6](https://github.com/JSQLParser/JSqlParser/commit/001d665d32c6df5) wumpz *2015-07-23 09:49:24*

****


[157ee](https://github.com/JSQLParser/JSqlParser/commit/157eebf7a07e23c) wumpz *2015-07-15 20:55:25*

**fixes #150**


[5b3ec](https://github.com/JSQLParser/JSqlParser/commit/5b3ec5ac9f502d4) wumpz *2015-07-15 20:42:54*

**fixes #149**


[d2b07](https://github.com/JSQLParser/JSqlParser/commit/d2b0706e6f175db) wumpz *2015-07-15 20:20:14*

**Fix inline usage of foreign keys in CREATE TABLE statements**


[23c19](https://github.com/JSQLParser/JSqlParser/commit/23c19a53fd72c26) Georg Semmler *2015-07-09 15:41:39*

**fixes #146**


[978b6](https://github.com/JSQLParser/JSqlParser/commit/978b60ebd0ffbbd) wumpz *2015-07-03 13:19:54*

**reincluded Apache 2.0 license**


[83899](https://github.com/JSQLParser/JSqlParser/commit/83899f824659012) wumpz *2015-07-02 20:54:03*

**reincluded Apache 2.0 license**


[7e52d](https://github.com/JSQLParser/JSqlParser/commit/7e52dd7cb474bc3) wumpz *2015-07-02 20:52:04*

**corrected deparser**


[cc4a5](https://github.com/JSQLParser/JSqlParser/commit/cc4a5fa149aeac4) wumpz *2015-07-01 20:51:03*

**updated readme**


[44716](https://github.com/JSQLParser/JSqlParser/commit/4471653646f286e) wumpz *2015-07-01 20:04:46*

**fixes #138 and AnyComparisionExpression**


[ab2b2](https://github.com/JSQLParser/JSqlParser/commit/ab2b2c07759af48) wumpz *2015-07-01 19:55:02*

**null toString used**


[e425d](https://github.com/JSQLParser/JSqlParser/commit/e425dc2528cc4d3) wumpz *2015-06-24 21:20:29*

**completed any and all comparisions**


[fc076](https://github.com/JSQLParser/JSqlParser/commit/fc076e89580ca78) wumpz *2015-06-24 20:59:45*

**Exceptions skiped during coverage tests**


[1849c](https://github.com/JSQLParser/JSqlParser/commit/1849c5b3f5ca03f) wumpz *2015-06-10 05:50:43*

**Update README.md**


[d561d](https://github.com/JSQLParser/JSqlParser/commit/d561d8ad60b84fc) Tobias *2015-06-09 21:39:04*

****


[a17d6](https://github.com/JSQLParser/JSqlParser/commit/a17d6280b1ecdf6) wumpz *2015-06-09 21:29:55*

**coveralls**


[5858a](https://github.com/JSQLParser/JSqlParser/commit/5858aa7a2215300) wumpz *2015-06-09 21:03:49*

****


[1d71c](https://github.com/JSQLParser/JSqlParser/commit/1d71c51a934dd1e) wumpz *2015-06-08 20:14:29*

**completed ExpressionVisitorAdapter**


[8ec0b](https://github.com/JSQLParser/JSqlParser/commit/8ec0b195ba8da00) wumpz *2015-06-07 22:37:50*

**completed ExpressionVisitorAdapter**


[cf703](https://github.com/JSQLParser/JSqlParser/commit/cf703d8225908d9) wumpz *2015-06-05 22:32:50*

**completed ExpressionVisitorAdapter**


[348fd](https://github.com/JSQLParser/JSqlParser/commit/348fd7ffce92125) wumpz *2015-06-05 22:29:30*

**updated some plugin versions**


[94c63](https://github.com/JSQLParser/JSqlParser/commit/94c63556b9d92a1) wumpz *2015-06-05 21:08:48*

**fixes #143**

* some refactorings done 

[4d2a0](https://github.com/JSQLParser/JSqlParser/commit/4d2a0a1151faff1) wumpz *2015-06-05 20:50:28*

**fixes #143**

* some refactorings done 

[f71c3](https://github.com/JSQLParser/JSqlParser/commit/f71c307f15c4cde) wumpz *2015-06-05 20:48:45*

**fixes #142**


[3d340](https://github.com/JSQLParser/JSqlParser/commit/3d340377b37ddd9) wumpz *2015-06-05 20:28:28*

**fixes #141**


[b78ed](https://github.com/JSQLParser/JSqlParser/commit/b78ed4b46472902) wumpz *2015-06-05 20:09:32*

****


[5123f](https://github.com/JSQLParser/JSqlParser/commit/5123fe295a4142b) wumpz *2015-06-05 20:07:12*

**root nodes established but not linked**


[71305](https://github.com/JSQLParser/JSqlParser/commit/71305d9cbdc0e5b) wumpz *2015-05-31 20:39:43*

**root nodes established but not linked**


[edd0c](https://github.com/JSQLParser/JSqlParser/commit/edd0c765ea34898) wumpz *2015-05-31 19:54:52*

**astnodes for columns and tables**


[d66a9](https://github.com/JSQLParser/JSqlParser/commit/d66a93a4066ccff) wumpz *2015-05-29 22:49:13*

**simple jjtree start**


[e594e](https://github.com/JSQLParser/JSqlParser/commit/e594e591e79f555) wumpz *2015-05-27 22:34:35*

**simple jjtree start**


[d9f5f](https://github.com/JSQLParser/JSqlParser/commit/d9f5fef5f9d43e1) wumpz *2015-05-24 21:08:32*

**fixes   #134 - preserve order of query**


[59470](https://github.com/JSQLParser/JSqlParser/commit/594705ae61fd1b5) wumpz *2015-05-24 20:28:51*

**fixes #136**


[5adeb](https://github.com/JSQLParser/JSqlParser/commit/5adebeee953bd2a) tejksat *2015-05-24 10:00:31*

**fixes   #134**


[d99e6](https://github.com/JSQLParser/JSqlParser/commit/d99e603e0e968f8) wumpz *2015-05-21 21:10:44*

**fixes  #72**


[8b540](https://github.com/JSQLParser/JSqlParser/commit/8b540614d7dbe9e) wumpz *2015-05-21 20:28:50*

**fixes  #72**


[3aaf1](https://github.com/JSQLParser/JSqlParser/commit/3aaf11d348e0b16) wumpz *2015-05-21 20:26:15*

**group_concat started**


[adca3](https://github.com/JSQLParser/JSqlParser/commit/adca3efe84aaeb3) wumpz *2015-05-20 21:48:36*

****


[3563c](https://github.com/JSQLParser/JSqlParser/commit/3563c2188f4ffde) wumpz *2015-05-12 22:32:17*


## jsqlparser-0.9.3 (2015-05-12)

### Other changes

**fixes  #69**


[16034](https://github.com/JSQLParser/JSqlParser/commit/16034878352a0fe) wumpz *2015-05-10 22:11:41*

**fixes  #69**


[35164](https://github.com/JSQLParser/JSqlParser/commit/35164e58c437fdc) wumpz *2015-05-10 22:02:24*

**fixes #90**


[536ba](https://github.com/JSQLParser/JSqlParser/commit/536ba9d091fbe75) wumpz *2015-05-07 22:53:42*

**fixes #90**


[db4a2](https://github.com/JSQLParser/JSqlParser/commit/db4a27e284ace55) wumpz *2015-05-07 22:50:35*

****


[55b8e](https://github.com/JSQLParser/JSqlParser/commit/55b8e7a4c9c947b) wumpz *2015-05-07 18:41:22*

**fixes #129**


[45132](https://github.com/JSQLParser/JSqlParser/commit/45132a7e3d57b15) wumpz *2015-04-30 19:07:21*

**fixes #128**


[aa291](https://github.com/JSQLParser/JSqlParser/commit/aa2913da90a4a05) wumpz *2015-04-27 21:59:00*

**Update README.md**


[c4f24](https://github.com/JSQLParser/JSqlParser/commit/c4f24e6a30b0b9d) Tobias *2015-04-23 08:56:37*

**fixes #126 - allows brackets around select**


[64b22](https://github.com/JSQLParser/JSqlParser/commit/64b22e45987284e) wumpz *2015-04-22 22:03:21*

**fixes #125 - added values as a column name**


[ac785](https://github.com/JSQLParser/JSqlParser/commit/ac785a405697df4) wumpz *2015-04-16 21:01:18*

**fixes #110 - first implementation**


[94195](https://github.com/JSQLParser/JSqlParser/commit/941952f58f5b5be) wumpz *2015-04-11 17:32:48*

**updated readme**


[f2d48](https://github.com/JSQLParser/JSqlParser/commit/f2d48cf28f452d4) wumpz *2015-04-09 21:52:40*

**solved some oracle test sql parsings**


[b76ba](https://github.com/JSQLParser/JSqlParser/commit/b76baa31439f841) wumpz *2015-04-09 21:45:16*

**fixes  #122**


[46f51](https://github.com/JSQLParser/JSqlParser/commit/46f51972cf47885) wumpz *2015-04-09 21:02:10*

**fixes  #123**


[68f47](https://github.com/JSQLParser/JSqlParser/commit/68f47dc7c49c1b2) wumpz *2015-04-09 20:23:50*

**refactoring**


[26e1c](https://github.com/JSQLParser/JSqlParser/commit/26e1c0ac1c9221f) wumpz *2015-04-08 21:20:40*

**refactoring**


[a9768](https://github.com/JSQLParser/JSqlParser/commit/a97686797e77480) wumpz *2015-04-08 21:17:30*

**fixes #120**


[c3995](https://github.com/JSQLParser/JSqlParser/commit/c39954916a0ab71) wumpz *2015-04-08 06:11:06*

**first try to fix  #114**


[a0d87](https://github.com/JSQLParser/JSqlParser/commit/a0d8733b6c57055) wumpz *2015-04-07 00:48:55*

****


[73822](https://github.com/JSQLParser/JSqlParser/commit/738226709622311) wumpz *2015-04-06 20:53:58*

**Update README.md**


[ad91b](https://github.com/JSQLParser/JSqlParser/commit/ad91b733c8b495c) Tobias *2015-04-06 20:34:30*

**travis**


[ade82](https://github.com/JSQLParser/JSqlParser/commit/ade827e1eda7248) wumpz *2015-04-06 20:04:47*

****


[673e0](https://github.com/JSQLParser/JSqlParser/commit/673e00b1996eaac) wumpz *2015-04-06 19:08:34*

**fixes  #109**


[a3285](https://github.com/JSQLParser/JSqlParser/commit/a32854822885741) wumpz *2015-04-02 23:08:59*

**fixes #119**


[c9b58](https://github.com/JSQLParser/JSqlParser/commit/c9b58b2b2dfba67) wumpz *2015-04-01 20:31:03*

**fixes #117**


[a3fc1](https://github.com/JSQLParser/JSqlParser/commit/a3fc1f23701d6d6) wumpz *2015-03-28 20:02:28*

**fixes #117**


[b3d91](https://github.com/JSQLParser/JSqlParser/commit/b3d91de4d54153f) wumpz *2015-03-28 19:50:20*

****


[f194b](https://github.com/JSQLParser/JSqlParser/commit/f194b7cb7cc810d) wumpz *2015-03-04 23:26:13*

**corrected lookup**


[8598d](https://github.com/JSQLParser/JSqlParser/commit/8598da5760028d6) wumpz *2015-03-04 22:33:16*

**updated readme**


[5472e](https://github.com/JSQLParser/JSqlParser/commit/5472e2d60e95590) wumpz *2015-03-04 22:28:45*

**fixes #115**


[a3e02](https://github.com/JSQLParser/JSqlParser/commit/a3e024a0b002ad0) wumpz *2015-03-04 22:18:59*

**fixes  #116**


[c916f](https://github.com/JSQLParser/JSqlParser/commit/c916f14c1e5f82c) wumpz *2015-03-04 21:55:29*

**Update README.md**


[18089](https://github.com/JSQLParser/JSqlParser/commit/18089564f2b9efb) Tobias *2015-02-12 22:54:15*


## jsqlparser-0.9.2 (2015-02-12)

### Other changes

**updated readme**


[13d29](https://github.com/JSQLParser/JSqlParser/commit/13d29e0d545bbaa) wumpz *2015-02-12 21:43:13*

**introduced user variables: fixes #107**


[0c288](https://github.com/JSQLParser/JSqlParser/commit/0c2889ca34c88fd) wumpz *2015-02-11 21:21:24*

**maybe not correct alter statement**


[ab001](https://github.com/JSQLParser/JSqlParser/commit/ab00181f492820a) wumpz *2015-02-04 22:53:06*

**fixes #102**


[d603e](https://github.com/JSQLParser/JSqlParser/commit/d603e41768a49fd) wumpz *2015-02-04 21:16:18*

**Update README.md**


[66517](https://github.com/JSQLParser/JSqlParser/commit/6651771afb0754f) Tobias *2015-02-01 20:03:21*

**fixes #91**


[495a7](https://github.com/JSQLParser/JSqlParser/commit/495a7f2590703db) wumpz *2015-02-01 00:07:07*

**Update README.md**


[d0ce4](https://github.com/JSQLParser/JSqlParser/commit/d0ce413581ace0d) Tobias *2015-01-30 21:30:28*

**fixes #89**


[4ac85](https://github.com/JSQLParser/JSqlParser/commit/4ac85f0b27c21ba) wumpz *2015-01-21 20:15:55*

**pivot function test**


[3cc5a](https://github.com/JSQLParser/JSqlParser/commit/3cc5acd2ee83f76) wumpz *2015-01-16 23:33:19*

**Update README.md**


[3bec5](https://github.com/JSQLParser/JSqlParser/commit/3bec524f829975b) Tobias *2015-01-11 10:37:55*

**fixes #99**


[d4bc7](https://github.com/JSQLParser/JSqlParser/commit/d4bc726944700c1) wumpz *2015-01-10 23:10:04*

**fixes #99**


[5ac27](https://github.com/JSQLParser/JSqlParser/commit/5ac27a7ceb6166d) wumpz *2015-01-10 23:08:28*

**small grammar cleanup**


[e4756](https://github.com/JSQLParser/JSqlParser/commit/e47566636b7fd83) wumpz *2014-12-18 21:24:23*

**fixes #93**


[02be9](https://github.com/JSQLParser/JSqlParser/commit/02be9cdbca314f6) wumpz *2014-12-10 23:33:19*

**fixes #92**


[e902a](https://github.com/JSQLParser/JSqlParser/commit/e902a41c71e1b44) wumpz *2014-12-10 23:17:40*

**updated readme**


[32bf3](https://github.com/JSQLParser/JSqlParser/commit/32bf3a2f1798270) wumpz *2014-12-10 21:56:12*

**add group ba additions to SelectUtils**


[dd77b](https://github.com/JSQLParser/JSqlParser/commit/dd77b6c4f2a1f24) wumpz *2014-12-08 22:51:59*

****


[adcaf](https://github.com/JSQLParser/JSqlParser/commit/adcaf0fe83dad8c) wumpz *2014-12-07 21:54:35*

**fixes #88**


[0d5cc](https://github.com/JSQLParser/JSqlParser/commit/0d5cc58237dbaae) wumpz *2014-12-03 23:45:07*

**oracle colls started**


[e8a18](https://github.com/JSQLParser/JSqlParser/commit/e8a18cc8b76c6bd) wumpz *2014-11-30 20:41:53*

**options**


[2e85a](https://github.com/JSQLParser/JSqlParser/commit/2e85a16ebf7bfe3) wumpz *2014-11-25 00:11:17*

**updated readme**


[72340](https://github.com/JSQLParser/JSqlParser/commit/72340e55d94f68e) wumpz *2014-11-24 20:17:52*

**added create table parameters to deparser**


[05967](https://github.com/JSQLParser/JSqlParser/commit/05967cce8c528db) wumpz *2014-11-24 20:10:48*

**added create parameters to include into deparser**


[6f89e](https://github.com/JSQLParser/JSqlParser/commit/6f89e35e4544112) wumpz *2014-11-23 23:36:35*

**added commit keyword**


[f276b](https://github.com/JSQLParser/JSqlParser/commit/f276b33528821ad) wumpz *2014-11-23 23:26:40*

**fixes #87**


[2056c](https://github.com/JSQLParser/JSqlParser/commit/2056cb064dffb3f) wumpz *2014-11-22 00:27:01*

**simple cleanup**


[6a98d](https://github.com/JSQLParser/JSqlParser/commit/6a98d2bd8dd504b) wumpz *2014-11-20 20:42:39*

**withitem - deparsing merged and modified**


[20e0f](https://github.com/JSQLParser/JSqlParser/commit/20e0f48c1a56da4) wumpz *2014-11-03 22:54:58*

**use accept() instead of toString() on StatementDeParser**


[67497](https://github.com/JSQLParser/JSqlParser/commit/674974ac08f822d) reed1 *2014-11-02 06:53:07*

**fixes #84**


[a5031](https://github.com/JSQLParser/JSqlParser/commit/a5031b403af80e4) wumpz *2014-10-31 22:57:23*

**for update selects implemented**


[92efe](https://github.com/JSQLParser/JSqlParser/commit/92efe5b962ede77) wumpz *2014-10-30 23:59:42*

**allow 'key' as object name**


[32b0a](https://github.com/JSQLParser/JSqlParser/commit/32b0a67999cf9ca) wumpz *2014-10-30 23:19:30*

**update readme**


[d9951](https://github.com/JSQLParser/JSqlParser/commit/d9951d7bc5129da) wumpz *2014-10-22 22:03:20*

**little housekeeping**


[5fb21](https://github.com/JSQLParser/JSqlParser/commit/5fb21dc2f605f81) wumpz *2014-10-22 21:51:37*

**Manage OFFSET and FETCH clauses in dedicated classes and rules in**

* jsqlparsercc.jj 
* Manage also jdbc parameter in these clauses. 

[26524](https://github.com/JSQLParser/JSqlParser/commit/26524ac850461cb) LionelNirva *2014-10-10 14:38:20*

**added test for wrong top distinct order.**


[ccefb](https://github.com/JSQLParser/JSqlParser/commit/ccefbeb29ac6693) wumpz *2014-10-09 19:23:52*

**Add support for new SQL Server 2012 and Oracle 12c versions of LIMIT**

* (equivalent to MySql and PostgreSQL LIMIT ... OFFSET ... clauses) for 
* parsing and deparsing. 

[dc215](https://github.com/JSQLParser/JSqlParser/commit/dc215bb6b8cf70b) LionelNirva *2014-10-08 12:38:04*

**Unit test for the fix Bug when Deparsing SQL Server request having TOP**

* and DISTINCT clauses. 

[c2ad9](https://github.com/JSQLParser/JSqlParser/commit/c2ad9d2b2c2ee9f) LionelNirva *2014-10-07 14:31:43*

**little housekeeping**


[fd25a](https://github.com/JSQLParser/JSqlParser/commit/fd25ab4b9279003) wumpz *2014-10-05 21:44:57*

**little housekeeping**


[c3ec6](https://github.com/JSQLParser/JSqlParser/commit/c3ec64d426102cc) wumpz *2014-10-05 21:38:24*

**compile error corrected**


[8ae4b](https://github.com/JSQLParser/JSqlParser/commit/8ae4bead1868ac7) wumpz *2014-10-05 21:15:34*

**Fix Bug when Deparsing SQL Server request having TOP and DISTINCT**

* clauses. SQL Server requires the DISTINCT clause to be the first. 

[7ac70](https://github.com/JSQLParser/JSqlParser/commit/7ac7002d1276f01) LionelNirva *2014-10-03 16:34:49*

**Update UpdateTest.java**

* Add an SQL test for Update with Select 

[aec82](https://github.com/JSQLParser/JSqlParser/commit/aec82516c9d0db6) CeeKayGit *2014-10-01 21:34:51*

**Update UpdateDeParser.java**


[f2fec](https://github.com/JSQLParser/JSqlParser/commit/f2fecb7b62f5882) CeeKayGit *2014-10-01 21:18:33*

**Update UpdateDeParser.java**


[e31ac](https://github.com/JSQLParser/JSqlParser/commit/e31ac1ad7090fc2) CeeKayGit *2014-10-01 20:51:26*

**Update Update.java**

* Add necessary Select import for Update with Select 

[8321a](https://github.com/JSQLParser/JSqlParser/commit/8321aae44a6adce) CeeKayGit *2014-10-01 20:37:42*

**Update UpdateDeParser.java**

* Extend the deparser to support DB2 Updates with Select clause 

[3a7bf](https://github.com/JSQLParser/JSqlParser/commit/3a7bf9d520ead88) CeeKayGit *2014-10-01 20:12:14*

**Update JSqlParserCC.jj**

* For DB2 &quot;$&quot; is also a standard letter, so handle &quot;$&quot; as #LETTER. 

[a6383](https://github.com/JSQLParser/JSqlParser/commit/a6383a52ccee448) CeeKayGit *2014-09-30 20:56:13*

**Update Update.java**

* Add support for DB2 Updates with Select clause 

[1bdb6](https://github.com/JSQLParser/JSqlParser/commit/1bdb69b8891ee26) CeeKayGit *2014-09-30 20:52:21*

**Update JSqlParserCC.jj**

* Add support for DB2 Updates with Select clause 

[17e3d](https://github.com/JSQLParser/JSqlParser/commit/17e3d539a4ce6bd) CeeKayGit *2014-09-30 20:47:54*

**Update README.md**


[23279](https://github.com/JSQLParser/JSqlParser/commit/232795fc575cdf1) Tobias *2014-09-23 20:32:34*


## jsqlparser-0.9.1 (2014-09-23)

### Other changes

**Update README.md**


[267b4](https://github.com/JSQLParser/JSqlParser/commit/267b443e23b95a2) Tobias *2014-09-22 07:25:49*

**corrected typo**


[90c93](https://github.com/JSQLParser/JSqlParser/commit/90c932332a6b8ed) wumpz *2014-09-07 20:28:38*

**refactored join processor to be more restrictive**


[f1544](https://github.com/JSQLParser/JSqlParser/commit/f154446364fd5db) wumpz *2014-09-06 20:24:16*

**simple execute clause support**


[531d6](https://github.com/JSQLParser/JSqlParser/commit/531d6177dfc03d1) wumpz *2014-08-14 21:46:11*

**simple start for execute**


[d6f01](https://github.com/JSQLParser/JSqlParser/commit/d6f0101b9a3d2e4) wumpz *2014-08-13 23:17:08*

**simple start for execute**


[406a1](https://github.com/JSQLParser/JSqlParser/commit/406a138cf3af9f7) wumpz *2014-08-13 23:08:35*

**updated readme**


[1532d](https://github.com/JSQLParser/JSqlParser/commit/1532d15c3dad9ff) wumpz *2014-08-12 14:33:26*

**correced select into parsing and deparsing**


[0b7f3](https://github.com/JSQLParser/JSqlParser/commit/0b7f3007dac51cb) wumpz *2014-08-12 14:29:46*

**refactored grammar a bit**


[096e8](https://github.com/JSQLParser/JSqlParser/commit/096e8742b6fff86) wumpz *2014-08-05 20:58:15*

**improved insert clause**


[a4de6](https://github.com/JSQLParser/JSqlParser/commit/a4de602442a73da) wumpz *2014-08-04 22:09:19*

**corrected a failing test**


[24dc0](https://github.com/JSQLParser/JSqlParser/commit/24dc08db8b9f536) wumpz *2014-07-30 20:47:49*

**updated readme**


[fc0ba](https://github.com/JSQLParser/JSqlParser/commit/fc0ba6a535c3f30) wumpz *2014-07-30 20:43:23*

**limit 0 and limit null included**


[50d3e](https://github.com/JSQLParser/JSqlParser/commit/50d3e8b4224bbaf) wumpz *2014-07-30 20:41:40*

**Add support for LIMIT 0 and LIMIT NULL statements**


[6db00](https://github.com/JSQLParser/JSqlParser/commit/6db009418e925ca) Michaël Cervera *2014-07-29 21:58:29*

**unlogged inlucded in deparser**


[e9939](https://github.com/JSQLParser/JSqlParser/commit/e9939fdfdfca726) wumpz *2014-07-27 21:42:18*

**Add support for 'UNLOGGED' tables**

* Support the PostgreSQL 9.1+ ‘UNLOGGED’ table feature 

[eb05c](https://github.com/JSQLParser/JSqlParser/commit/eb05ce30cb90b93) Michaël Cervera *2014-07-27 20:49:00*

**create table implemented version 2**


[98903](https://github.com/JSQLParser/JSqlParser/commit/989033d24e9b3a9) wumpz *2014-07-26 19:56:53*

**recent changes made more oracle tests succeed**


[bbf36](https://github.com/JSQLParser/JSqlParser/commit/bbf360e5620b073) wumpz *2014-07-24 20:04:01*

**updated readme**


[262d4](https://github.com/JSQLParser/JSqlParser/commit/262d48922d66d0f) wumpz *2014-07-24 19:59:25*

**replaced column list in expression list for partition by of analytic expressions**


[2ebaa](https://github.com/JSQLParser/JSqlParser/commit/2ebaaf03602a13b) wumpz *2014-07-24 19:53:17*

**updated readme**


[55e68](https://github.com/JSQLParser/JSqlParser/commit/55e68bfb4f84fa5) wumpz *2014-07-22 20:49:45*

**implemented create table .. as select ..**


[23b93](https://github.com/JSQLParser/JSqlParser/commit/23b938c096eaf67) wumpz *2014-07-22 20:48:00*

**simple improvements**


[5b3ea](https://github.com/JSQLParser/JSqlParser/commit/5b3ea032ff50712) wumpz *2014-07-16 20:27:54*

**added lookahead for regexp binary**


[75aed](https://github.com/JSQLParser/JSqlParser/commit/75aeded181a6f88) Sarah Komla-Ebri *2014-07-16 15:05:30*

**Added support for MySQL REGEXP BINARY (for case insensitivity)**


[ec0dc](https://github.com/JSQLParser/JSqlParser/commit/ec0dc88bc9b4280) Sarah Komla-Ebri *2014-07-16 13:50:37*

**Added support for MySQL REGEXP insensitivity case**


[4d9e4](https://github.com/JSQLParser/JSqlParser/commit/4d9e46198e0a679) Sarah Komla-Ebri *2014-07-16 13:27:25*

**simple first json syntax**


[cb674](https://github.com/JSQLParser/JSqlParser/commit/cb674bfa5937757) wumpz *2014-06-24 21:20:58*

**simple first json syntax**


[45ce9](https://github.com/JSQLParser/JSqlParser/commit/45ce94eb9c0c62a) wumpz *2014-06-24 21:19:04*

**returning implemented, column as identifier allowed**


[0020c](https://github.com/JSQLParser/JSqlParser/commit/0020c798f32c586) wumpz *2014-06-20 23:04:02*

**returning implemented, column as identifier allowed**


[733ff](https://github.com/JSQLParser/JSqlParser/commit/733ff6da5d59ac1) wumpz *2014-06-20 22:58:28*

**Update README.md**


[e12b8](https://github.com/JSQLParser/JSqlParser/commit/e12b8644326d3d2) Tobias *2014-06-04 20:50:35*

**Update README.md**


[e8927](https://github.com/JSQLParser/JSqlParser/commit/e8927d40ab55bdd) Tobias *2014-06-04 20:47:38*

**Update README.md**


[fb8ad](https://github.com/JSQLParser/JSqlParser/commit/fb8add01cb28b85) Tobias *2014-06-04 20:46:03*

**Update README.md**


[08c6a](https://github.com/JSQLParser/JSqlParser/commit/08c6a48b3180db4) Tobias *2014-06-04 20:45:22*

**fixes #56 : multitable updates**


[dfda1](https://github.com/JSQLParser/JSqlParser/commit/dfda1395c4275f6) wumpz *2014-05-25 20:16:31*

**fixes #56 : multitable updates**


[9c183](https://github.com/JSQLParser/JSqlParser/commit/9c183ed34760126) wumpz *2014-05-25 20:06:04*

**fixes #57: brackets were not handled properly**


[00367](https://github.com/JSQLParser/JSqlParser/commit/003674787cfa982) wumpz *2014-05-25 19:10:28*

**readme updated**


[55634](https://github.com/JSQLParser/JSqlParser/commit/5563419fd15e430) wumpz *2014-05-22 22:08:32*

**junit annotations**


[57154](https://github.com/JSQLParser/JSqlParser/commit/57154b37c22abfb) wumpz *2014-05-22 21:05:22*

**Unit test**


[8cc03](https://github.com/JSQLParser/JSqlParser/commit/8cc036b4a99117a) shuyangzhou *2014-05-20 21:52:29*

**TablesNamesFinder.getTableList(Delete) throws NPE when the sql does not have a where clause**


[6d828](https://github.com/JSQLParser/JSqlParser/commit/6d8287fcf9dd1ee) shuyangzhou *2014-05-20 21:52:24*

**upgrade to JavaCC 6.1.2**


[c3404](https://github.com/JSQLParser/JSqlParser/commit/c3404e01e59b8d5) wumpz *2014-05-16 06:49:21*

**Update README.md**


[67af4](https://github.com/JSQLParser/JSqlParser/commit/67af4cd64bf0af0) Tobias *2014-05-08 19:35:00*


## jsqlparser-0.9 (2014-05-08)

### Other changes

**support for some keywords as objectnames**


[60f2b](https://github.com/JSQLParser/JSqlParser/commit/60f2bc4345ad0c8) wumpz *2014-05-07 21:19:09*

**support for some keywords as objectnames**


[c126e](https://github.com/JSQLParser/JSqlParser/commit/c126e1e511d2deb) wumpz *2014-05-07 20:57:32*

**support for named pks included**


[3aabf](https://github.com/JSQLParser/JSqlParser/commit/3aabff2d744315e) wumpz *2014-04-20 23:56:06*

**support for named pks included**


[9e683](https://github.com/JSQLParser/JSqlParser/commit/9e683d3662350ce) wumpz *2014-04-20 23:51:10*

**util for conditional expression parsing included**


[ffeb7](https://github.com/JSQLParser/JSqlParser/commit/ffeb7b7013afaf4) wumpz *2014-04-07 21:29:42*

**util for conditional expression parsing included**


[bc5a6](https://github.com/JSQLParser/JSqlParser/commit/bc5a6a459533b4e) wumpz *2014-04-07 21:21:58*

**updated readme**


[01a82](https://github.com/JSQLParser/JSqlParser/commit/01a82c2e2da9ffb) wumpz *2014-03-23 21:21:41*

**updated readme**


[84979](https://github.com/JSQLParser/JSqlParser/commit/849796360b04aa1) wumpz *2014-03-22 22:43:53*

**changed configuration**


[96be5](https://github.com/JSQLParser/JSqlParser/commit/96be5d779bbf0bb) wumpz *2014-03-22 21:42:11*

**readme updated**


[986c8](https://github.com/JSQLParser/JSqlParser/commit/986c87e1c61f332) wumpz *2014-03-18 21:35:28*

**removed stachtrace printing for problematic sql scripts**


[7ff12](https://github.com/JSQLParser/JSqlParser/commit/7ff12fb4fded358) wumpz *2014-03-18 20:52:15*

**corrected some sql test scripts to be deparseable**

* corrected pivot handling in SelectDeParser 

[8ac25](https://github.com/JSQLParser/JSqlParser/commit/8ac250d9f90144d) wumpz *2014-03-12 23:24:47*

**site descriptor**


[a6265](https://github.com/JSQLParser/JSqlParser/commit/a626532c854c35b) wumpz *2014-03-12 19:56:37*

**pivot test sqls a little tweaked so parseing deparsing will work**


[dcd97](https://github.com/JSQLParser/JSqlParser/commit/dcd97c16a007c31) wumpz *2014-03-08 00:00:29*

**pivot for subquery corrected**


[eb480](https://github.com/JSQLParser/JSqlParser/commit/eb480d73dc02c5c) wumpz *2014-03-07 23:48:52*

**!= usage corrected**

* deparser for oracle hierarchical expressions corrected 
* order by asc/desc corrected 

[d4f74](https://github.com/JSQLParser/JSqlParser/commit/d4f744c761fffe9) wumpz *2014-03-06 22:27:11*

**ALL processing corrected**


[68938](https://github.com/JSQLParser/JSqlParser/commit/68938c79b0394e0) wumpz *2014-03-06 20:46:49*

**lax tests improved**


[ab578](https://github.com/JSQLParser/JSqlParser/commit/ab578af86ca2c26) wumpz *2014-03-05 23:17:40*

**toString for windowing elements corrected**

* lax equality test implemented 
* included oracle test sqls 
* included @ and # for identifiers 

[884dc](https://github.com/JSQLParser/JSqlParser/commit/884dcafa73ecfd2) wumpz *2014-03-05 23:01:03*

**First version of all *Visitor adapters + simple test**


[cdc42](https://github.com/JSQLParser/JSqlParser/commit/cdc42110478506b) aalmiray *2014-03-05 20:45:57*

**character set implemented**


[90048](https://github.com/JSQLParser/JSqlParser/commit/90048e535baa2f0) wumpz *2014-03-01 00:11:04*

**character set implemented**


[e7114](https://github.com/JSQLParser/JSqlParser/commit/e7114e3c53230c6) wumpz *2014-03-01 00:05:59*

**Update README.md**


[2c22b](https://github.com/JSQLParser/JSqlParser/commit/2c22b60433c9a51) Tobias *2014-02-20 22:51:32*


## jsqlparser-0.8.9 (2014-02-20)

### Other changes

**Update README.md**


[d6edd](https://github.com/JSQLParser/JSqlParser/commit/d6eddfd7e5d8a6b) Tobias *2014-02-15 22:11:51*

**readme**


[f82d8](https://github.com/JSQLParser/JSqlParser/commit/f82d8316f00c581) wumpz *2014-02-14 22:01:31*

**first statements version**


[4be27](https://github.com/JSQLParser/JSqlParser/commit/4be2700c2b5e0cd) wumpz *2014-02-14 21:56:27*

**update readme**


[ff94a](https://github.com/JSQLParser/JSqlParser/commit/ff94a3ded2c295c) wumpz *2014-02-11 00:13:16*

****


[c91d1](https://github.com/JSQLParser/JSqlParser/commit/c91d153020b588c) wumpz *2014-02-11 00:08:31*

**update readme**


[3b219](https://github.com/JSQLParser/JSqlParser/commit/3b21988a4857273) wumpz *2014-02-11 00:01:11*

**corrected some styling issues**


[a3a7f](https://github.com/JSQLParser/JSqlParser/commit/a3a7ff934980b41) wumpz *2014-02-10 23:44:22*

**backported analytic expressions from fork**


[43d97](https://github.com/JSQLParser/JSqlParser/commit/43d97ea79f1e48a) wumpz *2014-02-10 23:36:24*

**order by clause improved nulls first last**


[ce45f](https://github.com/JSQLParser/JSqlParser/commit/ce45fbcf7f2d3e3) wumpz *2014-02-09 23:59:29*

**added versions to pom**


[06bbd](https://github.com/JSQLParser/JSqlParser/commit/06bbda82b813e7b) wumpz *2014-02-08 11:01:56*

****


[9b2c2](https://github.com/JSQLParser/JSqlParser/commit/9b2c295a8552cf6) wumpz *2014-02-07 22:25:13*

**Updated most of the Maven dependencies;**


[e6aaf](https://github.com/JSQLParser/JSqlParser/commit/e6aaf8b260fadd7) Pap Lőrinc *2014-02-07 07:59:11*

**Added PERCENT support for the TOP statement;**


[5e127](https://github.com/JSQLParser/JSqlParser/commit/5e127196a96ced3) Pap Lőrinc *2014-02-07 07:58:51*

**Update README.md**


[53aab](https://github.com/JSQLParser/JSqlParser/commit/53aab5737c61d1e) Tobias *2014-02-06 21:18:20*

**readme updated**


[96654](https://github.com/JSQLParser/JSqlParser/commit/966541e5b90dbb8) wumpz *2014-02-06 21:14:04*

**added testcase, little refactoring**


[4f766](https://github.com/JSQLParser/JSqlParser/commit/4f76615cf3bbd9b) wumpz *2014-02-06 21:08:18*

**Modified the TOP expression to accept parentheses also;**


[b5849](https://github.com/JSQLParser/JSqlParser/commit/b5849b63d24bef3) Pap Lőrinc *2014-02-06 09:39:31*

**updated readme**


[e143a](https://github.com/JSQLParser/JSqlParser/commit/e143abbd6c6374f) wumpz *2014-02-04 22:57:24*

**removed Exception logging for false multipart name test**


[7a94f](https://github.com/JSQLParser/JSqlParser/commit/7a94fa7b57a085b) wumpz *2014-02-04 22:48:24*

****


[e1f06](https://github.com/JSQLParser/JSqlParser/commit/e1f06bb0204fe43) wumpz *2014-02-04 22:14:12*

**included maven site generation**


[fc016](https://github.com/JSQLParser/JSqlParser/commit/fc016126850b1ac) wumpz *2014-02-02 23:47:06*

**extended multipart identifier tests**


[a3e0f](https://github.com/JSQLParser/JSqlParser/commit/a3e0f9de56c4441) wumpz *2014-02-02 00:04:28*

****


[13f3c](https://github.com/JSQLParser/JSqlParser/commit/13f3c0c64f785fb) wumpz *2014-02-01 23:23:43*

**removed possibility of empty tablename**


[9a819](https://github.com/JSQLParser/JSqlParser/commit/9a819eb5ceae1f9) wumpz *2014-02-01 23:17:35*

**removed some changes**


[7fa4f](https://github.com/JSQLParser/JSqlParser/commit/7fa4f8501cc4453) wumpz *2014-02-01 22:55:57*

**signed expressions tests improved**


[a78ab](https://github.com/JSQLParser/JSqlParser/commit/a78ab94cd60353f) wumpz *2014-02-01 22:18:32*

**Replaced a leftover StringBuffer with StringBuilder;**


[9b8d7](https://github.com/JSQLParser/JSqlParser/commit/9b8d72739d464cc) Pap Lőrinc *2014-01-28 17:26:00*

**Corrected Sql Server multi-part table and column names (database.schema.table.column) in the select statement to accept 4 levels with empty inner parts;**


[ba6c5](https://github.com/JSQLParser/JSqlParser/commit/ba6c54de7da2efa) Pap Lőrinc *2014-01-28 17:23:37*

**Renamed getWholeColumnName and getWholeTableName to getFullyQualifiedName;**


[b4d54](https://github.com/JSQLParser/JSqlParser/commit/b4d547eeb04cb9a) Pap Lőrinc *2014-01-28 17:14:09*

**Corrected the signed expression behaviors and renamed InverseExpression to SignedExpression;**


[d1e71](https://github.com/JSQLParser/JSqlParser/commit/d1e7185633bf840) Pap Lőrinc *2014-01-28 08:36:06*

**Removed the leading and trailing whitespaces in the JavaCC parser file;**

* Organized the declared imports in order to ease further changes in the file and to remove unused ones; 
* Renamed the S_INTEGER token to S_LONG to be in sync with the S_DOUBLE token; 

[5d151](https://github.com/JSQLParser/JSqlParser/commit/5d151c7bdbe08fd) Pap Lőrinc *2014-01-28 08:24:04*

**fixes #34**


[144ca](https://github.com/JSQLParser/JSqlParser/commit/144ca3a140d1024) wumpz *2014-01-23 22:14:58*

**Update README.md**


[7024b](https://github.com/JSQLParser/JSqlParser/commit/7024bc7c1d1f553) Tobias *2014-01-22 23:23:54*


## jsqlparser-0.8.8 (2014-01-22)

### Other changes

**problem with git v1.8.5 adressed**


[9b12d](https://github.com/JSQLParser/JSqlParser/commit/9b12d17a4cb9744) wumpz *2014-01-22 23:05:23*

**version 0.8.8-SNAPSHOT**


[810ca](https://github.com/JSQLParser/JSqlParser/commit/810cabdafe2012a) wumpz *2014-01-22 22:41:41*

**version 0.8.8-SNAPSHOT**


[fdd3a](https://github.com/JSQLParser/JSqlParser/commit/fdd3a71426e473a) wumpz *2014-01-22 22:29:11*

**Update README.md**


[d4083](https://github.com/JSQLParser/JSqlParser/commit/d40837361354a27) Tobias *2014-01-21 23:37:41*

**addJoin introduced**


[c19f8](https://github.com/JSQLParser/JSqlParser/commit/c19f83f768a5e4a) wumpz *2014-01-21 23:35:34*

**readme updated**


[d3675](https://github.com/JSQLParser/JSqlParser/commit/d367559442a98ab) wumpz *2014-01-14 22:29:07*

**started simple utility function for select statement modification**


[bf454](https://github.com/JSQLParser/JSqlParser/commit/bf454e9a261cd4d) wumpz *2014-01-14 22:18:43*

**started simple utility function for select statement modification**


[6b5e2](https://github.com/JSQLParser/JSqlParser/commit/6b5e29af117e04e) wumpz *2014-01-14 22:11:13*

**update readme**


[311d6](https://github.com/JSQLParser/JSqlParser/commit/311d63639bda4a4) wumpz *2014-01-14 21:07:21*

**little housekeeping**


[1c927](https://github.com/JSQLParser/JSqlParser/commit/1c927884749b032) wumpz *2014-01-14 21:01:05*

**Alias class implemented and integrated**


[ce339](https://github.com/JSQLParser/JSqlParser/commit/ce3390677746faf) wumpz *2014-01-14 20:58:03*

**Added one simple insert SQL to test**


[2db02](https://github.com/JSQLParser/JSqlParser/commit/2db02ada400cd99) wumpz *2014-01-11 15:05:50*

****


[f58ca](https://github.com/JSQLParser/JSqlParser/commit/f58ca061b25ba5b) wumpz *2013-12-08 20:13:04*

**update readme**


[068b1](https://github.com/JSQLParser/JSqlParser/commit/068b19d17d9dba4) wumpz *2013-12-07 23:59:34*

**start alter statement**


[75575](https://github.com/JSQLParser/JSqlParser/commit/7557524708d0ce0) wumpz *2013-12-07 23:57:27*

**PostgresSQL regular expression match operators**


[e5e48](https://github.com/JSQLParser/JSqlParser/commit/e5e488d60c316f3) wumpz *2013-11-12 21:37:43*

**Update README.md**


[259dd](https://github.com/JSQLParser/JSqlParser/commit/259dd566a55a9b6) Tobias *2013-11-08 23:38:09*

**PostgresSQL regular expression case sensitive match**


[db923](https://github.com/JSQLParser/JSqlParser/commit/db923d53c5ceee7) wumpz *2013-11-08 23:33:46*

**PostgresSQL regular expression case sensitive match**


[88b5a](https://github.com/JSQLParser/JSqlParser/commit/88b5aa81b405d45) wumpz *2013-11-08 23:27:30*

**simple modifier cleanup**


[0528e](https://github.com/JSQLParser/JSqlParser/commit/0528ecca9768cf7) wumpz *2013-11-06 21:11:27*

**Update README.md**


[2a4bb](https://github.com/JSQLParser/JSqlParser/commit/2a4bbe5391e4dfd) Tobias *2013-10-30 22:37:09*

**update readme**


[e3b0e](https://github.com/JSQLParser/JSqlParser/commit/e3b0e6c4b5b7e48) wumpz *2013-10-30 22:31:03*


## jsqlparser-0.8.6 (2013-10-30)

### Other changes

**a little cleanup**


[bdc81](https://github.com/JSQLParser/JSqlParser/commit/bdc81933ab76b01) wumpz *2013-10-24 21:46:52*

**version not needed anymore**


[7a737](https://github.com/JSQLParser/JSqlParser/commit/7a737e70a76616d) wumpz *2013-10-24 21:34:14*

**Update README.md**


[f14e3](https://github.com/JSQLParser/JSqlParser/commit/f14e3c98917bc24) Tobias *2013-10-08 22:42:11*

**Update README.md**


[22c3e](https://github.com/JSQLParser/JSqlParser/commit/22c3ee9379b4f7c) Tobias *2013-10-08 22:37:23*

**update readme.md**


[d2dcf](https://github.com/JSQLParser/JSqlParser/commit/d2dcfaece51ceda) wumpz *2013-10-08 22:35:21*

**merge oracle hierarchical syntax into main**


[a0b21](https://github.com/JSQLParser/JSqlParser/commit/a0b21c36e5d2008) wumpz *2013-10-08 22:23:30*

**merge oracle hierarchical syntax into main**


[6c300](https://github.com/JSQLParser/JSqlParser/commit/6c30062bd7e9a59) wumpz *2013-10-08 22:22:19*

**OracleHierarchicalExpression implemented**


[b48f2](https://github.com/JSQLParser/JSqlParser/commit/b48f26e48a00f03) wumpz *2013-10-08 22:07:03*

**OracleHierarchicalExpression implemented**


[afa6e](https://github.com/JSQLParser/JSqlParser/commit/afa6e2d73d237f0) wumpz *2013-10-08 00:15:32*

**Update README.md**


[3901b](https://github.com/JSQLParser/JSqlParser/commit/3901bc6bf380099) Tobias *2013-10-07 12:09:23*

**Update README.md**


[9eb5f](https://github.com/JSQLParser/JSqlParser/commit/9eb5fa11dad1733) Tobias *2013-10-06 14:12:55*

**parser updated oracle recursives**


[aff53](https://github.com/JSQLParser/JSqlParser/commit/aff53603149d42c) wumpz *2013-10-01 19:07:16*

**begin implementation of oracle recursive queries**


[a7566](https://github.com/JSQLParser/JSqlParser/commit/a7566a7371bdca0) wumpz *2013-09-19 20:33:18*


## jsqlparser-0.8.5 (2013-10-06)

### Other changes

**preparing release of 0.8.5**


[7abce](https://github.com/JSQLParser/JSqlParser/commit/7abce433ce56bab) wumpz *2013-10-06 13:23:53*

**preparing release of 0.8.5**


[0e13b](https://github.com/JSQLParser/JSqlParser/commit/0e13b77a827dfb5) wumpz *2013-10-06 12:55:48*

**readme updated**


[3e68f](https://github.com/JSQLParser/JSqlParser/commit/3e68fa4928d5115) wumpz *2013-09-19 21:08:23*

**problems solved with postgresqls data type "character varying"**


[c84e5](https://github.com/JSQLParser/JSqlParser/commit/c84e59eadd380e0) wumpz *2013-09-19 21:05:18*

**problems solved with postgresqls data type "character varying"**


[f5af3](https://github.com/JSQLParser/JSqlParser/commit/f5af3053102af19) wumpz *2013-09-19 21:00:19*

**corrected version infos**


[9da72](https://github.com/JSQLParser/JSqlParser/commit/9da728241feb44b) wumpz *2013-09-17 21:49:19*

**Update README.md**


[da809](https://github.com/JSQLParser/JSqlParser/commit/da809493f5d6dbb) Tobias *2013-09-17 21:36:47*

**first snapshot deployed to sonatype**


[62970](https://github.com/JSQLParser/JSqlParser/commit/629705cc0a3d758) wumpz *2013-09-17 21:27:05*

**pom modification to publish to public repository**


[e60bc](https://github.com/JSQLParser/JSqlParser/commit/e60bc0e9fac79ee) wumpz *2013-09-17 12:14:43*

**pom modification to publish to public repository**


[085d4](https://github.com/JSQLParser/JSqlParser/commit/085d4970d2a56c8) wumpz *2013-09-17 09:11:36*

**CastExpression favours cast keyword instead of ::**


[68d12](https://github.com/JSQLParser/JSqlParser/commit/68d12944d835777) wumpz *2013-09-17 07:17:39*

**CastExpression favours cast keyword instead of ::**


[8cf8b](https://github.com/JSQLParser/JSqlParser/commit/8cf8b4b7733dbd4) wumpz *2013-09-17 07:16:27*

**removed unused modifiers**


[b3ee7](https://github.com/JSQLParser/JSqlParser/commit/b3ee75103cba4ec) wumpz *2013-08-29 20:22:48*

**improved function test**


[35608](https://github.com/JSQLParser/JSqlParser/commit/35608babd2caae1) wumpz *2013-08-29 19:32:20*

**workaround for mySql truncate function**


[c3b40](https://github.com/JSQLParser/JSqlParser/commit/c3b4046f8833357) wumpz *2013-08-29 19:22:57*


## jsqlparser-0.8.4 (2013-08-27)

### Other changes

**JJDoc output included in site configuration**


[b2688](https://github.com/JSQLParser/JSqlParser/commit/b2688799ada5918) wumpz *2013-08-25 19:30:45*

**Update README.md**


[891dd](https://github.com/JSQLParser/JSqlParser/commit/891dd0a8e7e3086) Tobias *2013-08-22 20:29:48*

**update readme**


[d375a](https://github.com/JSQLParser/JSqlParser/commit/d375ac9a51bc51e) wumpz *2013-08-22 20:20:29*

**update readme**


[e25d0](https://github.com/JSQLParser/JSqlParser/commit/e25d0be44a8223a) wumpz *2013-08-22 20:13:19*

**some minor additions to named parameters**


[8bff2](https://github.com/JSQLParser/JSqlParser/commit/8bff2c911467e84) wumpz *2013-08-22 20:11:19*

**added ability to parse named parameters**


[284ec](https://github.com/JSQLParser/JSqlParser/commit/284ec72cd00a574) audrium *2013-08-22 13:47:39*

**added changes to readme**


[6ab75](https://github.com/JSQLParser/JSqlParser/commit/6ab75599642eb23) wumpz *2013-08-14 21:34:21*

**added some test cases**


[00f76](https://github.com/JSQLParser/JSqlParser/commit/00f76fe1a853843) wumpz *2013-08-14 21:25:47*

**removed PivotForColumn**


[18904](https://github.com/JSQLParser/JSqlParser/commit/18904dd44c915d2) wumpz *2013-08-11 23:43:07*

**regexp_like transfered into a general boolean function**


[410a2](https://github.com/JSQLParser/JSqlParser/commit/410a2d125db963e) wumpz *2013-08-11 21:26:33*

**Add support old oracle join syntax to more expressions (simple comparisons and IN)**


[55f42](https://github.com/JSQLParser/JSqlParser/commit/55f42ea3712cf4d) Jonathan Burnhams *2013-08-02 07:52:05*

**Add support for lag and lead with offset and default value parameters**


[99b46](https://github.com/JSQLParser/JSqlParser/commit/99b46bf1e7b18ec) Jonathan Burnhams *2013-08-01 14:00:15*

**Added regexp_like support**


[33f4f](https://github.com/JSQLParser/JSqlParser/commit/33f4f66dbdd693d) Jonathan Burnhams *2013-08-01 13:26:05*

**Finished adding pivot support**


[43fe8](https://github.com/JSQLParser/JSqlParser/commit/43fe8ca9c71afff) Jonathan Burnhams *2013-08-01 08:56:50*

**Started to add pivot support**


[a9716](https://github.com/JSQLParser/JSqlParser/commit/a97160a801585fa) Jonathan Burnhams *2013-07-31 15:15:42*

**Ignore intellij**


[51783](https://github.com/JSQLParser/JSqlParser/commit/517839aa4734e24) Jonathan Burnhams *2013-07-31 13:25:20*

**Update README.md**


[3a8ea](https://github.com/JSQLParser/JSqlParser/commit/3a8eacabad85274) Tobias *2013-07-18 13:06:09*

**removed release plugin dryrun**


[6e45a](https://github.com/JSQLParser/JSqlParser/commit/6e45a4fb22fe72a) wumpz *2013-07-05 20:55:50*

**Update README.md**


[b53e7](https://github.com/JSQLParser/JSqlParser/commit/b53e74d3b50c5d5) Tobias *2013-07-05 05:58:12*

**maven release plugin for local git repository**


[bd05e](https://github.com/JSQLParser/JSqlParser/commit/bd05eab93f7303b) wumpz *2013-07-04 19:54:39*

**more sonar issues corrected**


[7fea9](https://github.com/JSQLParser/JSqlParser/commit/7fea946f0ea664e) wumpz *2013-07-04 19:23:29*

**Update README.md**


[79f32](https://github.com/JSQLParser/JSqlParser/commit/79f322aca2246bc) Tobias *2013-07-04 19:04:02*

**create table can now have foreign key definitions**

* fixes #14 

[0c41f](https://github.com/JSQLParser/JSqlParser/commit/0c41f27165a2f41) wumpz *2013-06-24 23:28:29*

**removed some unused imports**


[8c37c](https://github.com/JSQLParser/JSqlParser/commit/8c37ca2628188ac) wumpz *2013-06-20 21:59:24*

**more sonar identified stuff cleaned**


[51791](https://github.com/JSQLParser/JSqlParser/commit/5179164dc6d54b5) wumpz *2013-06-20 21:12:48*

**cleanup violations found by sonar**


[51d0b](https://github.com/JSQLParser/JSqlParser/commit/51d0ba3b1eec349) Ivan Vasyliev *2013-06-14 14:44:01*

**more sonar identified stuff cleaned**


[c84ed](https://github.com/JSQLParser/JSqlParser/commit/c84ed36f9555aa0) wumpz *2013-06-13 20:53:57*

**corrected Method names mentioned by Sonar**


[16e2d](https://github.com/JSQLParser/JSqlParser/commit/16e2d8dd4a3e7ac) wumpz *2013-06-13 20:46:02*

**cleaned up more critical points (sonar)**


[ad96c](https://github.com/JSQLParser/JSqlParser/commit/ad96cbb9c65e39f) wumpz *2013-06-13 20:42:40*

**solved critical "Performance - Method concatenates strings using + in a loop"**


[9b86e](https://github.com/JSQLParser/JSqlParser/commit/9b86e368e4b3b67) wumpz *2013-06-09 23:40:10*

**Update README.md**


[5c36d](https://github.com/JSQLParser/JSqlParser/commit/5c36da78eb4cc3d) Tobias *2013-06-08 22:14:15*

**Update README.md**


[cdbec](https://github.com/JSQLParser/JSqlParser/commit/cdbec9bc2194147) Tobias *2013-06-08 21:55:29*

**fixes #30**


[22f83](https://github.com/JSQLParser/JSqlParser/commit/22f839045489e20) wumpz *2013-05-30 20:11:44*

****


[4ebb5](https://github.com/JSQLParser/JSqlParser/commit/4ebb568f83de703) wumpz *2013-05-26 22:47:27*

**added readme entry**


[afc0e](https://github.com/JSQLParser/JSqlParser/commit/afc0e2957c5c52c) wumpz *2013-05-26 22:21:36*

**multi value IN expression introduced (a,b,c) in ...**

* fixes #30 

[3c443](https://github.com/JSQLParser/JSqlParser/commit/3c44391bf5f168f) wumpz *2013-05-26 22:19:03*

**introduces Interval expression**


[fc5e6](https://github.com/JSQLParser/JSqlParser/commit/fc5e6050533e34d) wumpz *2013-05-26 20:44:44*

**new version**


[f97c5](https://github.com/JSQLParser/JSqlParser/commit/f97c5a27faecc7b) wumpz *2013-05-23 22:03:06*

**new version**


[a470a](https://github.com/JSQLParser/JSqlParser/commit/a470ae965d06ab4) wumpz *2013-05-22 19:57:51*

**release**


[1366c](https://github.com/JSQLParser/JSqlParser/commit/1366caba4b4d087) wumpz *2013-05-22 19:57:21*

**readme**


[4a620](https://github.com/JSQLParser/JSqlParser/commit/4a620031bcbef1e) wumpz *2013-05-22 19:49:11*

**introduced generic list**


[8af04](https://github.com/JSQLParser/JSqlParser/commit/8af04647982fc78) wumpz *2013-05-22 19:38:36*

**refactored some test utility methods out into a new class**


[c6bc4](https://github.com/JSQLParser/JSqlParser/commit/c6bc4c3ae5fad83) wumpz *2013-05-22 19:33:05*

**improved test**


[5e106](https://github.com/JSQLParser/JSqlParser/commit/5e1069523441f0f) wumpz *2013-05-22 19:16:18*

**- corrected S_DOUBLE parsing**


[6e3dc](https://github.com/JSQLParser/JSqlParser/commit/6e3dce2af7f2edd) wumpz *2013-05-16 22:22:01*

**- solved critical grammar bug regarding concat expressions and parenthesis parsing**

* - introduced some tests to cover the above 
* - corrected ExpressionDeparser to deliver same result as toString for substractions 

[51365](https://github.com/JSQLParser/JSqlParser/commit/51365239ead790b) wumpz *2013-05-16 21:53:04*

**added simple delete sql check**


[45fad](https://github.com/JSQLParser/JSqlParser/commit/45fad04b620704c) wumpz *2013-05-03 20:36:31*

**add simple materialized view parsing without additional parameters**


[6b339](https://github.com/JSQLParser/JSqlParser/commit/6b339fc1e6478cc) wumpz *2013-04-24 20:48:16*

**add simple materialized view parsing without additional parameters**


[fa5da](https://github.com/JSQLParser/JSqlParser/commit/fa5daaa3cd8430b) wumpz *2013-04-24 20:45:22*

**Update README.md**


[1d923](https://github.com/JSQLParser/JSqlParser/commit/1d92374962fc745) Tobias *2013-04-24 06:03:14*

****


[a6615](https://github.com/JSQLParser/JSqlParser/commit/a661593121f07ac) wumpz *2013-04-23 21:14:24*

**- corrected TableNamesFinder to work properly on update statements**


[eab74](https://github.com/JSQLParser/JSqlParser/commit/eab747b7891cbd8) wumpz *2013-04-23 21:07:32*

**- Create View corrected in using set operations**


[97c03](https://github.com/JSQLParser/JSqlParser/commit/97c03337c1a2deb) wumpz *2013-04-23 20:42:51*

**- from clause can now be used in update statements**


[b24c1](https://github.com/JSQLParser/JSqlParser/commit/b24c1c727cd1f81) wumpz *2013-04-21 21:34:26*

**- from clause can now be used in update statements**


[b5c4c](https://github.com/JSQLParser/JSqlParser/commit/b5c4ce66d751579) wumpz *2013-04-21 21:31:30*

**- insertet toString in Update**

* - modified Update deparser to deliver better results 

[e2e4d](https://github.com/JSQLParser/JSqlParser/commit/e2e4d80675d525d) wumpz *2013-04-21 21:18:48*

**added cross join syntax support**


[777a0](https://github.com/JSQLParser/JSqlParser/commit/777a08b2afeb495) wumpz *2013-04-18 22:28:32*

**allow more complex expressions in extract clause**


[e551a](https://github.com/JSQLParser/JSqlParser/commit/e551a1603b1e2e6) wumpz *2013-04-18 21:49:45*

**allow more complex expressions in extract clause**


[ab2ab](https://github.com/JSQLParser/JSqlParser/commit/ab2ab73f08d4fc8) wumpz *2013-04-18 21:46:29*

**allow complete type in cast expression**


[8562d](https://github.com/JSQLParser/JSqlParser/commit/8562d7870a1c8c7) wumpz *2013-04-18 20:47:12*

**allow complete type in cast expression**


[905a6](https://github.com/JSQLParser/JSqlParser/commit/905a62377b3bedd) wumpz *2013-04-18 20:40:49*

**create view .. as (select ..) implemented**


[1b3c5](https://github.com/JSQLParser/JSqlParser/commit/1b3c507c922dd3a) wumpz *2013-04-18 20:23:19*

**corrected comma list to partition by**


[d82c7](https://github.com/JSQLParser/JSqlParser/commit/d82c7a189f1b579) wumpz *2013-04-18 20:01:55*

**corrected comma list to partition by**


[eafea](https://github.com/JSQLParser/JSqlParser/commit/eafea98e54e9b2e) wumpz *2013-04-18 19:59:20*

**corrected comma list to partition by**


[5d40d](https://github.com/JSQLParser/JSqlParser/commit/5d40d7f384935ee) wumpz *2013-04-18 19:55:03*

**added column names list to create view statement**


[b3a63](https://github.com/JSQLParser/JSqlParser/commit/b3a6354b5248884) wumpz *2013-04-17 22:15:38*

**added column names list to create view statement**


[b7e73](https://github.com/JSQLParser/JSqlParser/commit/b7e738a071c28d8) wumpz *2013-04-17 22:11:15*

**added more testcases**


[eae84](https://github.com/JSQLParser/JSqlParser/commit/eae84f640ba9619) wumpz *2013-04-07 21:08:13*

**Update README.md**


[f2765](https://github.com/JSQLParser/JSqlParser/commit/f27659d74fad122) Tobias *2013-04-05 19:34:33*

**support listing tables from other operations**


[1d09b](https://github.com/JSQLParser/JSqlParser/commit/1d09b0cd2a68fc5) Raymond Auge *2013-04-05 15:39:59*

**support for create index**


[dca7d](https://github.com/JSQLParser/JSqlParser/commit/dca7d9be7a6d63f) Raymond Auge *2013-04-05 13:54:56*

**added more tool functions to new tool class CCJSqlParserUtil**


[83a1e](https://github.com/JSQLParser/JSqlParser/commit/83a1e9590471581) wumpz *2013-03-23 22:31:09*

**added more tool functions to new tool class CCJSqlParserUtil**


[90341](https://github.com/JSQLParser/JSqlParser/commit/90341f89642659e) wumpz *2013-03-23 22:28:35*

**added more tool functions to new tool class CCJSqlParserUtil**


[56fe6](https://github.com/JSQLParser/JSqlParser/commit/56fe6558e978d8f) wumpz *2013-03-23 22:27:32*

**added more tool functions**


[71daa](https://github.com/JSQLParser/JSqlParser/commit/71daa4ceee100c0) wumpz *2013-03-23 22:20:20*

**switch to unicode parsing**


[886b7](https://github.com/JSQLParser/JSqlParser/commit/886b72c16d95a5a) wumpz *2013-03-21 22:58:37*

**ignoring jedit backup files**


[da1e3](https://github.com/JSQLParser/JSqlParser/commit/da1e319210c431b) Tobias Warneke *2013-03-20 20:33:35*

**Added automatic license header generation. The link to the original project was moved to the poms description.**

* Format of all sources done. 

[01f7a](https://github.com/JSQLParser/JSqlParser/commit/01f7a4c92670794) wumpz *2013-03-19 21:30:50*

**automatically create license header**


[8206d](https://github.com/JSQLParser/JSqlParser/commit/8206dea9caf33f9) wumpz *2013-03-19 21:05:27*

****


[26e3a](https://github.com/JSQLParser/JSqlParser/commit/26e3a8334563108) wumpz *2013-03-19 20:50:59*

****


[f37b7](https://github.com/JSQLParser/JSqlParser/commit/f37b7285295ec0a) wumpz *2013-03-19 20:38:04*

**wildcard extension for analytic expression**


[d36fd](https://github.com/JSQLParser/JSqlParser/commit/d36fd2bd7f17dba) wumpz *2013-03-19 20:18:46*

**readme updated**


[27be9](https://github.com/JSQLParser/JSqlParser/commit/27be9c57d64a2d3) wumpz *2013-03-19 00:16:05*

**analytic expressions updated**


[9d62c](https://github.com/JSQLParser/JSqlParser/commit/9d62c21b2553b9c) wumpz *2013-03-19 00:12:06*

**- additional test case for values**

* - additional test cases for analytical expressions 

[15fc8](https://github.com/JSQLParser/JSqlParser/commit/15fc834bf78ae91) wumpz *2013-03-18 23:59:07*

**Update README.md**


[d7f9f](https://github.com/JSQLParser/JSqlParser/commit/d7f9f39ab2b5e3c) Tobias *2013-03-17 00:32:02*

**additional test case**


[e7dc8](https://github.com/JSQLParser/JSqlParser/commit/e7dc8d0cb5bb192) wumpz *2013-03-17 00:26:21*

**multi value expression for select included**


[6b51f](https://github.com/JSQLParser/JSqlParser/commit/6b51f26b6025532) wumpz *2013-03-17 00:21:17*

**Update README.md**


[15a8d](https://github.com/JSQLParser/JSqlParser/commit/15a8d0dd8053881) Tobias *2013-03-14 21:43:12*

**multi value expression for insert included**


[898f3](https://github.com/JSQLParser/JSqlParser/commit/898f36937a9ad05) wumpz *2013-03-14 21:40:42*

**multi value expression for insert included**


[8f21c](https://github.com/JSQLParser/JSqlParser/commit/8f21c6608eacd56) wumpz *2013-03-14 21:36:44*

**corrected InsertDeParser to deliver same results like toString**


[95527](https://github.com/JSQLParser/JSqlParser/commit/955274b969983f6) wumpz *2013-03-12 22:15:07*

**test for lateral and TableNamesFinder**


[a3ec5](https://github.com/JSQLParser/JSqlParser/commit/a3ec55aa297eade) wumpz *2013-03-03 00:46:19*

**corrected readme**


[966d8](https://github.com/JSQLParser/JSqlParser/commit/966d8a7747becd1) wumpz *2013-02-27 23:27:02*

**corrected readme**


[20275](https://github.com/JSQLParser/JSqlParser/commit/202756d18453b25) wumpz *2013-02-27 23:13:22*

****


[3d72a](https://github.com/JSQLParser/JSqlParser/commit/3d72a4f45f7cdad) wumpz *2013-02-27 23:08:50*

**implemented lateral keyword**


[5bc39](https://github.com/JSQLParser/JSqlParser/commit/5bc39d3aaa74c24) wumpz *2013-02-27 23:05:01*

**add WithItem to visitor interface**


[10e8f](https://github.com/JSQLParser/JSqlParser/commit/10e8f7ffd9a3b07) wumpz *2013-02-20 23:06:07*

**some clean up**


[00799](https://github.com/JSQLParser/JSqlParser/commit/00799709a45f68c) wumpz *2013-02-20 22:47:14*

****


[35a17](https://github.com/JSQLParser/JSqlParser/commit/35a17b5a0190992) wumpz *2013-02-16 14:01:45*

**0.8.2-SNAPSHOT**


[e8ed7](https://github.com/JSQLParser/JSqlParser/commit/e8ed7c70fac2cd3) wumpz *2013-02-15 22:42:47*

**Release 0.8.1 to include in my maven repo**


[e41b3](https://github.com/JSQLParser/JSqlParser/commit/e41b333a4635c61) wumpz *2013-02-15 22:41:44*

**- problem with TablesNamesFinder: finds with - alias instead of tablenames**


[08236](https://github.com/JSQLParser/JSqlParser/commit/082362b174d3faf) wumpz *2013-02-15 22:41:07*

**- problem with TablesNamesFinder: finds with - alias instead of tablenames**


[43699](https://github.com/JSQLParser/JSqlParser/commit/436992983926da2) wumpz *2013-02-15 22:14:13*

**Update README.md**


[c438f](https://github.com/JSQLParser/JSqlParser/commit/c438fdd520ede8e) Tobias *2013-02-14 07:45:00*

**Update README.md**


[7beeb](https://github.com/JSQLParser/JSqlParser/commit/7beebabfe7eba67) Tobias *2013-02-14 00:11:32*

**deployment to github maven repository**


[d9fe3](https://github.com/JSQLParser/JSqlParser/commit/d9fe37ad578a0e2) wumpz *2013-02-14 00:04:46*

**Update README.md**


[5f306](https://github.com/JSQLParser/JSqlParser/commit/5f306a889b11754) Tobias *2013-01-04 09:39:02*

**CreateView merged in**


[0ba65](https://github.com/JSQLParser/JSqlParser/commit/0ba65276f0a67e0) wumpz *2013-01-04 09:32:08*

**CreateView merged in**


[b2eff](https://github.com/JSQLParser/JSqlParser/commit/b2eff8b0e0e7976) wumpz *2013-01-04 09:31:12*

**Add CREATE VIEW support**


[101ef](https://github.com/JSQLParser/JSqlParser/commit/101ef1d9e10e7d2) Jeffrey Gerard *2012-12-26 17:52:30*

**some housekeeping: replace string concats**


[db49b](https://github.com/JSQLParser/JSqlParser/commit/db49b43c2d7bace) wumpz *2012-11-15 22:38:04*

**some housekeeping: adding missing braces**


[5dabd](https://github.com/JSQLParser/JSqlParser/commit/5dabda1deca055a) wumpz *2012-11-15 22:33:05*

**some housekeeping: adding override annotations**


[5302b](https://github.com/JSQLParser/JSqlParser/commit/5302b4562af8bd0) wumpz *2012-11-15 22:21:05*

**changed source encoding to utf8 from win-1252**


[af7fc](https://github.com/JSQLParser/JSqlParser/commit/af7fc5c63a08f96) wumpz *2012-10-31 23:38:33*

**additional letters token rule included, to expand range of acceptable identifiers**


[86423](https://github.com/JSQLParser/JSqlParser/commit/86423b0ea8e9875) wumpz *2012-10-31 22:40:42*

**additional letters token rule included, to expand range of acceptable identifiers**


[aa8e1](https://github.com/JSQLParser/JSqlParser/commit/aa8e15a1923c0a1) wumpz *2012-10-31 19:16:02*

**Update README.md**


[50a56](https://github.com/JSQLParser/JSqlParser/commit/50a56b02c70dac2) Tobias *2012-10-27 22:42:10*

**initial import**


[be85e](https://github.com/JSQLParser/JSqlParser/commit/be85e2dd312821a) wumpz *2012-10-27 22:23:51*

**initial import**


[3331f](https://github.com/JSQLParser/JSqlParser/commit/3331fc415a20832) wumpz *2012-10-27 22:21:08*

**initial import**


[52ffc](https://github.com/JSQLParser/JSqlParser/commit/52ffcf6d42d673c) wumpz *2012-10-27 22:17:53*

**- introduced more generics in parser definition**


[4a89f](https://github.com/JSQLParser/JSqlParser/commit/4a89f9b93e3d4ff) wumpz *2012-10-26 22:23:16*

**- introduced more generics in parser definition**


[b0e4a](https://github.com/JSQLParser/JSqlParser/commit/b0e4a4712432492) wumpz *2012-10-26 22:16:59*

**- introduced more generics in parser definition**


[07317](https://github.com/JSQLParser/JSqlParser/commit/07317edca9c8f89) wumpz *2012-10-26 22:13:12*

**- introduced more generics in parser definition**


[fde24](https://github.com/JSQLParser/JSqlParser/commit/fde24683e0c9930) wumpz *2012-10-12 20:55:17*

**- clean up merge conflicts**


[e838d](https://github.com/JSQLParser/JSqlParser/commit/e838d9778060478) wumpz *2012-10-12 20:13:43*

**- clean up merge conflicts**


[bf5d1](https://github.com/JSQLParser/JSqlParser/commit/bf5d17f1594c7ca) wumpz *2012-10-12 20:11:30*

**Added SELECT parsing and JUnit test**


[e9766](https://github.com/JSQLParser/JSqlParser/commit/e9766966ccd7809) Christian Bockermann *2012-10-01 09:21:38*

**- corrected changelog**


[1047b](https://github.com/JSQLParser/JSqlParser/commit/1047bb1877cc1ac) wumpz *2012-09-16 21:14:25*

**- difference problem between deparser and tostring for function without parameters resolved**


[aee0f](https://github.com/JSQLParser/JSqlParser/commit/aee0fa79d5eb085) wumpz *2012-09-16 21:13:08*

**- difference problem between deparser and tostring for function without parameters resolved**


[90f48](https://github.com/JSQLParser/JSqlParser/commit/90f48ff7b87767a) wumpz *2012-09-16 21:11:10*

**- ExtractExpression integrated**

* - Tests ExtractExpression started 
* - Function problem found 

[7ea63](https://github.com/JSQLParser/JSqlParser/commit/7ea63d17c1ef299) wumpz *2012-09-16 21:01:53*

**- extract syntax integrated into jj file**


[7a50b](https://github.com/JSQLParser/JSqlParser/commit/7a50b960e974183) wumpz *2012-09-15 21:26:39*

**- expansion warnings removed by introducing lookaheads**


[db904](https://github.com/JSQLParser/JSqlParser/commit/db90438f3daab6f) wumpz *2012-09-15 21:04:18*

****


[3da19](https://github.com/JSQLParser/JSqlParser/commit/3da190b0b31a30d) wumpz *2012-09-11 19:33:24*

****


[ab60c](https://github.com/JSQLParser/JSqlParser/commit/ab60cfe02940fb8) wumpz *2012-09-08 20:58:20*

**- moved tables names finder to utils package of source package. it is a too useful tool to live only in the test packages**


[57025](https://github.com/JSQLParser/JSqlParser/commit/5702587ce4abae9) wumpz *2012-09-08 19:59:35*

**- moved tables names finder to utils package of source package. it is a too useful tool to live only in the test packages**


[4d33d](https://github.com/JSQLParser/JSqlParser/commit/4d33ded5322ca3b) wumpz *2012-09-08 19:57:25*

**- Tool expression connector included**


[bb726](https://github.com/JSQLParser/JSqlParser/commit/bb726681905a11b) wumpz *2012-09-08 19:46:28*

**- Tool alias adder implemented**


[2ee7a](https://github.com/JSQLParser/JSqlParser/commit/2ee7aa9688f8775) wumpz *2012-09-03 23:32:15*

**imports corrected**


[147e8](https://github.com/JSQLParser/JSqlParser/commit/147e8b70ef75e81) wumpz *2012-09-01 22:05:02*

**Fix some obvious compiler warnings**


[ea7a1](https://github.com/JSQLParser/JSqlParser/commit/ea7a174bf86f0bf) Ian Bacher *2012-06-13 20:35:48*

****


[99b92](https://github.com/JSQLParser/JSqlParser/commit/99b92b3e4e57860) wumpz *2012-06-12 21:49:05*

**quoted columns in create table statement included**

* CreateTableDeParser corrected (NPE with no indexes, toString delivers now same) 
* CreateTableTest expanded 

[69287](https://github.com/JSQLParser/JSqlParser/commit/69287d7b2e8b278) wumpz *2012-05-26 19:58:14*

**complex with tests included**

* exists formatting included from fork 

[7e39e](https://github.com/JSQLParser/JSqlParser/commit/7e39e9ec4f48448) wumpz *2012-05-23 19:57:10*

**changed version number due to visitor incombatibilities**


[87315](https://github.com/JSQLParser/JSqlParser/commit/87315094501ffbc) wumpz *2012-05-23 19:08:39*

****


[877ea](https://github.com/JSQLParser/JSqlParser/commit/877ea2c6783bf79) wumpz *2012-05-19 23:04:42*

**- removed deprecated union class (replaced by SetOperationList)**


[bce12](https://github.com/JSQLParser/JSqlParser/commit/bce1290bea487cf) wumpz *2012-05-19 22:09:03*

**set operation handling done**


[673eb](https://github.com/JSQLParser/JSqlParser/commit/673eb25f97c04d8) wumpz *2012-05-19 21:42:21*

**start implementing union intersection etc. set operation handling**


[e980b](https://github.com/JSQLParser/JSqlParser/commit/e980bf3ac12a6b3) wumpz *2012-05-18 21:17:50*

****


[37ecf](https://github.com/JSQLParser/JSqlParser/commit/37ecf0a5a7e26ab) wumpz *2012-05-18 20:11:59*

**- Added Oracle (+) join Syntax (instead of taba left join tabn on a=b oracle allows taba,tabn where a=b(+)  )**


[31e94](https://github.com/JSQLParser/JSqlParser/commit/31e9435776bebaa) wumpz *2012-05-18 20:10:14*

**- Analytic functions added: corrected PARTITION BY**


[4f2cb](https://github.com/JSQLParser/JSqlParser/commit/4f2cbb3eff61683) wumpz *2012-05-17 13:44:50*

**- Analytic functions added: row_number() over (order by a,c)**


[b8543](https://github.com/JSQLParser/JSqlParser/commit/b8543ac725fe773) wumpz *2012-05-17 12:46:52*

**Added support for modulo expression (5 % 4)**


[ae513](https://github.com/JSQLParser/JSqlParser/commit/ae51331f7c82d04) wumpz *2012-05-17 12:19:50*

**Added support for modulo expression (5 % 4)**


[4ef84](https://github.com/JSQLParser/JSqlParser/commit/4ef842925b27438) wumpz *2012-05-16 21:30:14*

**Include bracket quotation of columns and aliases. For example MSAccess supports this.**


[e6d5a](https://github.com/JSQLParser/JSqlParser/commit/e6d5accd0db8e1f) wumpz *2012-05-16 20:33:44*

****


[d6a22](https://github.com/JSQLParser/JSqlParser/commit/d6a22d1076b95f0) wumpz *2012-05-15 21:50:56*

**- allowed simple expressions in case else**


[54d2c](https://github.com/JSQLParser/JSqlParser/commit/54d2c54658d1644) wumpz *2012-05-15 21:36:59*

**- make "select function" pass assertSqlCanBeParsedAndDeparsed**


[21296](https://github.com/JSQLParser/JSqlParser/commit/21296032a0ef7db) wumpz *2012-05-15 21:00:51*

**- make "select function" pass assertSqlCanBeParsedAndDeparsed**


[f769e](https://github.com/JSQLParser/JSqlParser/commit/f769ef2e562f485) wumpz *2012-05-15 20:57:07*

**- make "select function" pass assertSqlCanBeParsedAndDeparsed**


[e5853](https://github.com/JSQLParser/JSqlParser/commit/e5853695b96ac0f) wumpz *2012-05-15 20:55:23*

**- make "select function" pass assertSqlCanBeParsedAndDeparsed**


[7a8bb](https://github.com/JSQLParser/JSqlParser/commit/7a8bbbe8abf9681) toben *2012-05-13 22:21:38*

**- CAST - statement included**


[e22f5](https://github.com/JSQLParser/JSqlParser/commit/e22f5a3b6fe70aa) toben *2012-05-13 22:09:34*

**removed main and junit 3 only artifacts from source code**


[3f854](https://github.com/JSQLParser/JSqlParser/commit/3f854eb7cbe63d9) toben *2012-05-13 12:39:52*

**junit upgrade to 4.10**


[44b0d](https://github.com/JSQLParser/JSqlParser/commit/44b0d01d19571d0) toben *2012-05-13 12:35:33*

**Refactoring of SelectTest**


[e1c80](https://github.com/JSQLParser/JSqlParser/commit/e1c80a0434df739) Florent Bécart *2011-12-02 07:20:18*

**Removal of tests that are not able to pass**

* - Functions do not accept conditions as arguments 

[aa5f0](https://github.com/JSQLParser/JSqlParser/commit/aa5f044c5bebc78) Florent Bécart *2011-12-02 07:20:18*

**Code reformat**


[272bf](https://github.com/JSQLParser/JSqlParser/commit/272bf237eddcdc2) Florent Bécart *2011-12-02 07:20:18*

**Modify DeParsers to output the same result as statement.toString();**


[2a35e](https://github.com/JSQLParser/JSqlParser/commit/2a35efd003ca5b5) Alice Rapunzel *2011-11-21 08:53:24*

**Changed DeParsers to generic list types.**


[666ca](https://github.com/JSQLParser/JSqlParser/commit/666cac2edecc8d7) Christian Bockermann *2011-09-17 07:58:43*

**Changed all lists to their appropriate generics type.**


[0dd60](https://github.com/JSQLParser/JSqlParser/commit/0dd60b143eae3f1) Christian Bockermann *2011-09-17 07:53:14*

**.gitignore update**


[dd2a7](https://github.com/JSQLParser/JSqlParser/commit/dd2a75bd7fde3fb) Florent Bécart *2011-09-16 22:41:36*

**Add the sources generated by javacc-maven-plugin to eclipse classpath**


[66556](https://github.com/JSQLParser/JSqlParser/commit/6655673c1531cac) Florent Bécart *2011-09-16 22:41:36*

**Set the version of javacc-maven-plugin in pom.xml as recommended by maven**


[3f9c4](https://github.com/JSQLParser/JSqlParser/commit/3f9c4cc36c1a628) Florent Bécart *2011-09-16 22:41:36*

**Project cleanup**


[0cb63](https://github.com/JSQLParser/JSqlParser/commit/0cb637199c9cd95) Florent Bécart *2011-09-16 22:41:36*

**README update**


[00b8c](https://github.com/JSQLParser/JSqlParser/commit/00b8ca8dbb2ca6b) Florent Bécart *2011-09-16 22:41:36*

**Added README**


[2005d](https://github.com/JSQLParser/JSqlParser/commit/2005d6db8594102) Christian Bockermann *2011-09-16 20:22:29*

**Removed javcc-5.0 directory (no covered by Maven plugin)**


[61fb4](https://github.com/JSQLParser/JSqlParser/commit/61fb4a16876f9e4) Christian Bockermann *2011-09-16 20:13:28*

**Added support for select without from-list ( SELECT 1 + 2 )**

* Modified test cases for test resources in src/test/resources 

[0e5d7](https://github.com/JSQLParser/JSqlParser/commit/0e5d732e65bf622) Christian Bockermann *2011-09-16 20:06:07*

**Moved classes to match Maven directory layout.**


[a8d70](https://github.com/JSQLParser/JSqlParser/commit/a8d70350729d085) Christian Bockermann *2011-09-16 19:48:54*

**Use of StringBuilders instead of StringBuffers**


[ef03d](https://github.com/JSQLParser/JSqlParser/commit/ef03d68f8a365fc) Florent Bécart *2011-06-23 00:50:08*

**Files generated by JavaCC should be ignored by Git**


[b5aff](https://github.com/JSQLParser/JSqlParser/commit/b5affcc84aaf6d7) Florent Bécart *2011-06-23 00:49:59*

**Removal of files generated by JavaCC**


[767e3](https://github.com/JSQLParser/JSqlParser/commit/767e3c4e257512e) Florent Bécart *2011-06-23 00:05:33*

**Bug Fix: the column deparser should use the table alias if any**


[bc8dc](https://github.com/JSQLParser/JSqlParser/commit/bc8dcbbe1fdae50) Florent Bécart *2011-06-23 00:01:57*

**Use of generics**

* - List of expressions in ItemsList 
* - List of columns in Statement 
* - List of joins in PlainSelect 
* - List of withItems in Select 
* - List of plainSelects in Union 
* - List of orderByElements in Union 
* - List of columns in Update 
* - List of expressions in Update 

[8b0b8](https://github.com/JSQLParser/JSqlParser/commit/8b0b865780ee962) Florent Bécart *2011-06-22 20:00:44*

**Insert statements accept both keywords "value" and "values"**


[1db31](https://github.com/JSQLParser/JSqlParser/commit/1db31a4491b01f1) Florent Bécart *2011-06-20 23:14:11*

**Organize imports**


[db5e2](https://github.com/JSQLParser/JSqlParser/commit/db5e2501d8eefd5) Florent Bécart *2011-06-20 22:49:36*

**Code reformat**


[fed74](https://github.com/JSQLParser/JSqlParser/commit/fed74aec2acf3db) Florent Bécart *2011-06-20 22:48:00*

**Fixes a compilation error by removing an unused import**


[b9ce3](https://github.com/JSQLParser/JSqlParser/commit/b9ce35c945ffdab) Florent Bécart *2011-06-20 22:35:16*

**Eclipse configuration files**

* Makes development environment installation easier 

[b93af](https://github.com/JSQLParser/JSqlParser/commit/b93af3e4f333cdc) Florent Bécart *2011-06-20 22:33:30*

**Add .gitignore file**

* Auto-generated files that won&#x27;t be pushed to the repo are: 
* - classes: compiled main code 
* - docs: project javadoc 
* - *.jar: distribution jar (sources and documentation) 
* - lib: library jar (.class files) 
* - testclasses: compiled unit tests 

[55197](https://github.com/JSQLParser/JSqlParser/commit/5519799ce41c45b) Florent Bécart *2011-06-20 22:08:46*

**Remove auto-generated folders docs and lib**


[d2f54](https://github.com/JSQLParser/JSqlParser/commit/d2f5416986e5c9b) Florent Bécart *2011-06-20 22:06:36*

**Add junit library (required for ant build)**


[e0ad5](https://github.com/JSQLParser/JSqlParser/commit/e0ad59290667fc2) Florent Bécart *2011-06-20 21:50:19*

**Add javacc-5.0 folder (required for ant build)**


[36ddc](https://github.com/JSQLParser/JSqlParser/commit/36ddcb236313093) Florent Bécart *2011-06-20 21:48:21*

**Initial commit (base version: 0.7.0)**

* Source: http://sourceforge.net/projects/jsqlparser/files/jsqlparser/jsqlparser-0.7.0.jar/download 

[67c91](https://github.com/JSQLParser/JSqlParser/commit/67c9150f5d93ced) Florent Bécart *2011-06-20 21:44:37*


