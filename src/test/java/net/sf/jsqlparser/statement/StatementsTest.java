package net.sf.jsqlparser.statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.parser.StringProvider;
import net.sf.jsqlparser.statement.select.Select;

/**
 *
 * @author toben
 */
public class StatementsTest {

    public StatementsTest() {}

    @BeforeClass
    public static void setUpClass() {}

    @AfterClass
    public static void tearDownClass() {}

    @Before
    public void setUp() {}

    @After
    public void tearDown() {}

    @Test
    public void testStatements() throws JSQLParserException {
        String sqls = "select * from mytable; select * from mytable2;";
        Statements parseStatements = CCJSqlParserUtil.parseStatements(sqls);

        assertEquals("SELECT * FROM mytable;\nSELECT * FROM mytable2;\n", parseStatements.toString());

        assertTrue(parseStatements.getStatements().get(0) instanceof Select);
        assertTrue(parseStatements.getStatements().get(1) instanceof Select);
    }

    @Test
    public void testStatementsProblem() throws JSQLParserException {
        String sqls = ";;select * from mytable;;select * from mytable2;;;";
        Statements parseStatements = CCJSqlParserUtil.parseStatements(sqls);

        assertEquals("SELECT * FROM mytable;\nSELECT * FROM mytable2;\n", parseStatements.toString());

        assertTrue(parseStatements.getStatements().get(0) instanceof Select);
        assertTrue(parseStatements.getStatements().get(1) instanceof Select);
    }

    @Test
    public void testStatementsErrorRecovery() throws JSQLParserException, ParseException {
        String sqls = "select * from mytable; select * from;";
        CCJSqlParser parser = new CCJSqlParser(new StringProvider(sqls));
        parser.setErrorRecovery(true);
        Statements parseStatements = parser.Statements();

        assertEquals(2, parseStatements.getStatements().size());

        assertTrue(parseStatements.getStatements().get(0) instanceof Select);
        assertNull(parseStatements.getStatements().get(1));
    }

    @Test
    public void testStatementsErrorRecovery2() throws JSQLParserException, ParseException {
        String sqls = "select * from1 table;";
        CCJSqlParser parser = new CCJSqlParser(new StringProvider(sqls));
        parser.setErrorRecovery(true);
        Statements parseStatements = parser.Statements();

        assertEquals(1, parseStatements.getStatements().size());

        assertTrue(parseStatements.getStatements().get(0) instanceof Select);
        assertEquals(1, parser.getParseErrors().size());
    }

    @Test
    public void testStatementsErrorRecovery3() throws JSQLParserException, ParseException {
        String sqls = "select * from mytable; select * from;select * from mytable2";
        CCJSqlParser parser = new CCJSqlParser(new StringProvider(sqls));
        parser.setErrorRecovery(true);
        Statements parseStatements = parser.Statements();

        assertEquals(2, parseStatements.getStatements().size());

        assertTrue(parseStatements.getStatements().get(0) instanceof Select);
        assertNull(parseStatements.getStatements().get(1));

        assertEquals(2, parser.getParseErrors().size());
    }
}
