package net.sf.jsqlparser.parser;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.sf.jsqlparser.expression.Expression;

/**
 * Test some cases linked to a boolean (condition) argument as function parameter.
 * 
 * @author Denis Fulachier
 *
 */
public class FunctionWithBooleanParamTest {

    public FunctionWithBooleanParamTest() {}

    @Test
    public void testParseOpLowerTotally() throws Exception {
        Expression result = CCJSqlParserUtil.parseExpression("if(a<b, c, d)");
        assertEquals("if(a < b, c, d)", result.toString());
    }

    @Test
    public void testParseOpLowerOrEqual() throws Exception {
        Expression result = CCJSqlParserUtil.parseExpression("if(a+x<=b+y, c, d)");
        assertEquals("if(a + x <= b + y, c, d)", result.toString());
    }
    
    @Test
    public void testParseOpGreaterTotally() throws Exception {
        Expression result = CCJSqlParserUtil.parseExpression("if(a>b, c, d)");
        assertEquals("if(a > b, c, d)", result.toString());
    }
    
    @Test
    public void testParseOpGreaterOrEqual() throws Exception {
        Expression result = CCJSqlParserUtil.parseExpression("if(a>=b, c, d)");
        assertEquals("if(a >= b, c, d)", result.toString());
    }
    
    @Test
    public void testParseOpEqual() throws Exception {
        Expression result = CCJSqlParserUtil.parseExpression("if(a=b, c, d)");
        assertEquals("if(a = b, c, d)", result.toString());
    }

    @Test
    public void testParseOpNotEqualStandard() throws Exception {
        Expression result = CCJSqlParserUtil.parseExpression("if(a<>b, c, d)");
        assertEquals("if(a <> b, c, d)", result.toString());
    }

    @Test
    public void testParseOpNotEqualBang() throws Exception {
        Expression result = CCJSqlParserUtil.parseExpression("if(a!=b, c, d)");
        assertEquals("if(a != b, c, d)", result.toString());
    }
}
