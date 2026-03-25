/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Regression tests for EXCEPT/MINUS ALL/DISTINCT modifier handling.
 * <p>
 * Verifies that the ALL and DISTINCT modifiers are correctly preserved during parse-toString
 * round-trips for all set operation types: UNION, INTERSECT, EXCEPT, and MINUS.
 *
 * @see <a href="https://github.com/JSQLParser/JSqlParser/issues/2419">#2419</a>
 */
@Execution(ExecutionMode.CONCURRENT)
public class SetOperationModifierTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT a FROM t1 EXCEPT ALL SELECT a FROM t2",
            "SELECT a FROM t1 EXCEPT DISTINCT SELECT a FROM t2",
            "SELECT a FROM t1 EXCEPT SELECT a FROM t2",
            "SELECT a FROM t1 MINUS ALL SELECT a FROM t2",
            "SELECT a FROM t1 MINUS DISTINCT SELECT a FROM t2",
            "SELECT a FROM t1 MINUS SELECT a FROM t2",
            "SELECT a FROM t1 UNION ALL SELECT a FROM t2",
            "SELECT a FROM t1 INTERSECT ALL SELECT a FROM t2",
            "SELECT a FROM t1 UNION ALL SELECT b FROM t2 EXCEPT DISTINCT SELECT c FROM t3"
    })
    void testSetOperationModifierRoundTrip(String sql) throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(sql);
    }

    @ParameterizedTest
    @MethodSource("provideModifierLeakCases")
    void testModifierDoesNotLeakBetweenOperators(String sql, String forbidden)
            throws JSQLParserException {
        Statement stmt = CCJSqlParserUtil.parse(sql);
        String deparsed = stmt.toString();
        assertFalse(deparsed.contains(forbidden),
                "Modifier leaked: found '" + forbidden + "' in: " + deparsed);
    }

    private static Stream<Arguments> provideModifierLeakCases() {
        return Stream.of(
                Arguments.of(
                        "SELECT a FROM t1 UNION ALL SELECT b FROM t2 EXCEPT SELECT c FROM t3",
                        "EXCEPT ALL"),
                Arguments.of(
                        "SELECT a FROM t1 INTERSECT ALL SELECT b FROM t2 UNION SELECT c FROM t3",
                        "UNION ALL"));
    }

    @ParameterizedTest
    @MethodSource("provideSetOperationObjectCases")
    void testSetOperationObjectState(String sql, Class<?> expectedType,
            boolean expectedAll, boolean expectedDistinct) throws JSQLParserException {
        SetOperationList setOpList = (SetOperationList) CCJSqlParserUtil.parse(sql);
        SetOperation op = setOpList.getOperations().get(0);
        assertInstanceOf(expectedType, op);
        assertEquals(expectedAll, op.isAll(),
                "isAll() mismatch for: " + sql);
        assertEquals(expectedDistinct, op.isDistinct(),
                "isDistinct() mismatch for: " + sql);
    }

    private static Stream<Arguments> provideSetOperationObjectCases() {
        return Stream.of(
                Arguments.of("SELECT a FROM t1 EXCEPT ALL SELECT a FROM t2",
                        ExceptOp.class, true, false),
                Arguments.of("SELECT a FROM t1 MINUS ALL SELECT a FROM t2",
                        MinusOp.class, true, false));
    }
}
