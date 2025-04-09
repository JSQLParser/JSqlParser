******************************
How to use it
******************************

.. hint::

    1) **Quoting:** Double Quotes ``".."`` are used for quoting identifiers. Parsing T-SQL on **MS SQL Server** or **Sybase** with Squared Brackets ``[..]`` depends on ``Squared Bracket Quotation`` as shown in section :ref:`Define the Parser Features` below.

    2) JSQLParser uses a more restrictive list of ``Reserved Keywords`` and such keywords will **need to be quoted**.

    3) **Escaping:** JSQLParser pre-defines standard compliant **Single Quote** ``'..`` **Escape Character**. Additional Back-slash ``\..`` Escaping needs to be activated by setting the ``BackSlashEscapeCharacter`` parser feature. See section :ref:`Define the Parser Features` below for details.

    4) Oracle Alternative Quoting is partially supported for common brackets such as ``q'{...}'``, ``q'[...]'``, ``q'(...)'`` and ``q''...''``.

    5) Supported Statement Separators are Semicolon ``;``, ``GO``, Slash ``/`` or two empty lines ``\n\n\n``.


Compile from Source Code
==============================

You will need to have ``JDK 8`` or ``JDK 11`` installed. Please note that JSQLParser-4.9 is the last ``JDK 8`` compatible release and all development after will depend on ``JDK 11``. Building JSQLParser-5.1 and newer with Gradle will depend on a JDK17 toolchain due to the used plugins.

.. tab:: Maven

  .. code-block:: shell

    git clone --depth 1 https://github.com/JSQLParser/JSqlParser.git
    cd JSqlParser
    mvn install

.. tab:: Gradle

  .. code-block:: shell

    git clone --depth 1 https://github.com/JSQLParser/JSqlParser.git
    cd JSqlParser
    gradle publishToMavenLocal



Build Dependencies
==============================

.. tab:: Maven Release

    .. code-block:: xml
        :substitutions:

        <dependency>
            <groupId>com.github.jsqlparser</groupId>
            <artifactId>jsqlparser</artifactId>
            <version>|JSQLPARSER_VERSION|</version>
        </dependency>

.. tab:: Maven Snapshot

    .. code-block:: xml
        :substitutions:

        <repositories>
            <repository>
                <id>jsqlparser-snapshots</id>
                <snapshots>
                    <enabled>true</enabled>
                </snapshots>
                <url>https://oss.sonatype.org/content/groups/public/</url>
            </repository>
        </repositories>
        <dependency>
            <groupId>com.github.jsqlparser</groupId>
            <artifactId>jsqlparser</artifactId>
            <version>|JSQLPARSER_SNAPSHOT_VERSION|</version>
        </dependency>

.. tab:: Gradle Stable

    .. code-block:: groovy
        :substitutions:

        repositories {
            mavenCentral()
        }

        dependencies {
            implementation 'com.github.jsqlparser:jsqlparser:|JSQLPARSER_VERSION|'
        }

.. tab:: Gradle Snapshot

    .. code-block:: groovy
        :substitutions:

        repositories {
            maven {
                url = uri('https://oss.sonatype.org/content/groups/public/')
            }
        }

        dependencies {
            implementation 'com.github.jsqlparser:jsqlparser:|JSQLPARSER_SNAPSHOT_VERSION|'
        }


Parse a SQL Statement
==============================			

Parse the SQL Text into Java Objects:

.. code-block:: java

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


For guidance with the API, use `JSQLFormatter <http://jsqlformatter.manticore-projects.com>`_ to visualize the Traversable Tree of Java Objects:

.. raw:: html

    <div class="highlight">
    <pre>
    SQL Text
          └─Statements: net.sf.jsqlparser.statement.select.Select
              ├─selectItems -> Collection<SelectItem>
              │  └─LongValue: 1
              ├─Table: dual
              └─where: net.sf.jsqlparser.expression.operators.relational.EqualsTo
                 ├─Column: a
                 └─Column: b
   </pre>
   </div>

Error Handling
==============================

There are two features for handling errors

- ``parser.withErrorRecovery(true)`` will continue to the next statement separator and return an empty statement.
- ``parser.withUnsupportedStatements(true)`` will return an instance of the `UnsupportedStatement` class, although the first statement **must** be a regular statement

.. code-block:: java
    :caption: Error Recovery

    CCJSqlParser parser = new CCJSqlParser(
            "select * from mytable; select from; select * from mytable2" );
    Statements statements = parser.withErrorRecovery().Statements();

    // 3 statements, the failing one set to NULL
    assertEquals(3, statements.size());
    assertNull(statements.get(1));

    // errors are recorded
    assertEquals(1, parser.getParseErrors().size());

.. code-block:: java
    :caption: Unsupported Statement

    Statements statements = CCJSqlParserUtil.parseStatements(
            "select * from mytable; select from; select * from mytable2; select 4;"
            , parser -> parser.withUnsupportedStatements() );

    // 4 statements with one Unsupported Statement holding the content
    assertEquals(4, statements.size());
    assertInstanceOf(UnsupportedStatement.class, statements.get(1));
    assertEquals("select from", statements.get(1).toString());

    // no errors records, because a statement has been returned
    assertEquals(0, parser.getParseErrors().size());


Use the Visitor Patterns
==============================

Traverse the Java Object Tree using the Visitor Patterns:

.. code-block:: java

    // Define an Expression Visitor reacting on any Expression
    // Overwrite the visit() methods for each Expression Class
    ExpressionVisitorAdapter<Void> expressionVisitorAdapter = new ExpressionVisitorAdapter<>() {
        public <S> Void visit(EqualsTo equalsTo, S context) {
            equalsTo.getLeftExpression().accept(this, context);
            equalsTo.getRightExpression().accept(this, context);
            return null;
        }
        public <S> Void visit(Column column, S context) {
            System.out.println("Found a Column " + column.getColumnName());
            return null;
        }
    };

    // Define a Select Visitor reacting on a Plain Select invoking the Expression Visitor on the Where Clause
    SelectVisitorAdapter<Void> selectVisitorAdapter = new SelectVisitorAdapter<>() {
        @Override
        public <S> Void visit(PlainSelect plainSelect, S context) {
            return plainSelect.getWhere().accept(expressionVisitorAdapter, context);
        }
    };

    // Define a Statement Visitor for dispatching the Statements
    StatementVisitorAdapter<Void> statementVisitor = new StatementVisitorAdapter<>() {
        public <S> Void visit(Select select, S context) {
            return select.getSelectBody().accept(selectVisitorAdapter, context);
        }
    };

    String sqlStr="select 1 from dual where a=b";
    Statement stmt = CCJSqlParserUtil.parse(sqlStr);

    // Invoke the Statement Visitor without a context
    stmt.accept(statementVisitor, null);

Find Table Names
==============================

The class ``net.sf.jsqlparser.util.TablesNamesFinder`` can be used to return all Table Names from a Query or an Expression.

.. code-block:: java

     // find in Statements
     String sqlStr = "select * from A left join B on A.id=B.id and A.age = (select age from C)";
     Set<String> tableNames = TablesNamesFinder.findTables(sqlStr);
     assertThat( tableNames ).containsExactlyInAnyOrder("A", "B", "C");

     // find in Expressions
     String exprStr = "A.id=B.id and A.age = (select age from C)";
     tableNames = TablesNamesFinder.findTablesInExpression(exprStr);
     assertThat( tableNames ).containsExactlyInAnyOrder("A", "B", "C");


Build a SQL Statement
==============================

Build any SQL Statement from Java Code using a fluent API:

.. code-block:: java

    String expectedSQLStr = "SELECT 1 FROM dual t WHERE a = b";

    // Step 1: generate the Java Object Hierarchy for
    Table table = new Table().withName("dual").withAlias(new Alias("t", false));

    Column columnA = new Column().withColumnName("a");
    Column columnB = new Column().withColumnName("b");
    Expression whereExpression =
            new EqualsTo().withLeftExpression(columnA).withRightExpression(columnB);

    PlainSelect select = new PlainSelect().addSelectItem(new LongValue(1))
            .withFromItem(table).withWhere(whereExpression);

    // Step 2a: Print into a SQL Statement
    Assertions.assertEquals(expectedSQLStr, select.toString());

    // Step 2b: De-Parse into a SQL Statement
    StringBuilder builder = new StringBuilder();
    StatementDeParser deParser = new StatementDeParser(builder);
    deParser.visit(select);

    Assertions.assertEquals(expectedSQLStr, builder.toString());


Define the Parser Features
==============================

JSQLParser interprets Squared Brackets ``[..]`` as Arrays, which does not work with MS SQL Server and T-SQL. Please use the Parser Features to instruct JSQLParser to read Squared Brackets as Quotes instead.

JSQLParser allows for standard compliant Single Quote ``'..`` Escaping. Additional Back-slash ``\..`` Escaping needs to be activated by setting the ``BackSlashEscapeCharacter`` parser feature.

Additionally there are Features to control the Parser's effort at the cost of the performance.

.. code-block:: java

    String sqlStr="select 1 from [sample_table] where [a]=[b]";

    // T-SQL Square Bracket Quotation
    Statement stmt = CCJSqlParserUtil.parse(
            sqlStr
            , parser -> parser
                .withSquareBracketQuotation(true)
    );

    // Set Parser Timeout to 6000 ms
    Statement stmt1 = CCJSqlParserUtil.parse(
            sqlStr
            , parser -> parser
                .withSquareBracketQuotation(true)
                .withTimeOut(6000)
    );

    // Allow Complex Parsing (which allows nested Expressions, but is much slower)
    Statement stmt2 = CCJSqlParserUtil.parse(
            sqlStr
            , parser -> parser
                .withSquareBracketQuotation(true)
                .withAllowComplexParsing(true)
                .withTimeOut(6000)
    );

    // Allow Back-slash escaping
    sqlStr="SELECT ('\\'Clark\\'', 'Kent')";
    Statement stmt2 = CCJSqlParserUtil.parse(
            sqlStr
            , parser -> parser
                .withBackslashEscapeCharacter(true)
    );
