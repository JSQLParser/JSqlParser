package net.sf.jsqlparser.test.delete;

import java.io.StringReader;
import static junit.framework.Assert.assertEquals;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.delete.Delete;
import org.junit.Test;

import static net.sf.jsqlparser.test.TestUtils.*;

public class DeleteTest {

	CCJSqlParserManager parserManager = new CCJSqlParserManager();

	@Test
	public void testDelete() throws JSQLParserException {
		String statement = "DELETE FROM mytable WHERE mytable.col = 9";

		Delete delete = (Delete) parserManager.parse(new StringReader(statement));
		assertEquals("mytable", delete.getTable().getName());
		assertEquals(statement, "" + delete);
	}
	
	@Test
	public void testDeleteWhereProblem1() throws JSQLParserException {
		String stmt = "DELETE FROM tablename WHERE a = 1 AND b = 1";
		assertSqlCanBeParsedAndDeparsed(stmt);
	}

	@Test
	public void testDeleteWithLimit() throws JSQLParserException {
		String stmt = "DELETE FROM tablename WHERE a = 1 AND b = 1 LIMIT 5";
		assertSqlCanBeParsedAndDeparsed(stmt);
	}

	@Test(expected = JSQLParserException.class)
	public void testDeleteDoesNotAllowLimitOffset() throws JSQLParserException {
		String statement = "DELETE FROM table1 WHERE A.cod_table = 'YYY' LIMIT 3,4";
		parserManager.parse(new StringReader(statement));
	}

	@Test
	public void testDeleteWithOrderBy() throws JSQLParserException {
		String stmt = "DELETE FROM tablename WHERE a = 1 AND b = 1 ORDER BY col";
		assertSqlCanBeParsedAndDeparsed(stmt);
	}

	@Test
	public void testDeleteWithOrderByAndLimit() throws JSQLParserException {
		String stmt = "DELETE FROM tablename WHERE a = 1 AND b = 1 ORDER BY col LIMIT 10";
		assertSqlCanBeParsedAndDeparsed(stmt);
	}

}
