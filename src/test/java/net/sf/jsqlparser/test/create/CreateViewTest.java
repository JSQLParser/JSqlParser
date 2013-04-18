package net.sf.jsqlparser.test.create;

import java.io.StringReader;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

import junit.framework.TestCase;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

public class CreateViewTest extends TestCase {

	CCJSqlParserManager parserManager = new CCJSqlParserManager();

	public CreateViewTest(String arg0) {
		super(arg0);
	}

	public void testCreateView() throws JSQLParserException {
		String statement = "CREATE VIEW myview AS SELECT * FROM mytab";
		CreateView createView = (CreateView) parserManager.parse(new StringReader(statement));
		assertFalse(createView.isOrReplace());
		assertEquals("myview", createView.getView().getName());
		assertEquals("mytab", ((Table) ((PlainSelect) createView.getSelectBody()).getFromItem()).getName());
		assertEquals(statement, createView.toString());
	}

	public void testCreateView2() throws JSQLParserException {
		String stmt = "CREATE VIEW myview AS SELECT * FROM mytab";
		assertSqlCanBeParsedAndDeparsed(stmt);
	}

	public void testCreateView3() throws JSQLParserException {
		String stmt = "CREATE OR REPLACE VIEW myview AS SELECT * FROM mytab";
		assertSqlCanBeParsedAndDeparsed(stmt);
	}

	public void testCreateView4() throws JSQLParserException {
		String stmt = "CREATE OR REPLACE VIEW view2 AS SELECT a, b, c FROM testtab INNER JOIN testtab2 ON testtab.col1 = testtab2.col2";
		assertSqlCanBeParsedAndDeparsed(stmt);
	}
	
	public void testCreateViewWithColumnNames1() throws JSQLParserException {
		String stmt = "CREATE OR REPLACE VIEW view1(col1, col2) AS SELECT a, b FROM testtab";
		assertSqlCanBeParsedAndDeparsed(stmt);
	}
	
	public void testCreateView5() throws JSQLParserException {
		String statement = "CREATE VIEW myview AS (SELECT * FROM mytab)";
		String statement2 = "CREATE VIEW myview AS SELECT * FROM mytab";
		CreateView createView = (CreateView) parserManager.parse(new StringReader(statement));
		assertFalse(createView.isOrReplace());
		assertEquals("myview", createView.getView().getName());
		assertEquals("mytab", ((Table) ((PlainSelect) createView.getSelectBody()).getFromItem()).getName());
		assertEquals(statement2, createView.toString());
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
