package net.sf.jsqlparser.util;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.Select;

/**
 *
 * @author tw
 */
public class ConnectExpressionsVisitorTest {

    @BeforeClass
    public static void setUpClass() {}

    @AfterClass
    public static void tearDownClass() {}

    @Before
    public void setUp() {}

    @After
    public void tearDown() {}

    private CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @Test
    public void testVisit_PlainSelect_concat() throws JSQLParserException {
        String sql = "select a,b,c from test";
        Select select = (Select) parserManager.parse(new StringReader(sql));
        ConnectExpressionsVisitor instance = new ConnectExpressionsVisitor() {

            @Override
            protected BinaryExpression createBinaryExpression() {
                return new Concat();
            }
        };
        select.getSelectBody().accept(instance);

        assertEquals("SELECT a || b || c AS expr FROM test", select.toString());
    }

    @Test
    public void testVisit_PlainSelect_addition() throws JSQLParserException {
        String sql = "select a,b,c from test";
        Select select = (Select) parserManager.parse(new StringReader(sql));
        ConnectExpressionsVisitor instance = new ConnectExpressionsVisitor("testexpr") {

            @Override
            protected BinaryExpression createBinaryExpression() {
                return new Addition();
            }
        };
        select.getSelectBody().accept(instance);

        assertEquals("SELECT a + b + c AS testexpr FROM test", select.toString());
    }
}
