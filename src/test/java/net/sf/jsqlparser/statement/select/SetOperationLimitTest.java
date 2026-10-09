/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class SetOperationLimitTest {

    @ParameterizedTest
    @MethodSource("globalLimitCases")
    void trailingLimitBelongsToSetOperation(String sql, String rowCount, String limitOffset,
            String offset) throws JSQLParserException {
        SetOperationList select = (SetOperationList) assertSqlCanBeParsedAndDeparsed(sql);
        assertNotNull(select.getLimit(), sql);
        assertEquals(rowCount, select.getLimit().getRowCount().toString());
        assertEquals(limitOffset, select.getLimit().getOffset() == null ? null
                : select.getLimit().getOffset().toString());
        assertEquals(offset, select.getOffset() == null ? null
                : select.getOffset().getOffset().toString());
        Select last = select.getSelects().get(select.getSelects().size() - 1);
        assertNull(last.getLimit());
        assertNull(last.getOffset());
        if (sql.contains("ORDER BY")) {
            assertNotNull(select.getOrderByElements());
            assertEquals(1, select.getOrderByElements().size());
            assertNull(last.getOrderByElements());
        }
    }

    static Stream<Arguments> globalLimitCases() {
        return Stream.of(
                Arguments.of("SELECT a FROM t1 UNION SELECT b FROM t2 LIMIT 1", "1", null, null),
                Arguments.of("SELECT a FROM t1 UNION ALL SELECT b FROM t2 LIMIT 1", "1", null,
                        null),
                Arguments.of("SELECT a FROM t1 INTERSECT SELECT b FROM t2 LIMIT 1", "1", null,
                        null),
                Arguments.of("SELECT a FROM t1 EXCEPT SELECT b FROM t2 LIMIT 1", "1", null, null),
                Arguments.of("SELECT a FROM t1 MINUS SELECT b FROM t2 LIMIT 1", "1", null, null),
                Arguments.of("SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 LIMIT 1", "1", null,
                        null),
                Arguments.of("WITH t AS (SELECT 1 AS a) SELECT a FROM t UNION SELECT 2 LIMIT 1",
                        "1", null, null),
                Arguments.of("SELECT a FROM t1 UNION (SELECT b FROM t2) LIMIT 1", "1", null, null),
                Arguments.of("SELECT a FROM t1 UNION SELECT b FROM t2 ORDER BY b LIMIT 1", "1",
                        null, null),
                Arguments.of(
                        "SELECT id FROM table1 UNION SELECT id FROM table2 ORDER BY id ASC LIMIT 55",
                        "55", null, null),
                Arguments.of(
                        "SELECT * FROM table1 UNION SELECT * FROM table2 ORDER BY col LIMIT 4 OFFSET 5",
                        "4", null, "5"),
                Arguments.of("SELECT a FROM t1 UNION SELECT b FROM t2 LIMIT (1 + 2)", "(1 + 2)",
                        null, null),
                Arguments.of("SELECT a FROM t1 UNION SELECT b FROM t2 LIMIT ? OFFSET ?", "?", null,
                        "?"),
                Arguments.of("SELECT a FROM t1 UNION SELECT b FROM t2 LIMIT :cap OFFSET :skip",
                        ":cap", null, ":skip"),
                Arguments.of("SELECT a FROM t1 UNION SELECT b FROM t2 LIMIT ALL OFFSET 2", "ALL",
                        null, "2"),
                Arguments.of("SELECT a FROM t1 UNION SELECT b FROM t2 LIMIT NULL", "NULL", null,
                        null),
                Arguments.of("SELECT a FROM t1 UNION SELECT b FROM t2 LIMIT 2, 3", "3", "2", null),
                Arguments.of("SELECT a FROM t1 UNION SELECT b FROM t2 LIMIT 3 OFFSET 2", "3", null,
                        "2"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT a FROM t1 UNION SELECT b FROM t2 OFFSET 2",
            "SELECT a FROM t1 UNION SELECT b FROM t2 OFFSET 2 ROWS FETCH NEXT 1 ROWS ONLY",
            "SELECT a FROM t1 UNION SELECT b FROM t2 ORDER BY b OFFSET 2 ROWS FETCH NEXT 1 ROWS ONLY"
    })
    void trailingOffsetBelongsToSetOperation(String sql) throws JSQLParserException {
        SetOperationList select = (SetOperationList) assertSqlCanBeParsedAndDeparsed(sql);
        assertNotNull(select.getOffset());
        assertEquals(new LongValue(2), select.getOffset().getOffset());
        assertNull(select.getSelects().get(1).getOffset());
        if (sql.contains("FETCH")) {
            assertEquals(1, select.getFetch().getRowCount());
            assertNull(select.getSelects().get(1).getFetch());
        }
    }

    @Test
    void fetchWithoutOffsetStillBelongsToSetOperation() throws JSQLParserException {
        SetOperationList select = (SetOperationList) assertSqlCanBeParsedAndDeparsed(
                "SELECT a FROM t1 UNION SELECT b FROM t2 FETCH FIRST 1 ROWS ONLY");
        assertEquals(1, select.getFetch().getRowCount());
        assertNull(select.getSelects().get(1).getFetch());
    }

    @Test
    void plainSelectKeepsItsLimitAndOffset() throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT a FROM t LIMIT 1 OFFSET 2");
        assertEquals(new LongValue(1), select.getLimit().getRowCount());
        assertEquals(new LongValue(2), select.getOffset().getOffset());
    }

    @ParameterizedTest
    @ValueSource(strings = {"LIMIT 1", "LIMIT 1 OFFSET 2", "ORDER BY b LIMIT 1 OFFSET 2",
            "OFFSET 2 ROWS FETCH FIRST 1 ROWS ONLY"})
    void parenthesizedBranchKeepsItsOwnClauses(String clauses) throws JSQLParserException {
        SetOperationList select = (SetOperationList) assertSqlCanBeParsedAndDeparsed(
                "SELECT a FROM t1 UNION (SELECT b FROM t2 " + clauses + ")");
        assertNull(select.getLimit());
        assertNull(select.getOffset());
        assertNull(select.getFetch());
        assertNull(select.getOrderByElements());
        PlainSelect branch = ((ParenthesedSelect) select.getSelects().get(1)).getPlainSelect();
        assertEquals("SELECT b FROM t2 " + clauses, branch.toString());
        if (clauses.contains("LIMIT")) {
            assertEquals(new LongValue(1), branch.getLimit().getRowCount());
        }
        if (clauses.contains("OFFSET")) {
            assertEquals(new LongValue(2), branch.getOffset().getOffset());
        }
        if (clauses.contains("FETCH")) {
            assertEquals(1, branch.getFetch().getRowCount());
        }
    }

    @Test
    void branchAndSetOperationCanBothHaveLimits() throws JSQLParserException {
        SetOperationList select = (SetOperationList) assertSqlCanBeParsedAndDeparsed(
                "(SELECT 1 LIMIT 1) UNION ALL (SELECT 2 LIMIT 2 OFFSET 1) LIMIT 3 OFFSET 4");
        assertEquals(new LongValue(3), select.getLimit().getRowCount());
        assertEquals(new LongValue(4), select.getOffset().getOffset());
        assertEquals(new LongValue(1),
                ((ParenthesedSelect) select.getSelects().get(0)).getSelect().getLimit()
                        .getRowCount());
        Select last = ((ParenthesedSelect) select.getSelects().get(1)).getSelect();
        assertEquals(new LongValue(2), last.getLimit().getRowCount());
        assertEquals(new LongValue(1), last.getOffset().getOffset());
    }

    @Test
    void nestedSetOperationLimitsStayInTheirOwnScope() throws JSQLParserException {
        SetOperationList select = (SetOperationList) assertSqlCanBeParsedAndDeparsed(
                "SELECT 1 UNION ALL (SELECT 2 UNION ALL SELECT 3 LIMIT 1) LIMIT 2");
        assertEquals(new LongValue(2), select.getLimit().getRowCount());
        SetOperationList inner =
                ((ParenthesedSelect) select.getSelects().get(1)).getSetOperationList();
        assertEquals(new LongValue(1), inner.getLimit().getRowCount());
        assertNull(inner.getSelects().get(1).getLimit());
    }

    @Test
    void rewritingGlobalLimitChangesWholeResult() throws Exception {
        SetOperationList select = (SetOperationList) CCJSqlParserUtil.parse(
                "SELECT 1 UNION ALL SELECT 2 LIMIT 1");
        assertEquals(1, rowCount(select.toString()));
        assertNotNull(select.getLimit());
        select.getLimit().setRowCount(new LongValue(0));
        assertEquals(0, rowCount(select.toString()));
    }

    @Test
    void rewritingLocalLimitKeepsOtherBranchRows() throws Exception {
        SetOperationList select = (SetOperationList) CCJSqlParserUtil.parse(
                "SELECT 1 UNION ALL (SELECT 2 LIMIT 1)");
        assertEquals(2, rowCount(select.toString()));
        Select branch = ((ParenthesedSelect) select.getSelects().get(1)).getSelect();
        branch.getLimit().setRowCount(new LongValue(0));
        assertNull(select.getLimit());
        assertEquals(1, rowCount(select.toString()));
    }

    private static int rowCount(String sql) throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:");
                java.sql.Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            int count = 0;
            while (resultSet.next()) {
                count++;
            }
            return count;
        }
    }
}
