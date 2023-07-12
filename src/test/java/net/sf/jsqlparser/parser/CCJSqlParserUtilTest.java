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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.test.MemoryLeakVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class CCJSqlParserUtilTest {

    private final static String INVALID_SQL = ""
            + "SELECT * FROM TABLE_1 t1\n"
            + "WHERE\n"
            + "(((t1.COL1 = 'VALUE2' )\n"
            + "AND (t1.CAL2 = 'VALUE2' ))\n"
            + "AND (((1 = 1 )\n"
            + "AND ((((((t1.id IN (940550 ,940600 ,940650 ,940700 ,940750 ,940800 ,940850 ,940900 ,940950 ,941000 ,941050 ,941100 ,941150 ,941200 ,941250 ,941300 ,941350 ,941400 ,941450 ,941500 ,941550 ,941600 ,941650 ,941700 ,941750 ,941800 ,941850 ,941900 ,941950 ,942000 ,942050 ,942100 ,942150 ,942200 ,942250 ,942300 ,942350 ,942400 ,942450 ,942500 ,942550 ,942600 ,942650 ,942700 ,942750 ,942800 ,942850 ,942900 ,942950 ,943000 ,943050 ,943100 ,943150 ,943200 ,943250 ,943300 ,943350 ,943400 ,943450 ,943500 ,943550 ,943600 ,943650 ,943700 ,943750 ,943800 ,943850 ,943900 ,943950 ,944000 ,944050 ,944100 ,944150 ,944200 ,944250 ,944300 ,944350 ,944400 ,944450 ,944500 ,944550 ,944600 ,944650 ,944700 ,944750 ,944800 ,944850 ,944900 ,944950 ,945000 ,945050 ,945100 ,945150 ,945200 ,945250 ,945300 ))\n"
            + "OR (t1.id IN (945350 ,945400 ,945450 ,945500 ,945550 ,945600 ,945650 ,945700 ,945750 ,945800 ,945850 ,945900 ,945950 ,946000 ,946050 ,946100 ,946150 ,946200 ,946250 ,946300 ,946350 ,946400 ,946450 ,946500 ,946550 ,946600 ,946650 ,946700 ,946750 ,946800 ,946850 ,946900 ,946950 ,947000 ,947050 ,947100 ,947150 ,947200 ,947250 ,947300 ,947350 ,947400 ,947450 ,947500 ,947550 ,947600 ,947650 ,947700 ,947750 ,947800 ,947850 ,947900 ,947950 ,948000 ,948050 ,948100 ,948150 ,948200 ,948250 ,948300 ,948350 ,948400 ,948450 ,948500 ,948550 ,948600 ,948650 ,948700 ,948750 ,948800 ,948850 ,948900 ,948950 ,949000 ,949050 ,949100 ,949150 ,949200 ,949250 ,949300 ,949350 ,949400 ,949450 ,949500 ,949550 ,949600 ,949650 ,949700 ,949750 ,949800 ,949850 ,949900 ,949950 ,950000 ,950050 ,950100 )))\n"
            + "OR (t1.id IN (950150 ,950200 ,950250 ,950300 ,950350 ,950400 ,950450 ,950500 ,950550 ,950600 ,950650 ,950700 ,950750 ,950800 ,950850 ,950900 ,950950 ,951000 ,951050 ,951100 ,951150 ,951200 ,951250 ,951300 ,951350 ,951400 ,951450 ,951500 ,951550 ,951600 ,951650 ,951700 ,951750 ,951800 ,951850 ,951900 ,951950 ,952000 ,952050 ,952100 ,952150 ,952200 ,952250 ,952300 ,952350 ,952400 ,952450 ,952500 ,952550 ,952600 ,952650 ,952700 ,952750 ,952800 ,952850 ,952900 ,952950 ,953000 ,953050 ,953100 ,953150 ,953200 ,953250 ,953300 ,953350 ,953400 ,953450 ,953500 ,953550 ,953600 ,953650 ,953700 )))\n"
            + "OR (t1.id IN (953750 ,953800 ,953850 ,953900 ,953950 ,954000 ,954050 ,954100 ,954150 ,954200 ,954250 ,954300 ,954350 ,954400 ,954450 ,954500 ,954550 ,954600 ,954650 ,954700 ,954750 ,954800 ,954850 ,954900 ,954950 ,955000 ,955050 ,955100 ,955150 ,955200 ,955250 ,955300 ,955350 ,955400 ,955450 ,955500 ,955550 ,955600 ,955650 ,955700 ,955750 ,955800 ,955850 ,955900 ,955950 ,956000 ,956050 ,956100 ,956150 ,956200 ,956250 ,956300 ,956350 ,956400 ,956450 ,956500 ,956550 ,956600 ,956650 ,956700 ,956750 ,956800 ,956850 ,956900 ,956950 ,957000 ,957050 ,957100 ,957150 ,957200 ,957250 ,957300 )))\n"
            + "OR (t1.id IN (944100, 944150, 944200, 944250, 944300, 944350, 944400, 944450, 944500, 944550, 944600, 944650, 944700, 944750, 944800, 944850, 944900, 944950, 945000 )))\n"
            + "OR (t1.id IN (957350 ,957400 ,957450 ,957500 ,957550 ,957600 ,957650 ,957700 ,957750 ,957800 ,957850 ,957900 ,957950 ,958000 ,958050 ,958100 ,958150 ,958200 ,958250 ,958300 ,958350 ,958400 ,958450 ,958500 ,958550 ,958600 ,958650 ,958700 ,958750 ,958800 ,958850 ,958900 ,958950 ,959000 ,959050 ,959100 ,959150 ,959200 ,959250 ,959300 ,959350 ,959400 ,959450 ,959500 ,959550 ,959600 ,959650 ,959700 ,959750 ,959800 ,959850 ,959900 ,959950 ,960000 ,960050 ,960100 ,960150 ,960200 ,960250 ,960300 ,960350 ,960400 ,960450 ,960500 ,960550 ,960600 ,960650 ,960700 ,960750 ,960800 ,960850 ,960900 ,960950 ,961000 ,961050 ,961100 ,961150 ,961200 ,961250 ,961300 ,961350 ,961400 ,961450 ,961500 ,961550 ,961600 ,961650 ,961700 ,961750 ,961800 ,961850 ,961900 ,961950 ,962000 ,962050 ,962100 ))))\n"
            + "OR (t1.id IN (962150 ,962200 ,962250 ,962300 ,962350 ,962400 ,962450 ,962500 ,962550 ,962600 ,962650 ,962700 ,962750 ,962800 ,962850 ,962900 ,962950 ,963000 ,963050 ,963100 ,963150 ,963200 ,963250 ,963300 ,963350 ,963400 ,963450 ,963500 ,963550 ,963600 ,963650 ,963700 ,963750 ,963800 ,963850 ,963900 ,963950 ,964000 ,964050 ,964100 ,964150 ,964200 ,964250 ,964300 ,964350 ,964400 ,964450 ,964500 ,964550 ,964600 ,964650 ,964700 ,964750 ,964800 ,964850 ,964900 ,964950 ,965000 ,965050 ,965100 ,965150 ,965200 ,965250 ,965300 ,965350 ,965400 ,965450 ,965500 ))))\n"
            + "AND t1.COL3 IN (\n"
            + "    SELECT\n"
            + "    t2.COL3\n"
            + "    FROM\n"
            + "    TABLE_6 t6,\n"
            + "    TABLE_1 t5,\n"
            + "    TABLE_4 t4,\n"
            + "    TABLE_3 t3,\n"
            + "    TABLE_1 t2\n"
            + "    WHERE\n"
            + "    (((((((t5.CAL3 = T6.id)\n"
            + "    AND (t5.CAL5 = t6.CAL5))\n"
            + "    AND (t5.CAL1 = t6.CAL1))\n"
            + "    AND (t3.CAL1 IN (108500)))\n"
            + "    AND (t5.id = t2.id))\n"
            + "    AND NOT ((t6.CAL6 IN ('VALUE'))))\n"
            + "    AND ((t2.id = t3.CAL2)\n"
            + "    AND (t4.id = t3.CAL3))))\n" +
            // add two redundant unmatched brackets in order to make the Simple Parser fail
            // and get the complex parser stuck
            " )) \n"
            + "ORDER BY\n"
            + "t1.id ASC";

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
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parseExpression("a+", false));

    }

    @Test
    public void testParseExpressionFromStringFail() throws Exception {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse("whatever$"));
    }

    @Test
    public void testParseExpressionFromRaderFail() throws Exception {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse(new StringReader("whatever$")));
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
                () -> CCJSqlParserUtil
                        .parse(new ByteArrayInputStream("BLA".getBytes(StandardCharsets.UTF_8))));

    }

    @Test
    public void testParseFromStreamWithEncodingFail() throws Exception {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse(
                        new ByteArrayInputStream("BLA".getBytes(StandardCharsets.UTF_8)),
                        StandardCharsets.UTF_8.name()));

    }

    @Test
    public void testParseCondExpressionNonPartial() throws Exception {
        Expression result = CCJSqlParserUtil.parseCondExpression("x=92 and y=29", false);
        assertEquals("x = 92 AND y = 29", result.toString());
    }

    @Test
    public void testParseCondExpressionNonPartial2() throws Exception {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parseCondExpression("x=92 lasd y=29", false));
    }

    @Test
    public void testParseCondExpressionPartial2() throws Exception {
        Expression result = CCJSqlParserUtil.parseCondExpression("x=92 lasd y=29", true);
        assertEquals("x = 92", result.toString());
    }

    @Test
    public void testParseCondExpressionIssue471() throws Exception {
        Expression result = CCJSqlParserUtil
                .parseCondExpression("(SSN,SSM) IN ('11111111111111', '22222222222222')");
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
    public void testParseStatementsFail() throws Exception {
        String sqlStr = "select * from dual;WHATEVER!!";

        // Won't fail but return Unsupported Statement instead
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                final Statements statements = CCJSqlParserUtil.parseStatements(sqlStr);
                assertEquals(2, statements.size());
                assertTrue(statements.get(0) instanceof PlainSelect);
                assertTrue(statements.get(1) instanceof UnsupportedStatement);
            }
        });
    }

    @Test
    public void testParseASTFail() throws Exception {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parseAST("select * from dual;WHATEVER!!"));
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
        assertEquals(
                "CREATE TABLE `table_name` (`id` bigint (20) NOT NULL AUTO_INCREMENT, `another_column_id` "
                        + "bigint (20) NOT NULL COMMENT 'column id as sent by SYSTEM', PRIMARY KEY (`id`), UNIQUE KEY `uk_another_column_id` "
                        + "(`another_column_id`));\n",
                result.toString());
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
                + "                            , t.valid_date\n"
                + "                            , t.ccf\n"
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
        Statement result = CCJSqlParserUtil.parse(
                "Select test.* from (Select * from sch.PERSON_TABLE // root test\n) as test");
        assertEquals("SELECT test.* FROM (SELECT * FROM sch.PERSON_TABLE) AS test",
                result.toString());
    }

    @Test
    public void testCondExpressionIssue1482() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil
                .parseCondExpression("test_table_enum.f1_enum IN ('TEST2'::test.test_enum)", false);
        assertEquals("test_table_enum.f1_enum IN ('TEST2'::test.test_enum)", expr.toString());
    }

    @Test
    public void testCondExpressionIssue1482_2() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil.parseCondExpression(
                "test_table_enum.f1_enum IN ('TEST2'::test.\"test_enum\")", false);
        assertEquals("test_table_enum.f1_enum IN ('TEST2'::test.\"test_enum\")", expr.toString());
    }

    /**
     * The purpose of the test is to run into a timeout and to stop the parser when this happens. We
     * provide an INVALID statement for this purpose, which will fail the SIMPLE parse and then hang
     * with COMPLEX parsing until the timeout occurs.
     * <p>
     * We repeat that test multiple times and want to see no stale references to the Parser after
     * timeout.
     */
    @Test
    public void testParserInterruptedByTimeout() {
        MemoryLeakVerifier verifier = new MemoryLeakVerifier();

        int parallelThreads = Runtime.getRuntime().availableProcessors() + 1;
        ExecutorService executorService = Executors.newFixedThreadPool(parallelThreads);
        ExecutorService timeOutService = Executors.newSingleThreadExecutor();
        for (int i = 0; i < parallelThreads; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {

                    try {
                        CCJSqlParser parser =
                                CCJSqlParserUtil.newParser(INVALID_SQL)
                                        .withAllowComplexParsing(true);
                        verifier.addObject(parser);
                        CCJSqlParserUtil.parseStatement(parser, timeOutService);
                    } catch (JSQLParserException ignore) {
                        // We expected that to happen.
                    }
                }
            });
        }
        timeOutService.shutdownNow();
        executorService.shutdown();

        // we should not run in any timeout here (because we expect that the Parser has timed out by
        // itself)
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            }
        });

        // we should not have any Objects left in the weak reference map
        verifier.assertGarbageCollected();
    }

    @Test
    public void testTimeOutIssue1582() {
        // This statement is INVALID on purpose
        // There are crafted INTO keywords in order to make it fail but only after a long time (40
        // seconds plus)

        String sqlStr = ""
                + "select\n"
                + "  t0.operatienr\n"
                + "  , case\n"
                + "        when\n"
                + "            case when (t0.vc_begintijd_operatie is null or lpad((extract('hours' into t0.vc_begintijd_operatie::timestamp))::text,2,'0') ||':'|| lpad(extract('minutes' from t0.vc_begintijd_operatie::timestamp)::text,2,'0') = '00:00') then null\n"
                + "                 else (greatest(((extract('hours' into (t0.vc_eindtijd_operatie::timestamp-t0.vc_begintijd_operatie::timestamp))*60 + extract('minutes' from (t0.vc_eindtijd_operatie::timestamp-t0.vc_begintijd_operatie::timestamp)))/60)::numeric(12,2),0))*60\n"
                + "        end = 0 then null\n"
                + "            else '25. Meer dan 4 uur'\n"
                + "        end\n"
                + "      as snijtijd_interval";

        // With DEFAULT TIMEOUT 6 Seconds, we expect the statement to timeout normally
        // A TimeoutException wrapped into a Parser Exception should be thrown
        assertThrows(JSQLParserException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                try {
                    CCJSqlParserUtil.parse(sqlStr);
                } catch (JSQLParserException ex) {
                    assertTrue(ex.getCause() instanceof TimeoutException);
                    throw ex;
                }
            }
        });

        // With custom TIMEOUT 60 Seconds, we expect the statement to not timeout but to fail
        // instead
        // No TimeoutException wrapped into a Parser Exception must be thrown
        // Instead we expect a Parser Exception only
        assertThrows(JSQLParserException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                try {
                    CCJSqlParserUtil.parse(sqlStr, parser -> {
                        parser.withTimeOut(60000);
                        parser.withAllowComplexParsing(false);
                    });
                } catch (JSQLParserException ex) {
                    assertFalse(ex.getCause() instanceof TimeoutException);
                    throw ex;
                }
            }
        });
    }

    // Supposed to time out
    @Test
    void testComplexIssue1792() throws JSQLParserException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        CCJSqlParserUtil.LOGGER.setLevel(Level.ALL);

        // Expect to fail fast with SIMPLE Parsing only when COMPLEX is not allowed
        // No TIMEOUT Exception shall be thrown
        // CCJSqlParserUtil.LOGGER will report:
        // 1) Allowed Complex Parsing: false
        // 2) Trying SIMPLE parsing only
        assertThrows(JSQLParserException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                try {
                    CCJSqlParserUtil.parse(INVALID_SQL, executorService, parser -> {
                        parser.withTimeOut(10000);
                        parser.withAllowComplexParsing(false);
                    });
                } catch (JSQLParserException ex) {
                    assertFalse(ex.getCause() instanceof TimeoutException);
                    throw ex;
                }
            }
        });

        // Expect to time-out with COMPLEX Parsing allowed
        // CCJSqlParserUtil.LOGGER will report:
        // 1) Allowed Complex Parsing: true
        // 2) Trying SIMPLE parsing first
        // 3) Trying COMPLEX parsing when SIMPLE parsing failed
        assertThrows(JSQLParserException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                try {
                    CCJSqlParserUtil.parse(INVALID_SQL, executorService, parser -> {
                        parser.withTimeOut(1000);
                        parser.withAllowComplexParsing(true);
                    });
                } catch (JSQLParserException ex) {
                    assertTrue(ex.getCause() instanceof TimeoutException);
                    throw ex;
                }
            }
        });
        executorService.shutdownNow();
        CCJSqlParserUtil.LOGGER.setLevel(Level.OFF);
    }

    @Test
    void testUnbalancedPosition() {
        String sqlStr = "SELECT * from ( test ";
        sqlStr = "select\n" +
                " concat('{','\"dffs\":\"',if(dffs is null,'',cast(dffs as string),'\",\"djr\":\"',if(djr is null,'',cast(djr as string),'\",\"djrq\":\"',if(djrq is null,'',cast(djrq as string),'\",\"thjssj\":\"',if(thjssj is null,'',cast(thjssj as string),'\",\"thkssj\":\"',if(thkssj is null,'',cast(thkssj as string),'\",\"sjc\":\"',if(sjc is null,'',cast(sjc as string),'\",\"ldhm\":\"',if(ldhm is null,'',cast(ldhm as string),'\",\"lxdh\":\"',if(lxdh is null,'',cast(lxdh as string),'\",\"md\":\"',if(md is null,'',cast(md as string),'\",\"nr\":\"',if(nr is null,'',cast(nr as string),'\",\"nrfl\":\"',if(nrfl is null,'',cast(nrfl as string),'\",\"nrwjid\":\"',if(nrwjid is null,'',cast(nrwjid as string),'\",\"sfbm\":\"',if(sfbm is null,'',cast(sfbm as string),'\",\"sjly\":\"',if(sjly is null,'',cast(sjly as string),'\",\"wtsd\":\"',if(wtsd is null,'',cast(wtsd as string),'\",\"xb\":\"',if(xb is null,'',cast(xb as string),'\",\"xfjbh\":\"',if(xfjbh is null,'',cast(xfjbh as string),'\",\"xfjid\":\"',if(xfjid is null,'',cast(xfjid as string),'\",\"xm\":\"',if(xm is null,'',cast(xm as string),'\",\"zhut\":\"',if(zhut is null,'',cast(zhut as string),'\",\"zt\":\"',if(zt is null,'',cast(zt as string),'\"}')\n" +
                " from tab";
        assertEquals(-1 , CCJSqlParserUtil.getUnbalancedPosition(sqlStr));
    }
}
