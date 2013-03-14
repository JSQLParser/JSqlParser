package net.sf.jsqlparser.test.insert;

import java.io.StringReader;
import static junit.framework.Assert.assertEquals;

import junit.framework.TestCase;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

public class InsertTest extends TestCase {

	CCJSqlParserManager parserManager = new CCJSqlParserManager();

	public InsertTest(String arg0) {
		super(arg0);
	}

	public void testRegularInsert() throws JSQLParserException {
		String statement = "INSERT INTO mytable (col1, col2, col3) VALUES (?, 'sadfsd', 234)";
		Insert insert = (Insert) parserManager.parse(new StringReader(statement));
		assertEquals("mytable", insert.getTable().getName());
		assertEquals(3, insert.getColumns().size());
		assertEquals("col1", ((Column) insert.getColumns().get(0)).getColumnName());
		assertEquals("col2", ((Column) insert.getColumns().get(1)).getColumnName());
		assertEquals("col3", ((Column) insert.getColumns().get(2)).getColumnName());
		assertEquals(3, ((ExpressionList) insert.getItemsList()).getExpressions().size());
		assertTrue(((ExpressionList) insert.getItemsList()).getExpressions().get(0) instanceof JdbcParameter);
		assertEquals("sadfsd",
				((StringValue) ((ExpressionList) insert.getItemsList()).getExpressions().get(1)).getValue());
		assertEquals(234, ((LongValue) ((ExpressionList) insert.getItemsList()).getExpressions().get(2)).getValue());
		assertEquals(statement, "" + insert);

		statement = "INSERT INTO myschema.mytable VALUES (?, ?, 2.3)";
		insert = (Insert) parserManager.parse(new StringReader(statement));
		assertEquals("myschema.mytable", insert.getTable().getWholeTableName());
		assertEquals(3, ((ExpressionList) insert.getItemsList()).getExpressions().size());
		assertTrue(((ExpressionList) insert.getItemsList()).getExpressions().get(0) instanceof JdbcParameter);
		assertEquals(2.3, ((DoubleValue) ((ExpressionList) insert.getItemsList()).getExpressions().get(2)).getValue(),
				0.0);
		assertEquals(statement, "" + insert);

	}

	public void testInsertWithKeywordValue() throws JSQLParserException {
		String statement = "INSERT INTO mytable (col1) VALUE ('val1')";
		Insert insert = (Insert) parserManager.parse(new StringReader(statement));
		assertEquals("mytable", insert.getTable().getName());
		assertEquals(1, insert.getColumns().size());
		assertEquals("col1", ((Column) insert.getColumns().get(0)).getColumnName());
		assertEquals("val1",
				((StringValue) ((ExpressionList) insert.getItemsList()).getExpressions().get(0)).getValue());
		assertEquals("INSERT INTO mytable (col1) VALUES ('val1')", insert.toString());
	}

	public void testInsertFromSelect() throws JSQLParserException {
		String statement = "INSERT INTO mytable (col1, col2, col3) SELECT * FROM mytable2";
		Insert insert = (Insert) parserManager.parse(new StringReader(statement));
		assertEquals("mytable", insert.getTable().getName());
		assertEquals(3, insert.getColumns().size());
		assertEquals("col1", ((Column) insert.getColumns().get(0)).getColumnName());
		assertEquals("col2", ((Column) insert.getColumns().get(1)).getColumnName());
		assertEquals("col3", ((Column) insert.getColumns().get(2)).getColumnName());
		assertTrue(insert.getItemsList() instanceof SubSelect);
		assertEquals("mytable2",
				((Table) ((PlainSelect) ((SubSelect) insert.getItemsList()).getSelectBody()).getFromItem()).getName());

		// toString uses brakets
		String statementToString = "INSERT INTO mytable (col1, col2, col3) (SELECT * FROM mytable2)";
		assertEquals(statementToString, "" + insert);
	}

	public void testInsertMultiRowValue() throws JSQLParserException {
		assertSqlCanBeParsedAndDeparsed("INSERT INTO mytable (col1, col2) VALUES (a, b), (d, e)");
	}

	public void testInsertMultiRowValueDifferent() throws JSQLParserException {
		try {
			assertSqlCanBeParsedAndDeparsed("INSERT INTO mytable (col1, col2) VALUES (a, b), (d, e, c)");
		} catch (Exception e) {
			return;
		}

		fail("should not work");
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
}
