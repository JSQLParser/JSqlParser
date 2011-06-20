package net.sf.jsqlparser.test.drop;

import java.io.StringReader;

import junit.framework.TestCase;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.drop.Drop;

public class DropTest extends TestCase {
	CCJSqlParserManager parserManager = new CCJSqlParserManager();

	public DropTest(String arg0) {
		super(arg0);
	}

	public void testDrop() throws JSQLParserException {
		String statement =
			"DROP TABLE mytab";
		Drop drop = (Drop) parserManager.parse(new StringReader(statement));
		assertEquals("TABLE", drop.getType());
		assertEquals("mytab", drop.getName());
		assertEquals(statement, ""+drop);
		
		statement =
					"DROP INDEX myindex CASCADE";
		drop = (Drop) parserManager.parse(new StringReader(statement));
		assertEquals("INDEX", drop.getType());
		assertEquals("myindex", drop.getName());
		assertEquals("CASCADE", drop.getParameters().get(0));
		assertEquals(statement, ""+drop);
	}

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(DropTest.class);
	}

}
