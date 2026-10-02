/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.parser.StreamProvider;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class UnsupportedStatementRecoveryTest {
    static Stream<String> malformedStatements() {
        return Stream.of("SELECT * FROM", "INSERT INTO t (a", "UPDATE t SET",
                "SELECT", "INSERT", "UPDATE", "WITH", "SELECT 1 +",
                "insert into t values (1,", "select f(1, 2) from t where x =",
                "IF x > 0 UPDATE t SET", "IF x > 0 SELECT 1; ELSE UPDATE t SET");
    }

    static Stream<Arguments> opaqueStatements() {
        return Stream.of(Arguments.of("shutdown defrag", "shutdown defrag"),
                Arguments.of("SET IDENTITY_INSERT tb_inter_d2v_transfer on",
                        "SET IDENTITY_INSERT tb_inter_d2v_transfer on"),
                Arguments.of("set isolation to dirty read", "set isolation to dirty read"),
                Arguments.of("set isolation dirty read", "set isolation dirty read"),
                Arguments.of("SET ISOLATION TO DIRTY READ WITH WARNING",
                        "SET ISOLATION TO DIRTY READ WITH WARNING"),
                Arguments.of("SET ISOLATION TO COMMITTED READ LAST COMMITTED",
                        "SET ISOLATION TO COMMITTED READ LAST COMMITTED"),
                Arguments.of("SET ISOLATION TO CURSOR STABILITY RETAIN UPDATE LOCKS",
                        "SET ISOLATION TO CURSOR STABILITY RETAIN UPDATE LOCKS"),
                Arguments.of("SET ISOLATION TO REPEATABLE READ",
                        "SET ISOLATION TO REPEATABLE READ"),
                // These are capture controls, not claims of valid dialect syntax.
                Arguments.of("mystery 'a;b' /* comment */ payload", "mystery 'a;b' payload"),
                Arguments.of("this is an unsupported statement",
                        "this is an unsupported statement"));
    }

    @ParameterizedTest
    @MethodSource("malformedStatements")
    void malformedSupportedSyntaxIsNotOpaque(String sql) throws Exception {
        for (boolean unsupported : new boolean[] {false, true}) {
            for (boolean complex : new boolean[] {false, true}) {
                for (String delimiter : new String[] {"", ";"}) {
                    String input = sql + delimiter;
                    assertThrowsExactly(JSQLParserException.class,
                            () -> CCJSqlParserUtil.parse(input,
                                    p -> p.withUnsupportedStatements(unsupported)
                                            .withAllowComplexParsing(complex)));
                    assertThrowsExactly(JSQLParserException.class,
                            () -> CCJSqlParserUtil.parseStatements(input,
                                    p -> p.withUnsupportedStatements(unsupported)
                                            .withAllowComplexParsing(complex)));
                    CCJSqlParser parser = CCJSqlParserUtil.newParser(input)
                            .withUnsupportedStatements(unsupported)
                            .withAllowComplexParsing(complex);
                    assertThrowsExactly(ParseException.class, parser::Statement);
                    parser = CCJSqlParserUtil.newParser(input)
                            .withUnsupportedStatements(unsupported)
                            .withAllowComplexParsing(complex);
                    assertThrowsExactly(ParseException.class, parser::Statements);
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT * FROM", "INSERT INTO t (a", "UPDATE t SET",
            "IF x > 0 SELECT 1; ELSE UPDATE t SET"})
    void syntaxRecoveryRecordsErrorWithoutPublishingPartialAst(String sql) throws Exception {
        for (boolean unsupported : new boolean[] {false, true}) {
            CCJSqlParser parser = CCJSqlParserUtil.newParser(sql)
                    .withUnsupportedStatements(unsupported).withErrorRecovery(true);
            assertNull(parser.Statement());
            assertEquals(1, parser.getParseErrors().size());
            for (String prefix : new String[] {"", "SELECT 0;", ";;SELECT 0;;;"}) {
                parser = CCJSqlParserUtil.newParser(prefix + sql + ";;SELECT 2;")
                        .withUnsupportedStatements(unsupported).withErrorRecovery(true);
                Statements statements = parser.Statements();
                int index = prefix.contains("SELECT") ? 1 : 0;
                assertEquals(index + 2, statements.size());
                if (index > 0) {
                    assertEquals("SELECT 0",
                            assertInstanceOf(Select.class, statements.get(0)).toString());
                }
                assertNull(statements.get(index));
                assertEquals("SELECT 2",
                        assertInstanceOf(Select.class, statements.get(index + 1)).toString());
                assertEquals(1, parser.getParseErrors().size());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT * FROM", "INSERT INTO t (a", "UPDATE t SET",
            "IF x > 0 SELECT 1; ELSE UPDATE t SET", "UPDATE t SET a = 'x;y',"})
    void laterMalformedStatementCannotEscapeAsPrefixAndOpaqueSuffix(String sql) {
        for (boolean unsupported : new boolean[] {false, true}) {
            for (boolean complex : new boolean[] {false, true}) {
                assertThrowsExactly(JSQLParserException.class,
                        () -> CCJSqlParserUtil.parseStatements("SELECT 0;" + sql + ";SELECT 2;",
                                p -> p.withUnsupportedStatements(unsupported)
                                        .withAllowComplexParsing(complex)));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("opaqueStatements")
    void opaqueStatementTextAndFollowingStatementsSurvive(String sql, String expected)
            throws Exception {
        for (boolean recovery : new boolean[] {false, true}) {
            Statement result = CCJSqlParserUtil.parse("/* before */" + sql + "; -- after\n",
                    p -> p.withUnsupportedStatements(true).withErrorRecovery(recovery));
            assertEquals(expected, assertInstanceOf(UnsupportedStatement.class, result).toString());
            TestUtils.assertStatementCanBeDeparsedAs(result, expected, true);
            for (String prefix : new String[] {"", ";;", "SELECT 0;", "SELECT 0;;;"}) {
                CCJSqlParser parser = CCJSqlParserUtil.newParser(prefix + sql + ";;;SELECT 2;")
                        .withUnsupportedStatements(true).withErrorRecovery(recovery);
                Statements statements = parser.Statements();
                int index = prefix.contains("SELECT") ? 1 : 0;
                assertEquals(index + 2, statements.size());
                if (index > 0) {
                    assertEquals("SELECT 0", statements.get(0).toString());
                }
                assertEquals(expected,
                        assertInstanceOf(UnsupportedStatement.class, statements.get(index))
                                .toString());
                assertEquals("SELECT 2",
                        assertInstanceOf(Select.class, statements.get(index + 1)).toString());
                assertTrue(parser.getParseErrors().isEmpty());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT * FROM", "INSERT INTO t (a", "UPDATE t SET"})
    void readerAndInputStreamUseSameRecoveryPolicy(String sql) throws Exception {
        for (boolean unsupported : new boolean[] {false, true}) {
            for (boolean recovery : new boolean[] {false, true}) {
                for (boolean multiple : new boolean[] {false, true}) {
                    CCJSqlParser reader =
                            new CCJSqlParser(new StreamProvider(new StringReader(sql)))
                                    .withUnsupportedStatements(unsupported)
                                    .withErrorRecovery(recovery);
                    CCJSqlParser stream = CCJSqlParserUtil.newParser(
                            new ByteArrayInputStream(sql.getBytes(StandardCharsets.UTF_8)), "UTF-8")
                            .withUnsupportedStatements(unsupported).withErrorRecovery(recovery);
                    for (CCJSqlParser parser : new CCJSqlParser[] {reader, stream}) {
                        if (recovery) {
                            assertNull(multiple ? parser.Statements().get(0) : parser.Statement());
                            assertEquals(1, parser.getParseErrors().size());
                        } else {
                            assertThrowsExactly(ParseException.class,
                                    () -> {
                                        if (multiple) {
                                            parser.Statements();
                                        } else {
                                            parser.Statement();
                                        }
                                    });
                        }
                    }
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"SET IDENTITY_INSERT = 1", "SET IDENTITY_INSERT.foo = 1",
            "SET isolation = 1", "SET isolation.foo = 1"})
    void ordinarySetAssignmentsAreNotRerouted(String sql) throws Exception {
        for (boolean unsupported : new boolean[] {false, true}) {
            for (Dialect dialect : new Dialect[] {Dialect.SQLSERVER, Dialect.POSTGRESQL,
                    Dialect.EXASOL}) {
                assertInstanceOf(SetStatement.class, TestUtils.assertSqlCanBeParsedAndDeparsed(sql,
                        true,
                        p -> p.withDialect(dialect).withUnsupportedStatements(unsupported)));
            }
            assertInstanceOf(SetStatement.class,
                    TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true,
                            p -> p.withUnsupportedStatements(unsupported)));
        }
    }

    @Test
    void ordinarySetToValueRemainsTypedInApplicableDialects() throws Exception {
        for (boolean unsupported : new boolean[] {false, true}) {
            assertInstanceOf(SetStatement.class, CCJSqlParserUtil.parse("SET isolation TO 1",
                    p -> p.withUnsupportedStatements(unsupported)));
            assertInstanceOf(SetStatement.class, CCJSqlParserUtil.parse("SET isolation TO 1",
                    p -> p.withDialect(Dialect.POSTGRESQL).withUnsupportedStatements(unsupported)));
        }
    }

    @Test
    void validTypedStatementsAndSeparatorlessAdjacencyRemainSupported() throws Exception {
        for (boolean unsupported : new boolean[] {false, true}) {
            for (boolean recovery : new boolean[] {false, true}) {
                assertInstanceOf(Select.class, TestUtils.assertSqlCanBeParsedAndDeparsed(
                        "SELECT a FROM t", true,
                        p -> p.withUnsupportedStatements(unsupported).withErrorRecovery(recovery)));
                assertInstanceOf(IfElseStatement.class, TestUtils.assertSqlCanBeParsedAndDeparsed(
                        "IF x > 0 SELECT 1 ELSE SELECT 2", true,
                        p -> p.withUnsupportedStatements(unsupported).withErrorRecovery(recovery)));
                Statements statements = CCJSqlParserUtil.parseStatements(
                        "SELECT 0; SELECT 1 SELECT 2",
                        p -> p.withUnsupportedStatements(unsupported).withErrorRecovery(recovery));
                assertEquals(3, statements.size());
                assertEquals("SELECT 2",
                        assertInstanceOf(Select.class, statements.get(2)).toString());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"FROM t SELECT a, b", "FROM t |> WHERE a > 1",
            "PIVOT sales ON region USING SUM(amount)", "UNPIVOT monthly_sales ON jan, feb",
            "DESC table_name", "SUMMARIZE cfe.test", "VALUE (1)", "BRANCH START"})
    void alternateKnownRootsKeepTheirTypedDispatch(String sql) throws Exception {
        Statement control = CCJSqlParserUtil.parse(sql);
        assertTrue(!(control instanceof UnsupportedStatement));
        Statement actual = CCJSqlParserUtil.parse(sql, p -> p.withUnsupportedStatements(true));
        assertEquals(control.getClass(), actual.getClass());
        assertEquals(control.toString(), actual.toString());
        Statements script = CCJSqlParserUtil.parseStatements("SELECT 0;" + sql + ";SELECT 2;",
                p -> p.withUnsupportedStatements(true));
        assertEquals(3, script.size());
        assertEquals(control.getClass(), script.get(1).getClass());
        assertEquals("SELECT 2", script.get(2).toString());
    }

    @Test
    void oneStatementEntryDoesNotCaptureMultipleStatementsAsOneOpaqueValue() {
        for (String sql : new String[] {"SELECT 1; SELECT 2", "shutdown defrag; SELECT 2"}) {
            assertThrowsExactly(JSQLParserException.class,
                    () -> CCJSqlParserUtil.parse(sql, p -> p.withUnsupportedStatements(true)));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"SET IDENTITY_INSERT t", "SET IDENTITY_INSERT a.b.c.d ON",
            "SET ISOLATION TO DIRTY", "SET IDENTITY_INSERT t ON garbage",
            "SET ISOLATION TO COMMITTED READ WITH WARNING",
            "SET ISOLATION TO REPEATABLE READ RETAIN UPDATE LOCKS"})
    void incompleteOpaqueVendorFormsAreRejected(String sql) {
        assertThrowsExactly(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse(sql, p -> p.withUnsupportedStatements(true)));
        assertThrowsExactly(JSQLParserException.class,
                () -> CCJSqlParserUtil.parseStatements(sql,
                        p -> p.withUnsupportedStatements(true)));
    }

    @Test
    void lexicalErrorsAreNotSwallowed() {
        assertThrowsExactly(JSQLParserException.class, () -> CCJSqlParserUtil.parse(
                "UPDATE t SET a = 'unterminated", p -> p.withUnsupportedStatements(true)));
    }

    @Test
    void directSingleStatementRemainsIncremental() throws Exception {
        CCJSqlParser parser =
                CCJSqlParserUtil.newParser("SELECT * FROM").withUnsupportedStatements(true);
        assertEquals("SELECT *",
                assertInstanceOf(Select.class, parser.SingleStatement()).toString());
        assertEquals("FROM", parser.getToken(1).image);
        parser = CCJSqlParserUtil.newParser("UPDATE t SET").withUnsupportedStatements(true)
                .withErrorRecovery(true);
        assertThrowsExactly(ParseException.class, parser::SingleStatement);
    }
}
