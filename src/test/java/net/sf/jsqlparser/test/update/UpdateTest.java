package net.sf.jsqlparser.test.update;

import java.io.StringReader;
import static junit.framework.Assert.assertEquals;

import junit.framework.TestCase;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.update.Update;
import static net.sf.jsqlparser.test.TestUtils.*;

public class UpdateTest extends TestCase {

	CCJSqlParserManager parserManager = new CCJSqlParserManager();

	public UpdateTest(String arg0) {
		super(arg0);
	}

	public void testUpdate() throws JSQLParserException {
		String statement = "UPDATE mytable set col1='as', col2=?, col3=565 Where o >= 3";
		Update update = (Update) parserManager.parse(new StringReader(statement));
		assertEquals("mytable", update.getTable().getName());
		assertEquals(3, update.getColumns().size());
		assertEquals("col1", ((Column) update.getColumns().get(0)).getColumnName());
		assertEquals("col2", ((Column) update.getColumns().get(1)).getColumnName());
		assertEquals("col3", ((Column) update.getColumns().get(2)).getColumnName());
		assertEquals("as", ((StringValue) update.getExpressions().get(0)).getValue());
		assertTrue(update.getExpressions().get(1) instanceof JdbcParameter);
		assertEquals(565, ((LongValue) update.getExpressions().get(2)).getValue());

		assertTrue(update.getWhere() instanceof GreaterThanEquals);
	}

	public void testUpdateWAlias() throws JSQLParserException {
		String statement = "UPDATE table1 A SET A.columna = 'XXX' WHERE A.cod_table = 'YYY'";
		Update update = (Update) parserManager.parse(new StringReader(statement));
	}
	
	public void testUpdateWithDeparser() throws JSQLParserException {
		assertSqlCanBeParsedAndDeparsed("UPDATE table1 AS A SET A.columna = 'XXX' WHERE A.cod_table = 'YYY'");
	}
	
	public void testUpdateWithFrom() throws JSQLParserException {
		assertSqlCanBeParsedAndDeparsed("UPDATE table1 SET columna = 5 FROM table1 LEFT JOIN table2 ON col1 = col2");
	}
}
