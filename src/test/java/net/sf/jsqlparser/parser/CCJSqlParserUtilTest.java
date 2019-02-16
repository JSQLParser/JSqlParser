/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statements;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CCJSqlParserUtilTest {

    public CCJSqlParserUtilTest() {
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
     * Test of parseExpression method, of class CCJSqlParserUtil.
     */
    @Test
    public void testParseExpression() throws Exception {
        Expression result = CCJSqlParserUtil.parseExpression("a+b");
        assertEquals("a + b", result.toString());
        assertTrue(result instanceof Addition);
        Addition add = (Addition) result;
        assertTrue(add.getLeftExpression() instanceof Column);
        assertTrue(add.getRightExpression() instanceof Column);
    }

    @Test
    public void testParseExpression2() throws Exception {
        Expression result = CCJSqlParserUtil.parseExpression("2*(a+6.0)");
        assertEquals("2 * (a + 6.0)", result.toString());
        assertTrue(result instanceof Multiplication);
        Multiplication mult = (Multiplication) result;
        assertTrue(mult.getLeftExpression() instanceof LongValue);
        assertTrue(mult.getRightExpression() instanceof Parenthesis);
    }

    @Test(expected = JSQLParserException.class)
    public void testParseExpressionNonPartial() throws Exception {
        CCJSqlParserUtil.parseExpression("a+", false);

    }

    @Test(expected = JSQLParserException.class)
    public void testParseExpressionFromStringFail() throws Exception {
         CCJSqlParserUtil.parse("whatever$");
    }

    @Test(expected = JSQLParserException.class)
    public void testParseExpressionFromRaderFail() throws Exception {
         CCJSqlParserUtil.parse(new StringReader("whatever$"));
    }

    @Test
    public void testParseExpressionNonPartial2() throws Exception {
        Expression result = CCJSqlParserUtil.parseExpression("a+", true);
        assertEquals("a", result.toString());
    }

    @Test
    public void testParseCondExpression() throws Exception {
        Expression result = CCJSqlParserUtil.parseCondExpression("a+b>5 and c<3");
        assertEquals("a + b > 5 AND c < 3", result.toString());
    }

    @Test(expected = JSQLParserException.class)
    public void testParseCondExpressionFail() throws Exception {
       CCJSqlParserUtil.parseCondExpression(";");

    }

    @Test(expected = JSQLParserException.class)
    public void testParseFromStreamFail() throws Exception {
       CCJSqlParserUtil.parse(new ByteArrayInputStream("BLA".getBytes(StandardCharsets.UTF_8)));

    }

    @Test(expected = JSQLParserException.class)
    public void testParseFromStreamWithEncodingFail() throws Exception {
       CCJSqlParserUtil.parse(new ByteArrayInputStream("BLA".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8.name());

    }

    @Test
    public void testParseCondExpressionNonPartial() throws Exception {
        Expression result = CCJSqlParserUtil.parseCondExpression("x=92 and y=29", false);
        assertEquals("x = 92 AND y = 29", result.toString());
    }

    @Test(expected = JSQLParserException.class)
    public void testParseCondExpressionNonPartial2() throws Exception {
        Expression result = CCJSqlParserUtil.parseCondExpression("x=92 lasd y=29", false);
        System.out.println(result.toString());
    }

    @Test
    public void testParseCondExpressionPartial2() throws Exception {
        Expression result = CCJSqlParserUtil.parseCondExpression("x=92 lasd y=29", true);
        assertEquals("x = 92", result.toString());
    }

    @Test
    public void testParseCondExpressionIssue471() throws Exception {
        Expression result = CCJSqlParserUtil.parseCondExpression("(SSN,SSM) IN ('11111111111111', '22222222222222')");
        assertEquals("(SSN, SSM) IN ('11111111111111', '22222222222222')", result.toString());
    }

    @Test
    public void testParseStatementsIssue691() throws Exception {
        Statements result = CCJSqlParserUtil.parseStatements(
                "select * from dual;\n"
                + "\n"
                + "select\n"
                + "*\n"
                + "from\n"
                + "dual;\n"
                + "\n"
                + "select *\n"
                + "from dual;");
        assertEquals("SELECT * FROM dual;\n"
                + "SELECT * FROM dual;\n"
                + "SELECT * FROM dual;\n", result.toString());
    }

    @Test(expected = JSQLParserException.class)
    public void testParseStatementsFail() throws Exception {
       CCJSqlParserUtil.parseStatements("select * from dual;WHATEVER!!");
    }

    @Test(expected = JSQLParserException.class)
    public void testParseASTFail() throws Exception {
       CCJSqlParserUtil.parseAST("select * from dual;WHATEVER!!");
    }

    @Test
    public void testParseStatementsIssue691_2() throws Exception {
        Statements result = CCJSqlParserUtil.parseStatements(
                "select * from dual;\n"
                + "---test");
        assertEquals("SELECT * FROM dual;\n", result.toString());
    }

    @Test
    public void testParseStatementIssue742() throws Exception {
        Statements result = CCJSqlParserUtil.parseStatements("CREATE TABLE `table_name` (\n" +
                "  `id` bigint(20) NOT NULL AUTO_INCREMENT,\n" +
                "  `another_column_id` bigint(20) NOT NULL COMMENT 'column id as sent by SYSTEM',\n" +
                "  PRIMARY KEY (`id`),\n" +
                "  UNIQUE KEY `uk_another_column_id` (`another_column_id`)\n" +
                ")");
        assertEquals("CREATE TABLE `table_name` (`id` bigint (20) NOT NULL AUTO_INCREMENT, `another_column_id` " +
                "bigint (20) NOT NULL COMMENT 'column id as sent by SYSTEM', PRIMARY KEY (`id`), UNIQUE KEY `uk_another_column_id` " +
                "(`another_column_id`));\n", result.toString());
    }
}
