package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
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
public class StatementsTest {
    
    public StatementsTest() {
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
     * Test of toString method, of class Statements.
     */
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
    
}
