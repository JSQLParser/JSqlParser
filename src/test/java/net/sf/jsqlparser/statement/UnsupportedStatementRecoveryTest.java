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
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.parser.StreamProvider;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class UnsupportedStatementRecoveryTest {
    static Stream<Arguments> incompleteStatements() {
        return Stream.of(
                Arguments.of("INSERT INTO t (a", "INSERT INTO t ( a"),
                Arguments.of("UPDATE t SET", "UPDATE t SET"),
                Arguments.of("select * from", "select * from"),
                Arguments.of("insert into t values (1,", "insert into t values ( 1 ,"),
                Arguments.of("update \"Mixed\" set a = 'x;y',", "update \"Mixed\" set a = 'x;y' ,"),
                Arguments.of("select 1 +", "select 1 +"),
                Arguments.of("select 1 unexpected trailing", "select 1 unexpected trailing"),
                Arguments.of("select f(1, 2) from t where x =",
                        "select f ( 1 , 2 ) from t where x ="),
                Arguments.of("IF x > 0 UPDATE t SET", "IF x > 0 UPDATE t SET"));
    }

    static Stream<Arguments> failedStatementProductions() {
        return Stream.of(
                Arguments.of("INSERT INTO t (a", "INSERT INTO t ( a"),
                Arguments.of("UPDATE t SET", "UPDATE t SET"),
                Arguments.of("insert into t values (1,", "insert into t values ( 1 ,"),
                Arguments.of("select 1 +", "select 1 +"),
                Arguments.of("select f(1, 2) from t where x =",
                        "select f ( 1 , 2 ) from t where x ="),
                Arguments.of("IF x > 0 UPDATE t SET", "IF x > 0 UPDATE t SET"));
    }

    static Stream<Arguments> incrementallyParsedPrefixes() {
        return Stream.of(
                Arguments.of("select * from", Select.class, "SELECT *", "from"),
                Arguments.of("update \"Mixed\" set a = 'x;y',", Update.class,
                        "UPDATE \"Mixed\" SET a = 'x;y'", ","),
                Arguments.of("select 1 unexpected trailing", Select.class,
                        "SELECT 1 unexpected", "trailing"));
    }

    @ParameterizedTest
    @MethodSource("incompleteStatements")
    void singleStatementPreservesConsumedTokens(String sql, String expected) throws Exception {
        for (boolean recovery : new boolean[] {false, true}) {
            for (String delimiter : new String[] {"", ";"}) {
                Statement statement = CCJSqlParserUtil.parse(sql + delimiter,
                        p -> p.withUnsupportedStatements(true).withErrorRecovery(recovery));
                assertInstanceOf(UnsupportedStatement.class, statement);
                assertEquals(expected, statement.toString());
                TestUtils.assertStatementCanBeDeparsedAs(statement, expected, true);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("failedStatementProductions")
    void statementListPreservesTextAndNextStatement(String sql, String expected) throws Exception {
        for (boolean recovery : new boolean[] {false, true}) {
            for (String prefix : new String[] {"", "SELECT 0;", ";;SELECT 0;;;"}) {
                int index = prefix.contains("SELECT") ? 1 : 0;
                CCJSqlParser parser = CCJSqlParserUtil.newParser(prefix + sql + ";;SELECT 2;")
                        .withUnsupportedStatements(true).withErrorRecovery(recovery);
                Statements statements = parser.Statements();
                assertEquals(index + 2, statements.size());
                if (index > 0) {
                    assertInstanceOf(Select.class, statements.get(0));
                    assertEquals("SELECT 0", statements.get(0).toString());
                }
                assertInstanceOf(UnsupportedStatement.class, statements.get(index));
                assertEquals(expected, statements.get(index).toString());
                assertInstanceOf(Select.class, statements.get(index + 1));
                assertEquals("SELECT 2", statements.get(index + 1).toString());
                assertTrue(parser.getParseErrors().isEmpty());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("incompleteStatements")
    void firstStatementInListPreservesText(String sql, String expected) throws Exception {
        for (String delimiter : new String[] {"", ";", ";SELECT 2;"}) {
            Statements statements = CCJSqlParserUtil.parseStatements(sql + delimiter,
                    p -> p.withUnsupportedStatements(true));
            assertEquals(delimiter.contains("SELECT") ? 2 : 1, statements.size());
            assertInstanceOf(UnsupportedStatement.class, statements.get(0));
            assertEquals(expected, statements.get(0).toString());
            if (statements.size() > 1) {
                assertEquals("SELECT 2",
                        assertInstanceOf(Select.class, statements.get(1)).toString());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("incrementallyParsedPrefixes")
    void laterStatementKeepsIncrementallyParsedPrefix(String sql, Class<?> expectedType,
            String expectedPrefix, String expectedSuffix) throws Exception {
        for (boolean recovery : new boolean[] {false, true}) {
            Statements statements =
                    CCJSqlParserUtil.parseStatements("SELECT 0;" + sql + ";SELECT 2;",
                            p -> p.withUnsupportedStatements(true).withErrorRecovery(recovery));
            assertEquals(4, statements.size());
            assertEquals("SELECT 0", assertInstanceOf(Select.class, statements.get(0)).toString());
            assertInstanceOf(expectedType, statements.get(1));
            assertEquals(expectedPrefix, statements.get(1).toString());
            assertEquals(expectedSuffix,
                    assertInstanceOf(UnsupportedStatement.class, statements.get(2)).toString());
            assertEquals("SELECT 2", assertInstanceOf(Select.class, statements.get(3)).toString());
        }
    }

    @ParameterizedTest
    @MethodSource("incompleteStatements")
    void configuredReaderAndInputStreamPreserveText(String sql, String expected) throws Exception {
        for (boolean multiple : new boolean[] {false, true}) {
            for (boolean recovery : new boolean[] {false, true}) {
                CCJSqlParser readerParser =
                        new CCJSqlParser(new StreamProvider(new StringReader(sql)))
                                .withUnsupportedStatements(true).withErrorRecovery(recovery);
                Statement readerStatement = multiple ? readerParser.Statements().get(0)
                        : readerParser.Statement();
                assertEquals(expected, assertInstanceOf(UnsupportedStatement.class,
                        readerStatement).toString());
                CCJSqlParser streamParser = CCJSqlParserUtil.newParser(
                        new ByteArrayInputStream(sql.getBytes(StandardCharsets.UTF_8)), "UTF-8")
                        .withUnsupportedStatements(true).withErrorRecovery(recovery);
                Statement streamStatement = multiple ? streamParser.Statements().get(0)
                        : streamParser.Statement();
                assertEquals(expected, assertInstanceOf(UnsupportedStatement.class,
                        streamStatement).toString());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"INSERT INTO t (a", "UPDATE t SET", "SELECT * FROM"})
    void disabledUnsupportedStatementsKeepsErrors(String sql) throws Exception {
        assertThrowsExactly(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
        assertThrowsExactly(JSQLParserException.class, () -> CCJSqlParserUtil.parseStatements(sql));
        assertThrowsExactly(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse(new StringReader(sql)));
        assertThrowsExactly(JSQLParserException.class, () -> CCJSqlParserUtil.parse(
                new ByteArrayInputStream(sql.getBytes(StandardCharsets.UTF_8))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"INSERT INTO t (a", "UPDATE t SET", "SELECT * FROM"})
    void disabledUnsupportedStatementsKeepsNullRecovery(String sql) throws Exception {
        CCJSqlParser parser = CCJSqlParserUtil.newParser(sql).withErrorRecovery(true);
        assertNull(parser.Statement());
        assertEquals(1, parser.getParseErrors().size());
        parser = CCJSqlParserUtil.newParser(sql + "; SELECT 2").withErrorRecovery(true);
        Statements statements = parser.Statements();
        assertEquals(2, statements.size());
        assertNull(statements.get(0));
        assertEquals("SELECT 2", assertInstanceOf(Select.class, statements.get(1)).toString());
        assertEquals(1, parser.getParseErrors().size());
    }

    @Test
    void validStatementRemainsTypedAndRoundTrips() throws Exception {
        String sql = "SELECT a FROM t WHERE a = 1";
        for (boolean unsupported : new boolean[] {false, true}) {
            for (boolean recovery : new boolean[] {false, true}) {
                assertInstanceOf(Select.class, TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true,
                        p -> p.withUnsupportedStatements(unsupported).withErrorRecovery(recovery)));
            }
        }
    }

    @Test
    void validIfElseRemainsTypedAndRoundTrips() throws Exception {
        String sql = "IF x > 0 SELECT 1 ELSE SELECT 2";
        for (boolean unsupported : new boolean[] {false, true}) {
            for (boolean recovery : new boolean[] {false, true}) {
                assertInstanceOf(IfElseStatement.class,
                        TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true,
                                p -> p.withUnsupportedStatements(unsupported)
                                        .withErrorRecovery(recovery)));
            }
        }
    }

    @Test
    void singleStatementStillRejectsMultipleStatements() {
        assertThrowsExactly(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse("SELECT 1; SELECT 2"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"select 1; select 2", "IF x > 0 SELECT 1 trailing garbage"})
    void singleStatementPreservesTrailingInput(String sql) throws Exception {
        Statement statement = CCJSqlParserUtil.parse(sql, p -> p.withUnsupportedStatements(true));
        assertInstanceOf(UnsupportedStatement.class, statement);
        assertEquals(sql.replace(";", " ;"), statement.toString());
    }

    @Test
    void lexicalErrorsAreNotSwallowed() {
        assertThrowsExactly(JSQLParserException.class, () -> CCJSqlParserUtil.parse(
                "UPDATE t SET a = 'unterminated", p -> p.withUnsupportedStatements(true)));
    }

    @Test
    void directSingleStatementDoesNotRecover() {
        CCJSqlParser parser = CCJSqlParserUtil.newParser("UPDATE t SET")
                .withUnsupportedStatements(true).withErrorRecovery(true);
        assertThrowsExactly(ParseException.class, parser::SingleStatement);
    }
}
