/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserConstants;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.parser.Token;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

class WithItemTest {

    @ParameterizedTest
    @CsvSource({"2047, 1", "2047, -1", "2048, 1", "4096, 1"})
    void testLargeExpressionAlias(int arguments, String firstArgument) throws JSQLParserException {
        String expression = "coalesce(" + firstArgument + ", 1".repeat(arguments - 1) + ")";
        Select select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "WITH " + expression + " AS v, t AS (SELECT v) SELECT v FROM t", true);
        WithItem<?> item = select.getWithItemsList().get(0);
        Function function = assertInstanceOf(Function.class, item.getExpression());
        assertEquals("v", item.getAliasName());
        assertNull(item.getParenthesedStatement());
        assertEquals("coalesce", function.getName());
        assertEquals(arguments, function.getParameters().size());
        assertEquals(firstArgument, function.getParameters().get(0).toString());
        assertEquals("1", function.getParameters().get(arguments - 1).toString());
        assertNotNull(select.getWithItemsList().get(1).getParenthesedStatement());
    }

    @ParameterizedTest
    @ValueSource(ints = {2047, 2048})
    void testLargeCteColumnList(int columns) throws JSQLParserException {
        String names = IntStream.range(0, columns).mapToObj(i -> "c" + i)
                .collect(Collectors.joining(", "));
        Select select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "WITH t(" + names + ") AS (SELECT 1" + ", 1".repeat(columns - 1)
                        + ") SELECT * FROM t",
                true);
        WithItem<?> item = select.getWithItemsList().get(0);
        assertEquals("t", item.getAliasName());
        assertNull(item.getExpression());
        assertNotNull(item.getParenthesedStatement());
        assertEquals(columns, item.getWithItemList().size());
        assertEquals("c" + (columns - 1), item.getWithItemList().get(columns - 1).toString());
    }

    @Test
    void testLargeNestedExpressionAlias() throws JSQLParserException {
        String expression = "concat(concat('(', ')')" + ", 'x'".repeat(2047) + ")";
        Select select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "WITH " + expression + " AS v SELECT v", true);
        Function function = assertInstanceOf(Function.class,
                select.getWithItemsList().get(0).getExpression());
        assertEquals(2048, function.getParameters().size());
        assertEquals("concat('(', ')')", function.getParameters().get(0).toString());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 64, 65})
    void testQualifiedExpressionAlias(int qualifiers) throws JSQLParserException {
        String name = "s.".repeat(qualifiers) + "coalesce";
        Select select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(
                "WITH " + name + "(1, 2) AS v SELECT v", true);
        Function function = assertInstanceOf(Function.class,
                select.getWithItemsList().get(0).getExpression());
        assertEquals(name, function.getName());
        assertEquals(2, function.getParameters().size());
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 2048})
    @Timeout(5)
    void testUnclosedExpressionAlias(int arguments) {
        String sql = "WITH coalesce(1" + ", 1".repeat(arguments - 1);
        assertThrows(ParseException.class, () -> CCJSqlParserUtil.newParser(sql).Statement());
    }

    @Test
    void testInterruptedWithItemLookaheadStopsReading() {
        CCJSqlParser parser = CCJSqlParserUtil.newParser(
                "WITH coalesce(1" + ", 1".repeat(512) + ") AS v SELECT v");
        AtomicBoolean reachedAlias = new AtomicBoolean();
        parser.token_source = Mockito.spy(parser.token_source);
        Mockito.doAnswer(invocation -> {
            Token token = (Token) invocation.callRealMethod();
            if (token.kind == CCJSqlParserConstants.S_LONG) {
                parser.interrupted = true;
            } else if (token.kind == CCJSqlParserConstants.K_AS) {
                reachedAlias.set(true);
            }
            return token;
        }).when(parser.token_source).getNextToken();
        assertThrows(CancellationException.class, parser::Statement);
        assertFalse(reachedAlias.get());
    }

    @Test
    void testNotMaterializedIssue2251() throws JSQLParserException {
        String sqlStr = "WITH devices AS NOT MATERIALIZED (\n"
                + "  SELECT\n"
                + "    d.uuid AS device_uuid\n"
                + "  FROM active_devices d\n"
                + ")\n"
                + "SELECT 1;";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "WITH\n" +
                    "  FUNCTION doubleup(x integer)\n" +
                    "    RETURNS integer\n" +
                    "    RETURN x * 2\n" +
                    "SELECT doubleup(21);\n",
            "WITH\n" +
                    "  FUNCTION doubleup(x integer)\n" +
                    "    RETURNS integer\n" +
                    "    RETURN x * 2,\n" +
                    "  FUNCTION doubleupplusone(x integer)\n" +
                    "    RETURNS integer\n" +
                    "    RETURN doubleup(x) + 1\n" +
                    "SELECT doubleupplusone(21);",
            "WITH\n" +
                    "  FUNCTION takesArray(x array<double>)\n" +
                    "    RETURNS double\n" +
                    "    RETURN x[1] + x[2] + x[3]\n" +
                    "SELECT takesArray(ARRAY[1.0, 2.0, 3.0]);"

    })
    void testWithFunction(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testRecursiveWithSearchBreadthClause() throws JSQLParserException {
        String sqlStr = "WITH RECURSIVE team_hierarchy AS (\n"
                + "    SELECT employee_id, first_name, manager_id, ARRAY[employee_id] AS path\n"
                + "    FROM employees\n"
                + "    WHERE manager_id IS NULL\n"
                + "    UNION ALL\n"
                + "    SELECT e.employee_id, e.first_name, e.manager_id, th.path || e.employee_id\n"
                + "    FROM employees e\n"
                + "    INNER JOIN team_hierarchy th ON e.manager_id = th.employee_id\n"
                + ")\n"
                + "SEARCH BREADTH FIRST BY employee_id SET order_col\n"
                + "SELECT employee_id, first_name, path, order_col FROM team_hierarchy ORDER BY order_col";

        Select select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        WithSearchClause searchClause = select.getWithItemsList().get(0).getSearchClause();

        assertNotNull(searchClause);
        assertEquals(WithSearchClause.SearchOrder.BREADTH, searchClause.getSearchOrder());
        assertEquals("employee_id", searchClause.getSearchColumns().get(0).toString());
        assertEquals("order_col", searchClause.getSequenceColumnName());
    }

    @Test
    void testRecursiveWithSearchDepthClause() throws JSQLParserException {
        String sqlStr = "WITH RECURSIVE search_tree AS (\n"
                + "    SELECT id, parent_id FROM nodes WHERE parent_id IS NULL\n"
                + "    UNION ALL\n"
                + "    SELECT n.id, n.parent_id FROM nodes n JOIN search_tree st ON st.id = n.parent_id\n"
                + ")\n"
                + "SEARCH DEPTH FIRST BY id, parent_id SET traversal_order\n"
                + "SELECT traversal_order FROM search_tree";

        Select select = (Select) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        WithSearchClause searchClause = select.getWithItemsList().get(0).getSearchClause();

        assertNotNull(searchClause);
        assertEquals(WithSearchClause.SearchOrder.DEPTH, searchClause.getSearchOrder());
        assertEquals("id", searchClause.getSearchColumns().get(0).toString());
        assertEquals("parent_id", searchClause.getSearchColumns().get(1).toString());
        assertEquals("traversal_order", searchClause.getSequenceColumnName());
    }
}
