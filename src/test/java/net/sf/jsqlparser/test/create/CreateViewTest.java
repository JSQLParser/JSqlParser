package net.sf.jsqlparser.test.create;

import java.io.StringReader;

import junit.framework.TestCase;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.select.PlainSelect;

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
		assertEquals("mytab", ((Table)((PlainSelect)createView.getSelectBody()).getFromItem()).getName());
		assertEquals(statement, createView.toString());
	}

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(CreateViewTest.class);
	}

}
