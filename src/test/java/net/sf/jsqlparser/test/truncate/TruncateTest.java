package net.sf.jsqlparser.test.truncate;

import java.io.StringReader;

import junit.framework.TestCase;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.truncate.Truncate;

public class TruncateTest extends TestCase {
	private CCJSqlParserManager parserManager = new CCJSqlParserManager();

	public TruncateTest(String arg0) {
		super(arg0);
	}

	public void testTruncate() throws Exception {
		String statement = "TRUncATE TABLE myschema.mytab";
		Truncate truncate = (Truncate) parserManager.parse(new StringReader(statement));
		assertEquals("myschema", truncate.getTable().getSchemaName());
		assertEquals("myschema.mytab", truncate.getTable().getWholeTableName());
		assertEquals(statement.toUpperCase(), truncate.toString().toUpperCase());

		statement = "TRUncATE   TABLE    mytab";
		String toStringStatement = "TRUncATE TABLE mytab";
		truncate = (Truncate) parserManager.parse(new StringReader(statement));
		assertEquals("mytab", truncate.getTable().getName());
		assertEquals(toStringStatement.toUpperCase(), truncate.toString().toUpperCase());
	}
}
