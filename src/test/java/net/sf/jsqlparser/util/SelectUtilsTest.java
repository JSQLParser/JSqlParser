package net.sf.jsqlparser.util;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author toben
 */
public class SelectUtilsTest {

	public SelectUtilsTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	/**
	 * Test of addColumn method, of class SelectUtils.
	 */
	@Test
	public void testAddExpr() throws JSQLParserException {
		Select select = (Select) CCJSqlParserUtil.parse("select a from mytable");
		SelectUtils.addExpression(select, new Column("b"));
		assertEquals("SELECT a, b FROM mytable", select.toString());
	}
}
