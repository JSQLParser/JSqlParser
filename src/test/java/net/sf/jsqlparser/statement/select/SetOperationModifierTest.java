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
import org.junit.jupiter.api.Test;
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

    /**
     * The grammar records the modifier verbatim from the source, so the quantifier survives in
     * whatever case it was written. Every case spells the same set operation.
     *
     * @see <a href="https://github.com/JSQLParser/JSqlParser/issues/2419">#2419</a>
     */
    @ParameterizedTest
    @ValueSource(strings = {"ALL", "all", "All", "aLL"})
    void testAllModifierRecognisedInAnyCase(String quantifier) throws JSQLParserException {
        SetOperation operation = firstOperation(
                "SELECT a FROM t1 UNION " + quantifier + " SELECT a FROM t2");
        assertTrue(operation.isAll(), "UNION " + quantifier + " should be an ALL union");
        assertFalse(operation.isDistinct(), "UNION " + quantifier + " is not DISTINCT");
    }

    @ParameterizedTest
    @ValueSource(strings = {"DISTINCT", "distinct", "Distinct"})
    void testDistinctModifierRecognisedInAnyCase(String quantifier) throws JSQLParserException {
        SetOperation operation = firstOperation(
                "SELECT a FROM t1 UNION " + quantifier + " SELECT a FROM t2");
        assertTrue(operation.isDistinct(), "UNION " + quantifier + " should be a DISTINCT union");
        assertFalse(operation.isAll(), "UNION " + quantifier + " is not ALL");
    }

    @ParameterizedTest
    @ValueSource(strings = {"EXCEPT", "INTERSECT", "MINUS"})
    void testAllModifierRecognisedInAnyCaseForEverySetOperation(String setOperation)
            throws JSQLParserException {
        assertTrue(firstOperation("SELECT a FROM t1 " + setOperation + " all SELECT a FROM t2")
                .isAll(), setOperation + " all should be an ALL operation");
    }

    /**
     * A plain set operation carries no quantifier, and neither predicate may claim one.
     */
    @Test
    void testUnqualifiedSetOperationIsNeitherAllNorDistinct() throws JSQLParserException {
        SetOperation operation = firstOperation("SELECT a FROM t1 UNION SELECT a FROM t2");
        assertFalse(operation.isAll());
        assertFalse(operation.isDistinct());
    }

    /**
     * The setters record what they are told rather than the keyword they are named after.
     */
    @Test
    void testSettersHonourTheirArgument() {
        UnionOp union = new UnionOp();

        union.setAll(true);
        assertTrue(union.isAll());
        union.setAll(false);
        assertFalse(union.isAll(), "setAll(false) must not leave an ALL modifier behind");

        union.setDistinct(true);
        assertTrue(union.isDistinct());
        union.setDistinct(false);
        assertFalse(union.isDistinct(), "setDistinct(false) must not leave a DISTINCT modifier");
    }

    private static SetOperation firstOperation(String sql) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        assertInstanceOf(SetOperationList.class, statement);
        return ((SetOperationList) statement).getOperations().get(0);
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
