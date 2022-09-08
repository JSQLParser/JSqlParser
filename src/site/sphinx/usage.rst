******************************
How to use it
******************************

.. warning::

	1) Parsing **T-SQL on MS SQL Server** or Sybase depends on ``Squared Bracket Quotation`` as shown in section :ref:`Define the Parser Features` below.

	2) JSQLParser uses a more restrictive list of ``Reserved Keywords`` and such keywords will **need to be quoted**.



Compile from Source Code
==============================

You will need to have ``JDK 8`` or ``JDK 11`` installed.

.. tabs::

  .. tab:: Maven

    .. code-block:: shell

			git clone https://github.com/JSQLParser/JSqlParser.git
			cd jsqlformatter	
			mvn install

  .. tab:: Gradle

	  .. code-block:: shell
    
			git clone https://github.com/JSQLParser/JSqlParser.git
			cd jsqlformatter
			gradle build



Maven Artifacts
==============================

.. tabs::
	

  .. tab:: Stable Release

		.. code-block:: xml
			:substitutions:

			<dependency>
				<groupId>com.github.jsqlparser</groupId>
				<artifactId>jsqlparser</artifactId>
				<version>|JSQLPARSER_VERSION|</version>
			</dependency>

  .. tab:: Development Snapshot
		
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

			
Parse a SQL Statements
==============================			

Parse the SQL Text into Java Objects:

.. code-block:: java

	String sqlStr="select 1 from dual where a=b";

	Statement statement = CCJSqlParserUtil.parse(sqlStr);
	if (statement instanceof Select) {
		Select select = (Select) statement;
		PlainSelect plainSelect = (PlainSelect)  select.getSelectBody();

		SelectExpressionItem selectExpressionItem = (SelectExpressionItem) plainSelect.getSelectItems().get(0);
		Assertions.assertEquals( new LongValue(1), selectExpressionItem.getExpression());

		Table table = (Table) plainSelect.getFromItem();
		Assertions.assertEquals("dual", table.getName());

		EqualsTo equalsTo = (EqualsTo) plainSelect.getWhere();
		Column a = (Column) equalsTo.getLeftExpression();
		Column b = (Column) equalsTo.getRightExpression();
		Assertions.assertEquals("a", a.getColumnName());
		Assertions.assertEquals("b", b.getColumnName());
	}


For guidance with the API, use `JSQLFormatter <http://jsqlformatter.manticore-projects.com>`_ to visualize the Traversable Tree of Java Objects:

.. raw:: html

    <div class="highlight">
    <pre>
    SQL Text
     └─<font color="#739FCF"><b>Statements</b></font>: <font color="#836B00">net.sf.jsqlparser.statement.select.Select</font>
        └─<font color="#739FCF"><b>selectBody</b></font>: <font color="#836B00">net.sf.jsqlparser.statement.select.PlainSelect</font>
           ├─<font color="#739FCF"><b>selectItems</b></font> -&gt; Collection&lt;<font color="#836B00">SelectExpressionItem</font>&gt;
           │  └─<font color="#739FCF"><b>selectItems</b></font>: <font color="#836B00">net.sf.jsqlparser.statement.select.SelectExpressionItem</font>
           │     └─<font color="#739FCF"><b>LongValue</b></font>: <font color="#836B00">1</font>
           ├─<font color="#739FCF"><b>Table</b></font>: <font color="#836B00">dual</font>
           └─<font color="#739FCF"><b>where</b></font>: <font color="#836B00">net.sf.jsqlparser.expression.operators.relational.EqualsTo</font>
              ├─<font color="#739FCF"><b>Column</b></font>: <font color="#836B00">a</font>
              └─<font color="#739FCF"><b>Column</b></font>: <font color="#836B00">b</font>
   </pre>
   </div>


Use the Visitor Patterns
==============================

Traverse the Java Object Tree using the Visitor Patterns:

.. code-block:: java

	// Define an Expression Visitor reacting on any Expression
	// Overwrite the visit() methods for each Expression Class
	ExpressionVisitorAdapter expressionVisitorAdapter = new ExpressionVisitorAdapter() {
		public void visit(EqualsTo equalsTo) {
			equalsTo.getLeftExpression().accept(this);
			equalsTo.getRightExpression().accept(this);
		}
		public void visit(Column column) {
			System.out.println("Found a Column " + column.getColumnName());
		}
	};

	// Define a Select Visitor reacting on a Plain Select invoking the Expression Visitor on the Where Clause
	SelectVisitorAdapter selectVisitorAdapter = new SelectVisitorAdapter() {
		@Override
		public void visit(PlainSelect plainSelect) {
			plainSelect.getWhere().accept(expressionVisitorAdapter);
		}
	};

	// Define a Statement Visitor for dispatching the Statements
	StatementVisitorAdapter statementVisitor = new StatementVisitorAdapter() {
		public void visit(Select select) {
			select.getSelectBody().accept(selectVisitorAdapter);
		}
	};

	String sqlStr="select 1 from dual where a=b";
	Statement stmt = CCJSqlParserUtil.parse(sqlStr);

	// Invoke the Statement Visitor
	stmt.accept(statementVisitor);


Define the Parser Features
==============================

JSQLParser interprets Squared Brackets ``[..]`` as Arrays, which does not work with MS SQL Server and T-SQL. Please use the Parser Features to instruct JSQLParser to read Squared Brackets as Quotes instead.

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