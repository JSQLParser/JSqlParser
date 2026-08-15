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

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClickHouseTest {

    @Test
    public void testGlobalJoin() throws JSQLParserException {
        String sql =
                "SELECT a.*,b.* from lineorder_all as a  global left join supplier_all as b on a.LOLINENUMBER=b.SSUPPKEY";
        assertSqlCanBeParsedAndDeparsed(sql, true);
    }

    @Test
    public void testGlobalAnyLeftJoin() throws JSQLParserException {
        String sql = "SELECT * FROM events e GLOBAL ANY LEFT JOIN users u ON e.user_id = u.id";
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        Join join = select.getJoins().get(0);
        Assertions.assertTrue(join.isGlobal());
        Assertions.assertTrue(join.isAny());
        Assertions.assertTrue(join.isLeft());
    }

    @Test
    public void testGlobalAllRightJoin() throws JSQLParserException {
        String sql = "SELECT * FROM events e GLOBAL ALL RIGHT JOIN users u ON e.user_id = u.id";
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        Join join = select.getJoins().get(0);
        Assertions.assertTrue(join.isGlobal());
        Assertions.assertTrue(join.isAll());
        Assertions.assertTrue(join.isRight());
    }

    @Test
    public void testLeftAnyJoinOrderVariant() throws JSQLParserException {
        String sql = "SELECT * FROM events e LEFT ANY JOIN users u ON e.user_id = u.id";
        Select statement = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect select = (PlainSelect) statement.getSelectBody();
        Join join = select.getJoins().get(0);
        Assertions.assertTrue(join.isAny());
        Assertions.assertTrue(join.isLeft());
    }

    @Test
    public void testRightAllJoinOrderVariant() throws JSQLParserException {
        String sql = "SELECT * FROM events e RIGHT ALL JOIN users u ON e.user_id = u.id";
        Select statement = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect select = (PlainSelect) statement.getSelectBody();
        Join join = select.getJoins().get(0);
        Assertions.assertTrue(join.isAll());
        Assertions.assertTrue(join.isRight());
    }

    @Test
    public void testFunctionWithAttributesIssue1742() throws JSQLParserException {
        String sql = "SELECT f1(arguments).f2.f3 from dual";
        assertSqlCanBeParsedAndDeparsed(sql, true);

        sql = "SELECT f1(arguments).f2(arguments).f3.f4 from dual";
        assertSqlCanBeParsedAndDeparsed(sql, true);

        sql = "SELECT schemaName.f1(arguments).f2(arguments).f3.f4 from dual";
        assertSqlCanBeParsedAndDeparsed(sql, true);
    }

    @Test
    public void testTuplePositionalAccessIssue2442() throws JSQLParserException {
        String sql = "SELECT tuple(1, 2, 3).2 FROM tuple_demo";
        assertSqlCanBeParsedAndDeparsed(sql, true);
    }

    @Test
    public void testGlobalIn() throws JSQLParserException {
        String sql =
                "SELECT lo_linenumber,lo_orderkey from lo_linenumber where lo_linenumber global in (1,2,3)";
        assertSqlCanBeParsedAndDeparsed(sql, true);
    }

    @Test
    public void testGlobalKeywordIssue1883() throws JSQLParserException {
        String sqlStr = "select a.* from  a global join  b on a.name = b.name ";
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        Assertions.assertTrue(select.getJoins().get(0).isGlobal());
    }

    @Test
    public void testPreWhereClause() throws JSQLParserException {
        String sqlStr = "SELECT * FROM table1 PREWHERE column_name = 'value'";
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        Assertions.assertNotNull(select.getPreWhere());
        Assertions.assertNull(select.getWhere());
    }

    @Test
    public void testPreWhereWithWhereClause() throws JSQLParserException {
        String sqlStr =
                "SELECT * FROM table1 PREWHERE column_name = 'value' WHERE id > 10";
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        Assertions.assertNotNull(select.getPreWhere());
        Assertions.assertNotNull(select.getWhere());
    }

    @Test
    public void testParameterizedAggregateFunctionIssue2125() throws JSQLParserException {
        String sql =
                "SELECT toStartOfDay(timestamp) AS date, count(1) AS count, quantile(0.95)(cost) AS cost95 FROM apm_log_event";
        Select select = (Select) assertSqlCanBeParsedAndDeparsed(sql, true);

        Function function = ((PlainSelect) select.getSelectBody())
                .getSelectItem(2)
                .getExpression(Function.class);
        Assertions.assertNotNull(function.getParameters());
        Assertions.assertNotNull(function.getChainedParameters());
        Assertions.assertEquals(1, function.getParameters().size());
        Assertions.assertEquals(1, function.getChainedParameters().size());
    }

    @Test
    public void testSettingsClauseIssue2362() throws JSQLParserException {
        String sql = "SELECT *\nFROM events\nSETTINGS max_threads = 1";
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        Assertions.assertNotNull(select.getSettings());
        Assertions.assertEquals(1, select.getSettings().size());
        Assertions.assertEquals("max_threads = 1", select.getSettings().get(0).toString());
    }

    @Test
    public void testMultipleSettingsClauseIssue2362() throws JSQLParserException {
        String sql = "SELECT * FROM events SETTINGS max_threads = 1, max_rows_to_read = 1000";
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        Assertions.assertNotNull(select.getSettings());
        Assertions.assertEquals(2, select.getSettings().size());
    }

    @Test
    public void testOrderByWithFillIssue2467() throws JSQLParserException {
        String sql =
                "SELECT d, v FROM t ORDER BY d WITH FILL FROM toDate('2024-01-01') TO toDate('2024-02-01') STEP INTERVAL 1 DAY INTERPOLATE (v AS v + 1)";
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);

        WithFill withFill = select.getOrderByElements().get(0).getWithFill();
        Assertions.assertNotNull(withFill);
        Assertions.assertEquals("toDate('2024-01-01')", withFill.getFrom().toString());
        Assertions.assertEquals("toDate('2024-02-01')", withFill.getTo().toString());
        Assertions.assertEquals("INTERVAL 1 DAY", withFill.getStep().toString());
        Assertions.assertNull(withFill.getStaleness());

        Assertions.assertEquals(1, select.getInterpolate().size());
        Assertions.assertEquals("v", select.getInterpolate().get(0).getColumn().getColumnName());
        Assertions.assertEquals("v + 1", select.getInterpolate().get(0).getExpression().toString());
    }

    @Test
    public void testOrderByWithFillOnly() throws JSQLParserException {
        String sql = "SELECT key, value FROM t ORDER BY key WITH FILL";
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        Assertions.assertNotNull(select.getOrderByElements().get(0).getWithFill());
        Assertions.assertNull(select.getOrderByElements().get(0).getWithFill().getFrom());
    }

    @Test
    public void testOrderByWithFillFromToStep() throws JSQLParserException {
        String sql = "SELECT n FROM t ORDER BY n WITH FILL FROM 0 TO 5.51 STEP 0.5";
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        WithFill withFill = select.getOrderByElements().get(0).getWithFill();
        Assertions.assertEquals("0", withFill.getFrom().toString());
        Assertions.assertEquals("5.51", withFill.getTo().toString());
        Assertions.assertEquals("0.5", withFill.getStep().toString());
    }

    @Test
    public void testOrderByWithFillStepIntervalAndStaleness() throws JSQLParserException {
        String sql =
                "SELECT d1, d2 FROM t ORDER BY d1 WITH FILL STEP INTERVAL 1 DAY STALENESS 3, d2 WITH FILL";
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        Assertions.assertEquals(2, select.getOrderByElements().size());
        WithFill first = select.getOrderByElements().get(0).getWithFill();
        Assertions.assertEquals("INTERVAL 1 DAY", first.getStep().toString());
        Assertions.assertEquals("3", first.getStaleness().toString());
        Assertions.assertNotNull(select.getOrderByElements().get(1).getWithFill());
    }

    @Test
    public void testOrderByWithFillDescNullsLast() throws JSQLParserException {
        String sql = "SELECT d FROM t ORDER BY d DESC NULLS LAST WITH FILL TO 10";
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        OrderByElement element = select.getOrderByElements().get(0);
        Assertions.assertFalse(element.isAsc());
        Assertions.assertEquals(OrderByElement.NullOrdering.NULLS_LAST, element.getNullOrdering());
        Assertions.assertEquals("10", element.getWithFill().getTo().toString());
    }

    @Test
    public void testOrderByWithFillInterpolateVariants() throws JSQLParserException {
        // multiple items, with and without AS
        String sql = "SELECT a, b, c FROM t ORDER BY a WITH FILL INTERPOLATE (b, c AS c + 1)";
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        Assertions.assertEquals(2, select.getInterpolate().size());
        Assertions.assertNull(select.getInterpolate().get(0).getExpression());
        Assertions.assertEquals("c + 1", select.getInterpolate().get(1).getExpression().toString());

        // bare INTERPOLATE fills all allowed columns
        sql = "SELECT a, b FROM t ORDER BY a WITH FILL INTERPOLATE";
        select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        Assertions.assertNotNull(select.getInterpolate());
        Assertions.assertTrue(select.getInterpolate().isEmpty());
    }

    @Test
    public void testOrderByWithFillUnionInterpolate() throws JSQLParserException {
        String sql =
                "SELECT a FROM t1 UNION SELECT a FROM t2 ORDER BY a WITH FILL INTERPOLATE (a AS a + 1) LIMIT 10";
        Select select = (Select) assertSqlCanBeParsedAndDeparsed(sql, true);
        SetOperationList setOperationList = (SetOperationList) select.getSelectBody();
        Assertions.assertNotNull(setOperationList.getOrderByElements().get(0).getWithFill());
        Assertions.assertEquals(1, setOperationList.getInterpolate().size());
    }

    @Test
    public void testOrderByWithFillSubquery() throws JSQLParserException {
        String sql = "SELECT * FROM (SELECT d, v FROM t ORDER BY d WITH FILL FROM 0 TO 9) AS sub";
        assertSqlCanBeParsedAndDeparsed(sql, true);
    }

    @Test
    public void testFillStepInterpolateStalenessAsColumnNames() throws JSQLParserException {
        // the new keywords stay non-reserved and must remain usable as plain column names
        assertSqlCanBeParsedAndDeparsed("SELECT fill, step, staleness FROM t ORDER BY interpolate",
                true);
        assertSqlCanBeParsedAndDeparsed("SELECT interpolate FROM t WHERE fill = 1 AND step = 2",
                true);
    }

    @Test
    public void testCastToNestedParametricTypeIssue2441() throws JSQLParserException {
        // ClickHouse allows parametric (constructor-style) data types as a CAST target,
        // including nested ones such as Nullable(Decimal(p, s)).
        String sql = "SELECT CAST(x AS Nullable(Decimal(10, 2))) FROM cast_demo";
        assertSqlCanBeParsedAndDeparsed(sql, true);

        // The inner parametric type may itself be wrapped by another parametric type.
        sql = "SELECT CAST(x AS LowCardinality(Decimal(10, 2))) FROM cast_demo";
        assertSqlCanBeParsedAndDeparsed(sql, true);
    }
}
