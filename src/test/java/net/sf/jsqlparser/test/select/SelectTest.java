package net.sf.jsqlparser.test.select;

import java.io.StringReader;

import junit.framework.TestCase;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.Union;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

public class SelectTest extends TestCase {
	CCJSqlParserManager parserManager = new CCJSqlParserManager();

	public SelectTest(String arg0) {
		super(arg0);
	}

	public void testLimit() throws JSQLParserException {
		String statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT 3, ?";

		Select select = (Select) parserManager.parse(new StringReader(statement));

		assertEquals(3, ((PlainSelect) select.getSelectBody()).getLimit().getOffset());
		assertTrue(((PlainSelect) select.getSelectBody()).getLimit().isRowCountJdbcParameter());
		assertFalse(((PlainSelect) select.getSelectBody()).getLimit().isOffsetJdbcParameter());
		assertFalse(((PlainSelect) select.getSelectBody()).getLimit().isLimitAll());

		// toString uses standard syntax
		statement = "SELECT * FROM mytable WHERE mytable.col = 9 LIMIT ? OFFSET 3";
		assertSqlCanBeParsedAndDeparsed(statement);

		statement = "SELECT * FROM mytable WHERE mytable.col = 9 OFFSET ?";
		select = (Select) parserManager.parse(new StringReader(statement));

		assertEquals(0, ((PlainSelect) select.getSelectBody()).getLimit().getRowCount());
		assertTrue(((PlainSelect) select.getSelectBody()).getLimit().isOffsetJdbcParameter());
		assertFalse(((PlainSelect) select.getSelectBody()).getLimit().isLimitAll());
		assertStatementCanBeDeparsedAs(select, statement);

		statement = "(SELECT * FROM mytable WHERE mytable.col = 9 OFFSET ?) UNION "
				+ "(SELECT * FROM mytable2 WHERE mytable2.col = 9 OFFSET ?) LIMIT 3, 4";
		select = (Select) parserManager.parse(new StringReader(statement));
		Union union = (Union) select.getSelectBody();
		assertEquals(3, union.getLimit().getOffset());
		assertEquals(4, union.getLimit().getRowCount());

		// toString uses standard syntax
		statement = "(SELECT * FROM mytable WHERE mytable.col = 9 OFFSET ?) UNION "
				+ "(SELECT * FROM mytable2 WHERE mytable2.col = 9 OFFSET ?) LIMIT 4 OFFSET 3";
		assertSqlCanBeParsedAndDeparsed(statement);

		statement = "(SELECT * FROM mytable WHERE mytable.col = 9 OFFSET ?) UNION ALL "
				+ "(SELECT * FROM mytable2 WHERE mytable2.col = 9 OFFSET ?) UNION ALL "
				+ "(SELECT * FROM mytable3 WHERE mytable4.col = 9 OFFSET ?) LIMIT 4 OFFSET 3";
		assertSqlCanBeParsedAndDeparsed(statement);

	}

	public void testTop() throws JSQLParserException {
		String statement = "SELECT TOP 3 * FROM mytable WHERE mytable.col = 9";

		Select select = (Select) parserManager.parse(new StringReader(statement));

		assertEquals(3, ((PlainSelect) select.getSelectBody()).getTop().getRowCount());
		assertStatementCanBeDeparsedAs(select, statement);

		statement = "select top 5 foo from bar";
		select = (Select) parserManager.parse(new StringReader(statement));
		assertEquals(5, ((PlainSelect) select.getSelectBody()).getTop().getRowCount());

	}

	public void testSelectItems() throws JSQLParserException {
		String statement = "SELECT myid AS MYID, mycol, tab.*, schema.tab.*, mytab.mycol2, myschema.mytab.mycol, myschema.mytab.* FROM mytable WHERE mytable.col = 9";
		Select select = (Select) parserManager.parse(new StringReader(statement));
		PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

		assertEquals("MYID", ((SelectExpressionItem) plainSelect.getSelectItems().get(0)).getAlias());
		assertEquals("mycol",
				((Column) ((SelectExpressionItem) plainSelect.getSelectItems().get(1)).getExpression()).getColumnName());
		assertEquals("tab", ((AllTableColumns) plainSelect.getSelectItems().get(2)).getTable().getName());
		assertEquals("schema", ((AllTableColumns) plainSelect.getSelectItems().get(3)).getTable().getSchemaName());
		assertEquals("schema.tab", ((AllTableColumns) plainSelect.getSelectItems().get(3)).getTable()
				.getWholeTableName());
		assertEquals("mytab.mycol2",
				((Column) ((SelectExpressionItem) plainSelect.getSelectItems().get(4)).getExpression())
						.getWholeColumnName());
		assertEquals("myschema.mytab.mycol",
				((Column) ((SelectExpressionItem) plainSelect.getSelectItems().get(5)).getExpression())
						.getWholeColumnName());
		assertEquals("myschema.mytab", ((AllTableColumns) plainSelect.getSelectItems().get(6)).getTable()
				.getWholeTableName());
		assertStatementCanBeDeparsedAs(select, statement);

		statement = "SELECT myid AS MYID, (SELECT MAX(ID) AS myid2 FROM mytable2) AS myalias FROM mytable WHERE mytable.col = 9";
		select = (Select) parserManager.parse(new StringReader(statement));
		plainSelect = (PlainSelect) select.getSelectBody();
		assertEquals("myalias", ((SelectExpressionItem) plainSelect.getSelectItems().get(1)).getAlias());
		assertStatementCanBeDeparsedAs(select, statement);

		statement = "SELECT (myid + myid2) AS MYID FROM mytable WHERE mytable.col = 9";
		select = (Select) parserManager.parse(new StringReader(statement));
		plainSelect = (PlainSelect) select.getSelectBody();
		assertEquals("MYID", ((SelectExpressionItem) plainSelect.getSelectItems().get(0)).getAlias());
		assertStatementCanBeDeparsedAs(select, statement);
	}

	public void testUnion() throws JSQLParserException {
		String statement = "SELECT * FROM mytable WHERE mytable.col = 9 UNION "
				+ "SELECT * FROM mytable3 WHERE mytable3.col = ? UNION " + "SELECT * FROM mytable2 LIMIT 3,4";

		Select select = (Select) parserManager.parse(new StringReader(statement));
		Union union = (Union) select.getSelectBody();
		assertEquals(3, union.getPlainSelects().size());
		assertEquals("mytable", ((Table) ((PlainSelect) union.getPlainSelects().get(0)).getFromItem()).getName());
		assertEquals("mytable3", ((Table) ((PlainSelect) union.getPlainSelects().get(1)).getFromItem()).getName());
		assertEquals("mytable2", ((Table) ((PlainSelect) union.getPlainSelects().get(2)).getFromItem()).getName());
		assertEquals(3, ((PlainSelect) union.getPlainSelects().get(2)).getLimit().getOffset());

		// use brakets for toString
		// use standard limit syntax
		String statementToString = "(SELECT * FROM mytable WHERE mytable.col = 9) UNION "
				+ "(SELECT * FROM mytable3 WHERE mytable3.col = ?) UNION "
				+ "(SELECT * FROM mytable2 LIMIT 4 OFFSET 3)";
		assertStatementCanBeDeparsedAs(select, statementToString);
	}

	public void testDistinct() throws JSQLParserException {
		String statement = "SELECT DISTINCT ON (myid) myid, mycol FROM mytable WHERE mytable.col = 9";
		Select select = (Select) parserManager.parse(new StringReader(statement));
		PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
		assertEquals("myid",
				((Column) ((SelectExpressionItem) plainSelect.getDistinct().getOnSelectItems().get(0)).getExpression())
						.getColumnName());
		assertEquals("mycol",
				((Column) ((SelectExpressionItem) plainSelect.getSelectItems().get(1)).getExpression()).getColumnName());
		assertStatementCanBeDeparsedAs(select, statement);
	}

	public void testFrom() throws JSQLParserException {
		String statement = "SELECT * FROM mytable as mytable0, mytable1 alias_tab1, mytable2 as alias_tab2, (SELECT * FROM mytable3) AS mytable4 WHERE mytable.col = 9";
		String statementToString = "SELECT * FROM mytable AS mytable0, mytable1 AS alias_tab1, mytable2 AS alias_tab2, (SELECT * FROM mytable3) AS mytable4 WHERE mytable.col = 9";

		Select select = (Select) parserManager.parse(new StringReader(statement));
		PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
		assertEquals(3, plainSelect.getJoins().size());
		assertEquals("mytable0", ((Table) plainSelect.getFromItem()).getAlias());
		assertEquals("alias_tab1", ((Join) plainSelect.getJoins().get(0)).getRightItem().getAlias());
		assertEquals("alias_tab2", ((Join) plainSelect.getJoins().get(1)).getRightItem().getAlias());
		assertEquals("mytable4", ((Join) plainSelect.getJoins().get(2)).getRightItem().getAlias());
		assertStatementCanBeDeparsedAs(select, statementToString);
	}

	public void testJoin() throws JSQLParserException {
		String statement = "SELECT * FROM tab1 LEFT OUTER JOIN tab2 ON tab1.id = tab2.id";
		Select select = (Select) parserManager.parse(new StringReader(statement));
		PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
		assertEquals(1, plainSelect.getJoins().size());
		assertEquals("tab2", ((Table) ((Join) plainSelect.getJoins().get(0)).getRightItem()).getWholeTableName());
		assertEquals("tab1.id",
				((Column) ((EqualsTo) ((Join) plainSelect.getJoins().get(0)).getOnExpression()).getLeftExpression())
						.getWholeColumnName());
		assertTrue(((Join) plainSelect.getJoins().get(0)).isOuter());
		assertStatementCanBeDeparsedAs(select, statement);

		statement = "SELECT * FROM tab1 LEFT OUTER JOIN tab2 ON tab1.id = tab2.id INNER JOIN tab3";
		select = (Select) parserManager.parse(new StringReader(statement));
		plainSelect = (PlainSelect) select.getSelectBody();
		assertEquals(2, plainSelect.getJoins().size());
		assertEquals("tab3", ((Table) ((Join) plainSelect.getJoins().get(1)).getRightItem()).getWholeTableName());
		assertFalse(((Join) plainSelect.getJoins().get(1)).isOuter());
		assertStatementCanBeDeparsedAs(select, statement);

		statement = "SELECT * FROM tab1 LEFT OUTER JOIN tab2 ON tab1.id = tab2.id JOIN tab3";
		select = (Select) parserManager.parse(new StringReader(statement));
		plainSelect = (PlainSelect) select.getSelectBody();
		assertEquals(2, plainSelect.getJoins().size());
		assertEquals("tab3", ((Table) ((Join) plainSelect.getJoins().get(1)).getRightItem()).getWholeTableName());
		assertFalse(((Join) plainSelect.getJoins().get(1)).isOuter());
		assertStatementCanBeDeparsedAs(select, statement);

		// implicit INNER
		statement = "SELECT * FROM tab1 LEFT OUTER JOIN tab2 ON tab1.id = tab2.id INNER JOIN tab3";
		select = (Select) parserManager.parse(new StringReader(statement));
		assertStatementCanBeDeparsedAs(select, statement);

		statement = "SELECT * FROM TA2 LEFT OUTER JOIN O USING (col1, col2) WHERE D.OasSD = 'asdf' AND (kj >= 4 OR l < 'sdf')";
		select = (Select) parserManager.parse(new StringReader(statement));
		assertStatementCanBeDeparsedAs(select, statement);

		statement = "SELECT * FROM tab1 INNER JOIN tab2 USING (id, id2)";
		select = (Select) parserManager.parse(new StringReader(statement));
		plainSelect = (PlainSelect) select.getSelectBody();
		assertEquals(1, plainSelect.getJoins().size());
		assertEquals("tab2", ((Table) ((Join) plainSelect.getJoins().get(0)).getRightItem()).getWholeTableName());
		assertFalse(((Join) plainSelect.getJoins().get(0)).isOuter());
		assertEquals(2, ((Join) plainSelect.getJoins().get(0)).getUsingColumns().size());
		assertEquals("id2",
				((Column) ((Join) plainSelect.getJoins().get(0)).getUsingColumns().get(1)).getWholeColumnName());
		assertStatementCanBeDeparsedAs(select, statement);

		statement = "SELECT * FROM tab1 RIGHT OUTER JOIN tab2 USING (id, id2)";
		assertSqlCanBeParsedAndDeparsed(statement);

		statement = "SELECT * FROM foo AS f LEFT INNER JOIN (bar AS b RIGHT OUTER JOIN baz AS z ON f.id = z.id) ON f.id = b.id";
		select = (Select) parserManager.parse(new StringReader(statement));
		assertStatementCanBeDeparsedAs(select, statement);

	}

	public void testFunctions() throws JSQLParserException {
		String statement = "SELECT MAX(id) AS max FROM mytable WHERE mytable.col = 9";
		Select select = (Select) parserManager.parse(new StringReader(statement));
		PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
		assertEquals("max", ((SelectExpressionItem) plainSelect.getSelectItems().get(0)).getAlias());
		assertStatementCanBeDeparsedAs(select, statement);

		statement = "SELECT MAX(id), AVG(pro) AS myavg FROM mytable WHERE mytable.col = 9 GROUP BY pro";
		select = (Select) parserManager.parse(new StringReader(statement));
		plainSelect = (PlainSelect) select.getSelectBody();
		assertEquals("myavg", ((SelectExpressionItem) plainSelect.getSelectItems().get(1)).getAlias());
		assertStatementCanBeDeparsedAs(select, statement);

		statement = "SELECT MAX(a, b, c), COUNT(*), D FROM tab1 GROUP BY D";
		select = (Select) parserManager.parse(new StringReader(statement));
		plainSelect = (PlainSelect) select.getSelectBody();
		Function fun = (Function) ((SelectExpressionItem) plainSelect.getSelectItems().get(0)).getExpression();
		assertEquals("MAX", fun.getName());
		assertEquals("b", ((Column) fun.getParameters().getExpressions().get(1)).getWholeColumnName());
		assertTrue(((Function) ((SelectExpressionItem) plainSelect.getSelectItems().get(1)).getExpression())
				.isAllColumns());
		assertStatementCanBeDeparsedAs(select, statement);

		statement = "SELECT {fn MAX(a, b, c)}, COUNT(*), D FROM tab1 GROUP BY D";
		select = (Select) parserManager.parse(new StringReader(statement));
		plainSelect = (PlainSelect) select.getSelectBody();
		fun = (Function) ((SelectExpressionItem) plainSelect.getSelectItems().get(0)).getExpression();
		assertTrue(fun.isEscaped());
		assertEquals("MAX", fun.getName());
		assertEquals("b", ((Column) fun.getParameters().getExpressions().get(1)).getWholeColumnName());
		assertTrue(((Function) ((SelectExpressionItem) plainSelect.getSelectItems().get(1)).getExpression())
				.isAllColumns());
		assertStatementCanBeDeparsedAs(select, statement);

		statement = "SELECT ab.MAX(a, b, c), cd.COUNT(*), D FROM tab1 GROUP BY D";
		select = (Select) parserManager.parse(new StringReader(statement));
		plainSelect = (PlainSelect) select.getSelectBody();
		fun = (Function) ((SelectExpressionItem) plainSelect.getSelectItems().get(0)).getExpression();
		assertEquals("ab.MAX", fun.getName());
		assertEquals("b", ((Column) fun.getParameters().getExpressions().get(1)).getWholeColumnName());
		fun = (Function) ((SelectExpressionItem) plainSelect.getSelectItems().get(1)).getExpression();
		assertEquals("cd.COUNT", fun.getName());
		assertTrue(fun.isAllColumns());
		assertStatementCanBeDeparsedAs(select, statement);
	}

	public void testWhere() throws JSQLParserException {

		final String statement = "SELECT * FROM tab1 WHERE";
		String whereToString = "(a + b + c / d + e * f) * (a / b * (a + b)) > ?";
		PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement + " "
				+ whereToString))).getSelectBody();
		assertTrue(plainSelect.getWhere() instanceof GreaterThan);
		assertTrue(((GreaterThan) plainSelect.getWhere()).getLeftExpression() instanceof Multiplication);
		assertEquals(statement + " " + whereToString, plainSelect.toString());

		assertExpressionCanBeDeparsedAs(plainSelect.getWhere(), whereToString);

		whereToString = "(7 * s + 9 / 3) NOT BETWEEN 3 AND ?";
		plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement + " " + whereToString)))
				.getSelectBody();

		assertExpressionCanBeDeparsedAs(plainSelect.getWhere(), whereToString);
		assertEquals(statement + " " + whereToString, plainSelect.toString());

		whereToString = "a / b NOT IN (?, 's''adf', 234.2)";
		plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement + " " + whereToString)))
				.getSelectBody();

		assertExpressionCanBeDeparsedAs(plainSelect.getWhere(), whereToString);
		assertEquals(statement + " " + whereToString, plainSelect.toString());

		whereToString = " NOT 0 = 0";
		plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement + whereToString)))
				.getSelectBody();

		whereToString = " NOT (0 = 0)";
		plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement + whereToString)))
				.getSelectBody();

		assertExpressionCanBeDeparsedAs(plainSelect.getWhere(), whereToString);
		assertEquals(statement + whereToString, plainSelect.toString());
	}

	public void testGroupBy() throws JSQLParserException {
		String statement = "SELECT * FROM tab1 WHERE a > 34 GROUP BY tab1.b";
		Select select = (Select) parserManager.parse(new StringReader(statement));
		PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
		assertEquals(1, plainSelect.getGroupByColumnReferences().size());
		assertEquals("tab1.b", ((Column) plainSelect.getGroupByColumnReferences().get(0)).getWholeColumnName());
		assertStatementCanBeDeparsedAs(select, statement);

		statement = "SELECT * FROM tab1 WHERE a > 34 GROUP BY 2, 3";
		select = (Select) parserManager.parse(new StringReader(statement));
		plainSelect = (PlainSelect) select.getSelectBody();
		assertEquals(2, plainSelect.getGroupByColumnReferences().size());
		assertEquals(2, ((LongValue) plainSelect.getGroupByColumnReferences().get(0)).getValue());
		assertEquals(3, ((LongValue) plainSelect.getGroupByColumnReferences().get(1)).getValue());
		assertStatementCanBeDeparsedAs(select, statement);
	}

	public void testHaving() throws JSQLParserException {
		String statement = "SELECT MAX(tab1.b) FROM tab1 WHERE a > 34 GROUP BY tab1.b HAVING MAX(tab1.b) > 56";
		Select select = (Select) parserManager.parse(new StringReader(statement));
		PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
		assertTrue(plainSelect.getHaving() instanceof GreaterThan);
		assertStatementCanBeDeparsedAs(select, statement);

		statement = "SELECT MAX(tab1.b) FROM tab1 WHERE a > 34 HAVING MAX(tab1.b) IN (56, 32, 3, ?)";
		select = (Select) parserManager.parse(new StringReader(statement));
		plainSelect = (PlainSelect) select.getSelectBody();
		assertTrue(plainSelect.getHaving() instanceof InExpression);
		assertStatementCanBeDeparsedAs(select, statement);
	}

	public void testExists() throws JSQLParserException {
		String statement = "SELECT * FROM tab1 WHERE";
		String where = " EXISTS (SELECT * FROM tab2)";
		statement += where;
		Statement parsed = parserManager.parse(new StringReader(statement));

		assertEquals(statement, parsed.toString());

		PlainSelect plainSelect = (PlainSelect) ((Select) parsed).getSelectBody();
		assertExpressionCanBeDeparsedAs(plainSelect.getWhere(), where);
	}

	public void testOrderBy() throws JSQLParserException {
		// TODO: should there be a DESC marker in the OrderByElement class?
		String statement = "SELECT * FROM tab1 WHERE a > 34 GROUP BY tab1.b ORDER BY tab1.a DESC, tab1.b ASC";
		String statementToString = "SELECT * FROM tab1 WHERE a > 34 GROUP BY tab1.b ORDER BY tab1.a DESC, tab1.b";
		Select select = (Select) parserManager.parse(new StringReader(statement));
		PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
		assertEquals(2, plainSelect.getOrderByElements().size());
		assertEquals("tab1.a",
				((Column) ((OrderByElement) plainSelect.getOrderByElements().get(0)).getExpression())
						.getWholeColumnName());
		assertEquals("b",
				((Column) ((OrderByElement) plainSelect.getOrderByElements().get(1)).getExpression()).getColumnName());
		assertTrue(((OrderByElement) plainSelect.getOrderByElements().get(1)).isAsc());
		assertFalse(((OrderByElement) plainSelect.getOrderByElements().get(0)).isAsc());
		assertStatementCanBeDeparsedAs(select, statementToString);

		statement = "SELECT * FROM tab1 WHERE a > 34 GROUP BY tab1.b ORDER BY tab1.a, 2";
		select = (Select) parserManager.parse(new StringReader(statement));
		plainSelect = (PlainSelect) select.getSelectBody();
		assertEquals(2, plainSelect.getOrderByElements().size());
		assertEquals("a",
				((Column) ((OrderByElement) plainSelect.getOrderByElements().get(0)).getExpression()).getColumnName());
		assertEquals(2,
				((LongValue) ((OrderByElement) plainSelect.getOrderByElements().get(1)).getExpression()).getValue());
		assertStatementCanBeDeparsedAs(select, statement);

	}

	public void testTimestamp() throws JSQLParserException {
		String statement = "SELECT * FROM tab1 WHERE a > {ts '2004-04-30 04:05:34.56'}";
		Select select = (Select) parserManager.parse(new StringReader(statement));
		PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
		assertEquals("2004-04-30 04:05:34.56",
				((TimestampValue) ((GreaterThan) plainSelect.getWhere()).getRightExpression()).getValue().toString());
		assertStatementCanBeDeparsedAs(select, statement);
	}

	public void testTime() throws JSQLParserException {
		String statement = "SELECT * FROM tab1 WHERE a > {t '04:05:34'}";
		Select select = (Select) parserManager.parse(new StringReader(statement));
		PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
		assertEquals("04:05:34",
				(((TimeValue) ((GreaterThan) plainSelect.getWhere()).getRightExpression()).getValue()).toString());
		assertStatementCanBeDeparsedAs(select, statement);
	}

	public void testCase() throws JSQLParserException {
		String statement = "SELECT a, CASE b WHEN 1 THEN 2 END FROM tab1";
		assertSqlCanBeParsedAndDeparsed(statement);

		statement = "SELECT a, (CASE WHEN (a > 2) THEN 3 END) AS b FROM tab1";
		assertSqlCanBeParsedAndDeparsed(statement);

		statement = "SELECT a, (CASE WHEN a > 2 THEN 3 ELSE 4 END) AS b FROM tab1";
		assertSqlCanBeParsedAndDeparsed(statement);

		statement = "SELECT a, (CASE b WHEN 1 THEN 2 WHEN 3 THEN 4 ELSE 5 END) FROM tab1";
		assertSqlCanBeParsedAndDeparsed(statement);

		statement = "SELECT a, (CASE " + "WHEN b > 1 THEN 'BBB' " + "WHEN a = 3 THEN 'AAA' " + "END) FROM tab1";
		assertSqlCanBeParsedAndDeparsed(statement);

		statement = "SELECT a, (CASE " + "WHEN b > 1 THEN 'BBB' " + "WHEN a = 3 THEN 'AAA' " + "END) FROM tab1 "
				+ "WHERE c = (CASE " + "WHEN d <> 3 THEN 5 " + "ELSE 10 " + "END)";
		assertSqlCanBeParsedAndDeparsed(statement);

		statement = "SELECT a, CASE a " + "WHEN 'b' THEN 'BBB' " + "WHEN 'a' THEN 'AAA' " + "END AS b FROM tab1";
		assertSqlCanBeParsedAndDeparsed(statement);

		statement = "SELECT a FROM tab1 WHERE CASE b WHEN 1 THEN 2 WHEN 3 THEN 4 ELSE 5 END > 34";
		assertSqlCanBeParsedAndDeparsed(statement);

		statement = "SELECT a FROM tab1 WHERE CASE b WHEN 1 THEN 2 + 3 ELSE 4 END > 34";
		assertSqlCanBeParsedAndDeparsed(statement);

		statement = "SELECT a, (CASE " + "WHEN (CASE a WHEN 1 THEN 10 ELSE 20 END) > 15 THEN 'BBB' " +
		// "WHEN (SELECT c FROM tab2 WHERE d = 2) = 3 THEN 'AAA' " +
				"END) FROM tab1";
		assertSqlCanBeParsedAndDeparsed(statement);

	}

	public void testReplaceAsFunction() throws JSQLParserException {
		String statement = "SELECT REPLACE(a, 'b', c) FROM tab1";
		assertSqlCanBeParsedAndDeparsed(statement);
	}

	public void testLike() throws JSQLParserException {
		String statement = "SELECT * FROM tab1 WHERE a LIKE 'test'";
		Select select = (Select) parserManager.parse(new StringReader(statement));
		PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
		assertEquals("test",
				(((StringValue) ((LikeExpression) plainSelect.getWhere()).getRightExpression()).getValue()).toString());
		assertStatementCanBeDeparsedAs(select, statement);

		statement = "SELECT * FROM tab1 WHERE a LIKE 'test' ESCAPE 'test2'";
		select = (Select) parserManager.parse(new StringReader(statement));
		plainSelect = (PlainSelect) select.getSelectBody();
		assertEquals("test",
				(((StringValue) ((LikeExpression) plainSelect.getWhere()).getRightExpression()).getValue()).toString());
		assertEquals("test2", (((LikeExpression) plainSelect.getWhere()).getEscape()));
		assertStatementCanBeDeparsedAs(select, statement);
	}

	public void testSelectOrderHaving() throws JSQLParserException {
		String statement = "SELECT units, count(units) AS num FROM currency GROUP BY units HAVING count(units) > 1 ORDER BY num";
		assertSqlCanBeParsedAndDeparsed(statement);
	}

	public void testDouble() throws JSQLParserException {
		String statement = "SELECT 1e2, * FROM mytable WHERE mytable.col = 9";
		Select select = (Select) parserManager.parse(new StringReader(statement));

		assertEquals(1e2, ((DoubleValue) ((SelectExpressionItem) ((PlainSelect) select.getSelectBody())
				.getSelectItems().get(0)).getExpression()).getValue(), 0);
		assertStatementCanBeDeparsedAs(select, statement);

		statement = "SELECT * FROM mytable WHERE mytable.col = 1.e2";
		select = (Select) parserManager.parse(new StringReader(statement));

		assertEquals(1e2,
				((DoubleValue) ((BinaryExpression) ((PlainSelect) select.getSelectBody()).getWhere())
						.getRightExpression()).getValue(), 0);
		assertStatementCanBeDeparsedAs(select, statement);

		statement = "SELECT * FROM mytable WHERE mytable.col = 1.2e2";
		select = (Select) parserManager.parse(new StringReader(statement));

		assertEquals(1.2e2,
				((DoubleValue) ((BinaryExpression) ((PlainSelect) select.getSelectBody()).getWhere())
						.getRightExpression()).getValue(), 0);
		assertStatementCanBeDeparsedAs(select, statement);

		statement = "SELECT * FROM mytable WHERE mytable.col = 2e2";
		select = (Select) parserManager.parse(new StringReader(statement));

		assertEquals(2e2,
				((DoubleValue) ((BinaryExpression) ((PlainSelect) select.getSelectBody()).getWhere())
						.getRightExpression()).getValue(), 0);
		assertStatementCanBeDeparsedAs(select, statement);
	}

	public void testWith() throws JSQLParserException {
		String statement = "WITH DINFO (DEPTNO, AVGSALARY, EMPCOUNT) AS "
				+ "(SELECT OTHERS.WORKDEPT, AVG(OTHERS.SALARY), COUNT(*) FROM EMPLOYEE AS OTHERS "
				+ "GROUP BY OTHERS.WORKDEPT), DINFOMAX AS (SELECT MAX(AVGSALARY) AS AVGMAX FROM DINFO) "
				+ "SELECT THIS_EMP.EMPNO, THIS_EMP.SALARY, DINFO.AVGSALARY, DINFO.EMPCOUNT, DINFOMAX.AVGMAX "
				+ "FROM EMPLOYEE AS THIS_EMP INNER JOIN DINFO INNER JOIN DINFOMAX "
				+ "WHERE THIS_EMP.JOB = 'SALESREP' AND THIS_EMP.WORKDEPT = DINFO.DEPTNO";
		assertSqlCanBeParsedAndDeparsed(statement);
	}

	public void testSelectAliasInQuotes() throws JSQLParserException {
		String statement = "SELECT mycolumn AS \"My Column Name\" FROM mytable";
		assertSqlCanBeParsedAndDeparsed(statement);
	}

	public void testSelectJoinWithComma() throws JSQLParserException {
		String statement = "SELECT cb.Genus, cb.Species FROM Coleccion_de_Briofitas AS cb, unigeoestados AS es "
				+ "WHERE es.nombre = \"Tamaulipas\" AND cb.the_geom = es.geom";
		assertSqlCanBeParsedAndDeparsed(statement);
	}

	public void testDeparser() throws JSQLParserException {
		String statement = "SELECT a.OWNERLASTNAME, a.OWNERFIRSTNAME " + "FROM ANTIQUEOWNERS AS a, ANTIQUES AS b "
				+ "WHERE b.BUYERID = a.OWNERID AND b.ITEM = 'Chair'";
		assertSqlCanBeParsedAndDeparsed(statement);

		statement = "SELECT count(DISTINCT f + 4) FROM a";
		assertSqlCanBeParsedAndDeparsed(statement);

		statement = "SELECT count(DISTINCT f, g, h) FROM a";
		assertSqlCanBeParsedAndDeparsed(statement);
	}

	public void testMysqlQuote() throws JSQLParserException {
		String statement = "SELECT `a.OWNERLASTNAME`, `OWNERFIRSTNAME` " + "FROM `ANTIQUEOWNERS` AS a, ANTIQUES AS b "
				+ "WHERE b.BUYERID = a.OWNERID AND b.ITEM = 'Chair'";
		assertSqlCanBeParsedAndDeparsed(statement);
	}

	public void testConcat() throws JSQLParserException {
		String statement = "SELECT a || b || c + 4 FROM t";
		assertSqlCanBeParsedAndDeparsed(statement);
	}

	public void testMatches() throws JSQLParserException {
		String statement = "SELECT * FROM team WHERE team.search_column @@ to_tsquery('new & york & yankees')";
		assertSqlCanBeParsedAndDeparsed(statement);
	}

	public void testGroupByExpression() throws JSQLParserException {
		String statement = "SELECT col1, col2, col1 + col2, sum(col8)" + " FROM table1 "
				+ "GROUP BY col1, col2, col1 + col2";
		assertSqlCanBeParsedAndDeparsed(statement);
	}

	public void testBitwise() throws JSQLParserException {
		String statement = "SELECT col1 & 32, col2 ^ col1, col1 | col2" + " FROM table1";
		assertSqlCanBeParsedAndDeparsed(statement);
	}
	
	public void testSelectFunction() throws JSQLParserException {
		String statement = "SELECT 1 + 2 AS sum";
		assertSqlCanBeParsedAndDeparsed(statement);
	}
	
	public void testCast() throws JSQLParserException  {
		String stmt = "SELECT CAST(a AS varchar) FROM tabelle1";
		assertSqlCanBeParsedAndDeparsed(stmt);
		stmt = "SELECT CAST(a AS varchar2) FROM tabelle1";
		assertSqlCanBeParsedAndDeparsed(stmt);
	}
	
	public void testCastInCast() throws JSQLParserException {
		String stmt = "SELECT CAST(CAST(a AS numeric) AS varchar) FROM tabelle1";
		assertSqlCanBeParsedAndDeparsed(stmt);
	}
	

	public void testCastInCast2() throws JSQLParserException {
		String stmt = "SELECT CAST('test' + CAST(assertEqual AS numeric) AS varchar) FROM tabelle1";
		assertSqlCanBeParsedAndDeparsed(stmt);
	}
	
	public void testCaseElseAddition() throws JSQLParserException  {
		String stmt = "SELECT CASE WHEN 1 + 3 > 20 THEN 0 ELSE 1000 + 1 END AS d FROM dual";
		assertSqlCanBeParsedAndDeparsed(stmt);
	}

	private void assertSqlCanBeParsedAndDeparsed(String statement) throws JSQLParserException {
		Statement parsed = parserManager.parse(new StringReader(statement));
		assertStatementCanBeDeparsedAs(parsed, statement);
	}

	private void assertStatementCanBeDeparsedAs(Statement parsed, String statement) {
		assertEquals(statement, parsed.toString());

		StatementDeParser deParser = new StatementDeParser(new StringBuilder());
		parsed.accept(deParser);
		assertEquals(statement, deParser.getBuffer().toString());
	}

	private void assertExpressionCanBeDeparsedAs(final Expression parsed, String expression) {
		ExpressionDeParser expressionDeParser = new ExpressionDeParser();
		StringBuilder stringBuffer = new StringBuilder();
		expressionDeParser.setBuffer(stringBuffer);
		SelectDeParser selectDeParser = new SelectDeParser(expressionDeParser, stringBuffer);
		expressionDeParser.setSelectVisitor(selectDeParser);
		parsed.accept(expressionDeParser);

		assertEquals(expression, stringBuffer.toString());
	}
}
