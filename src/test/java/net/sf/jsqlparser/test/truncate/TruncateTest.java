package net.sf.jsqlparser.test.truncate;

import java.io.StringReader;

import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.truncate.Truncate;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TruncateTest  {

	private CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @Test
	public void testTruncate() throws Exception {
		String statement = "TRUncATE TABLE myschema.mytab";
		Truncate truncate = (Truncate) parserManager.parse(new StringReader(statement));
		assertEquals("myschema", truncate.getTable().getSchemaName());
		assertEquals("myschema.mytab", truncate.getTable().getFullyQualifiedName());
		assertEquals(statement.toUpperCase(), truncate.toString().toUpperCase());

		statement = "TRUncATE   TABLE    mytab";
		String toStringStatement = "TRUncATE TABLE mytab";
		truncate = (Truncate) parserManager.parse(new StringReader(statement));
		assertEquals("mytab", truncate.getTable().getName());
		assertEquals(toStringStatement.toUpperCase(), truncate.toString().toUpperCase());
	}
}
