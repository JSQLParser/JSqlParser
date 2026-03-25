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

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Regression tests for EXCEPT/MINUS ALL/DISTINCT modifier handling.
 * <p>
 * Verifies that the ALL and DISTINCT modifiers are correctly preserved during
 * parse-toString round-trips for all set operation types: UNION, INTERSECT,
 * EXCEPT, and MINUS.
 *
 * @see <a href="https://github.com/JSQLParser/JSqlParser/issues/2080">#2080</a>
 */
@Execution(ExecutionMode.CONCURRENT)
public class SetOperationModifierTest {

    // ── EXCEPT modifier tests ─────────────────────────────────────

    @Test
    public void testExceptAllRoundTrip() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT a FROM t1 EXCEPT ALL SELECT a FROM t2");
    }

    @Test
    public void testExceptDistinctRoundTrip() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT a FROM t1 EXCEPT DISTINCT SELECT a FROM t2");
    }

    @Test
    public void testPlainExceptRoundTrip() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT a FROM t1 EXCEPT SELECT a FROM t2");
    }

    // ── MINUS modifier tests ─────────────────────────────────────

    @Test
    public void testMinusAllRoundTrip() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT a FROM t1 MINUS ALL SELECT a FROM t2");
    }

    @Test
    public void testMinusDistinctRoundTrip() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT a FROM t1 MINUS DISTINCT SELECT a FROM t2");
    }

    @Test
    public void testPlainMinusRoundTrip() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT a FROM t1 MINUS SELECT a FROM t2");
    }

    // ── Cross-check: UNION and INTERSECT still work ──────────────

    @Test
    public void testUnionAllRoundTrip() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT a FROM t1 UNION ALL SELECT a FROM t2");
    }

    @Test
    public void testIntersectAllRoundTrip() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT a FROM t1 INTERSECT ALL SELECT a FROM t2");
    }

    // ── Modifier leak prevention: modifiers must not bleed across operators ──

    @Test
    public void testModifierDoesNotLeakFromUnionAllToExcept() throws JSQLParserException {
        String sql = "SELECT a FROM t1 UNION ALL SELECT b FROM t2 EXCEPT SELECT c FROM t3";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        String result = stmt.toString();
        // EXCEPT must NOT inherit ALL from the preceding UNION ALL
        assertFalse(result.contains("EXCEPT ALL"),
                "Modifier should not leak from UNION ALL to a subsequent plain EXCEPT: " + result);
    }

    @Test
    public void testModifierDoesNotLeakFromIntersectAllToUnion() throws JSQLParserException {
        String sql = "SELECT a FROM t1 INTERSECT ALL SELECT b FROM t2 UNION SELECT c FROM t3";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        String result = stmt.toString();
        assertFalse(result.contains("UNION ALL"),
                "Modifier should not leak from INTERSECT ALL to a subsequent plain UNION: " + result);
    }

    @Test
    public void testMixedModifiersPreserved() throws JSQLParserException {
        // UNION ALL followed by EXCEPT DISTINCT
        assertSqlCanBeParsedAndDeparsed(
                "SELECT a FROM t1 UNION ALL SELECT b FROM t2 EXCEPT DISTINCT SELECT c FROM t3");
    }

    // ── SetOperation object state verification ──────────────────

    @Test
    public void testExceptAllSetOperationObject() throws JSQLParserException {
        String sql = "SELECT a FROM t1 EXCEPT ALL SELECT a FROM t2";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        SetOperationList setOpList = (SetOperationList) stmt;
        SetOperation op = setOpList.getOperation(0);

        assertInstanceOf(ExceptOp.class, op);
        assertTrue(op.isAll(), "ExceptOp should report isAll() == true");
        assertFalse(op.isDistinct(), "ExceptOp should report isDistinct() == false");
    }

    @Test
    public void testMinusAllSetOperationObject() throws JSQLParserException {
        String sql = "SELECT a FROM t1 MINUS ALL SELECT a FROM t2";
        Statement stmt = CCJSqlParserUtil.parse(sql);
        SetOperationList setOpList = (SetOperationList) stmt;
        SetOperation op = setOpList.getOperation(0);

        assertInstanceOf(MinusOp.class, op);
        assertTrue(op.isAll(), "MinusOp should report isAll() == true");
    }
}
