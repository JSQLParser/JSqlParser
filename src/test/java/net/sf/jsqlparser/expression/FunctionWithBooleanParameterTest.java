/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Test some cases linked to a boolean (condition) argument as function parameter.
 *
 * @author Denis Fulachier
 *
 */
public class FunctionWithBooleanParameterTest {

    public FunctionWithBooleanParameterTest() {
    }

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
