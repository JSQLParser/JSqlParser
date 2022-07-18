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

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class CCJSqlParserUtilTest {

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

    @Test
    public void testParseExpressionNonPartial() throws Exception {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parseExpression("a+", false));

    }

    @Test
    public void testParseExpressionFromStringFail() throws Exception {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse("whatever$"));
    }

    @Test
    public void testParseExpressionFromRaderFail() throws Exception {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(new StringReader("whatever$")));
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

    @Test
    public void testParseCondExpressionFail() throws Exception {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parseCondExpression(";"));
    }

    @Test
    public void testParseFromStreamFail() throws Exception {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse(new ByteArrayInputStream("BLA".getBytes(StandardCharsets.UTF_8))));

    }

    @Test
    public void testParseFromStreamWithEncodingFail() throws Exception {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse(new ByteArrayInputStream("BLA".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8.name()));

    }

    @Test
    public void testParseCondExpressionNonPartial() throws Exception {
        Expression result = CCJSqlParserUtil.parseCondExpression("x=92 and y=29", false);
        assertEquals("x = 92 AND y = 29", result.toString());
    }

    @Test
    public void testParseCondExpressionNonPartial2() throws Exception {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parseCondExpression("x=92 lasd y=29", false));
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

    @Test
    public void testStreamStatementsIssue777() throws Exception {
        final List<Statement> list = new ArrayList<>();

        CCJSqlParserUtil.streamStatements(new StatementListener() {
            @Override
            public void accept(Statement statement) {
                list.add(statement);
            }
        }, new ByteArrayInputStream(("select * from dual;\n"
                + "select\n"
                + "*\n"
                + "from\n"
                + "dual;\n"
                + "\n"
                + "-- some comment\n"
                + "select *\n"
                + "from dual;").getBytes(StandardCharsets.UTF_8)), "UTF-8");

        assertEquals(list.size(), 3);
    }

    @Test
    @Disabled
    public void testParseStatementsFail() throws Exception {
        // This will not fail, but always return the Unsupported Statements
        // Since we can't LOOKAHEAD in the Statements() production
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parseStatements("select * from dual;WHATEVER!!"));
    }

    @Test
    public void testParseASTFail() throws Exception {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parseAST("select * from dual;WHATEVER!!"));
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
        Statements result = CCJSqlParserUtil.parseStatements("CREATE TABLE `table_name` (\n"
                + "  `id` bigint(20) NOT NULL AUTO_INCREMENT,\n"
                + "  `another_column_id` bigint(20) NOT NULL COMMENT 'column id as sent by SYSTEM',\n"
                + "  PRIMARY KEY (`id`),\n"
                + "  UNIQUE KEY `uk_another_column_id` (`another_column_id`)\n"
                + ")");
        assertEquals("CREATE TABLE `table_name` (`id` bigint (20) NOT NULL AUTO_INCREMENT, `another_column_id` "
                + "bigint (20) NOT NULL COMMENT 'column id as sent by SYSTEM', PRIMARY KEY (`id`), UNIQUE KEY `uk_another_column_id` "
                + "(`another_column_id`));\n", result.toString());
    }

    @Test
    public void testParseExpressionIssue982() throws Exception {
        Expression result = CCJSqlParserUtil.parseExpression("tab.col");
        assertEquals("tab.col", result.toString());
    }

    @Test
    public void testParseExpressionWithBracketsIssue1159() throws Exception {
        Expression result = CCJSqlParserUtil.parseExpression("[travel_data].[travel_id]", false,
                parser -> parser.withSquareBracketQuotation(true));
        assertEquals("[travel_data].[travel_id]", result.toString());
    }

    @Test
    public void testParseExpressionWithBracketsIssue1159_2() throws Exception {
        Expression result = CCJSqlParserUtil.parseCondExpression("[travel_data].[travel_id]", false,
                parser -> parser.withSquareBracketQuotation(true));
        assertEquals("[travel_data].[travel_id]", result.toString());
    }

    @Test
    public void testNestingDepth() throws Exception {
        assertEquals(2,
                CCJSqlParserUtil.getNestingDepth("SELECT concat(concat('A','B'),'B') FROM mytbl"));
        assertEquals(20, CCJSqlParserUtil.getNestingDepth(
                "concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat(concat('A','B'),'B'),'B'),'B'),'B'),'B'),'B'),'B'),'B'),'B'),'B'),'B'),'B'),'B'),'B'),'B'),'B'),'B'),'B'),'B') FROM mytbl"));
        assertEquals(4, CCJSqlParserUtil.getNestingDepth(""
                + "-- MERGE 1\n"
                + "MERGE INTO cfe.impairment imp\n" + "    USING ( WITH x AS (\n"
                + "                    SELECT  a.id_instrument\n"
                + "                            , a.id_currency\n"
                + "                            , a.id_instrument_type\n"
                + "                            , b.id_portfolio\n"
                + "                            , c.attribute_value product_code\n"
                + "                            , t.valid_date\n" + "                            , t.ccf\n"
                + "                    FROM cfe.instrument a\n"
                + "                        INNER JOIN cfe.impairment b\n"
                + "                            ON a.id_instrument = b.id_instrument\n"
                + "                        LEFT JOIN cfe.instrument_attribute c\n"
                + "                            ON a.id_instrument = c.id_instrument\n"
                + "                                AND c.id_attribute = 'product'\n"
                + "                        INNER JOIN cfe.ext_ccf t\n"
                + "                            ON ( a.id_currency LIKE t.id_currency )\n"
                + "                                AND ( a.id_instrument_type LIKE t.id_instrument_type )\n"
                + "                                AND ( b.id_portfolio LIKE t.id_portfolio\n"
                + "                                        OR ( b.id_portfolio IS NULL\n"
                + "                                                AND t.id_portfolio = '%' ) )\n"
                + "                                AND ( c.attribute_value LIKE t.product_code\n"
                + "                                        OR ( c.attribute_value IS NULL\n"
                + "                                                AND t.product_code = '%' ) ) )\n"
                + "SELECT /*+ PARALLEL */ *\n" + "            FROM x x1\n"
                + "            WHERE x1.valid_date = ( SELECT max\n"
                + "                                    FROM x\n"
                + "                                    WHERE id_instrument = x1.id_instrument ) ) s\n"
                + "        ON ( imp.id_instrument = s.id_instrument )\n" + "WHEN MATCHED THEN\n"
                + "    UPDATE SET  imp.ccf = s.ccf\n" + ";"));
    }

    @Test
    public void testParseStatementIssue1250() throws Exception {
        Statement result = CCJSqlParserUtil.parse("Select test.* from (Select * from sch.PERSON_TABLE // root test\n) as test");
        assertEquals("SELECT test.* FROM (SELECT * FROM sch.PERSON_TABLE) AS test", result.toString());
    }
    
    @Test
    public void testCondExpressionIssue1482() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil.parseCondExpression("test_table_enum.f1_enum IN ('TEST2'::test.test_enum)", false);
        assertEquals("test_table_enum.f1_enum IN ('TEST2'::test.test_enum)", expr.toString());
    }
    
    @Test
    public void testCondExpressionIssue1482_2() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil.parseCondExpression("test_table_enum.f1_enum IN ('TEST2'::test.\"test_enum\")", false);
        assertEquals("test_table_enum.f1_enum IN ('TEST2'::test.\"test_enum\")", expr.toString());
    }

    @Test
    public void testTimeOutIssue1582() throws InterruptedException {
        // This statement is INVALID on purpose
        // There are crafted INTO keywords in order to make it fail but only after a long time (40 seconds plus)

        String sqlStr = "" +
                "select\n" +
                "              t0.operatienr\n" +
                "            , case\n" +
                "                when\n" +
                "                    case when (t0.vc_begintijd_operatie is null or lpad((extract('hours' into t0.vc_begintijd_operatie::timestamp))::text,2,'0') ||':'|| lpad(extract('minutes' from t0.vc_begintijd_operatie::timestamp)::text,2,'0') = '00:00') then null\n" +
                "                         else (greatest(((extract('hours' into (t0.vc_eindtijd_operatie::timestamp-t0.vc_begintijd_operatie::timestamp))*60 + extract('minutes' from (t0.vc_eindtijd_operatie::timestamp-t0.vc_begintijd_operatie::timestamp)))/60)::numeric(12,2),0))*60\n" +
                "                end = 0 then null\n" +
                "                    else '25. Meer dan 4 uur'\n" +
                "                end                                                                                                                                                  \n" +
                "              as snijtijd_interval";

        // With DEFAULT TIMEOUT 6 Seconds, we expect the statement to timeout normally
        // A TimeoutException wrapped into a Parser Exception should be thrown

        assertThrows(TimeoutException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                try {
                    CCJSqlParserUtil.parse(sqlStr);
                } catch (JSQLParserException ex) {
                    Throwable cause = ((JSQLParserException) ex).getCause();
                    if (cause!=null) {
                        throw cause;
                    } else {
                        throw ex;
                    }
                }
            }
        });

        // With custom TIMEOUT 60 Seconds, we expect the statement to not timeout but to fail instead
        // No TimeoutException wrapped into a Parser Exception must be thrown
        // Instead we expect a Parser Exception only
        assertThrows(JSQLParserException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                try {
                    CCJSqlParserUtil.parse(sqlStr, parser -> parser.withTimeOut(60000));
                } catch (JSQLParserException ex) {
                    Throwable cause = ((JSQLParserException) ex).getCause();
                    if (cause instanceof TimeoutException) {
                        throw cause;
                    } else {
                        throw ex;
                    }
                }
            }
        });
    }
}
