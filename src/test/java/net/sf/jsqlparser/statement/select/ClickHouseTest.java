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
import net.sf.jsqlparser.expression.ColumnsExpression;
import net.sf.jsqlparser.expression.ColumnsTransformer;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LambdaExpression;
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

    @Test
    public void testColumnsApplyLambdaIssue2631() throws JSQLParserException {
        // ClickHouse applies a function or lambda expression to all columns matching
        // the regular expression: https://github.com/JSQLParser/JSqlParser/issues/2631
        String sql = "SELECT COLUMNS('^metric_') APPLY(x -> round(x, 2)) FROM metrics";
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        ColumnsExpression columnsExpression = Assertions.assertInstanceOf(ColumnsExpression.class,
                plainSelect.getSelectItems().get(0).getExpression());
        Assertions.assertEquals(1, columnsExpression.getTransformers().size());
        ColumnsTransformer transformer = columnsExpression.getTransformers().get(0);
        Assertions.assertEquals(ColumnsTransformer.ColumnsTransformerType.APPLY,
                transformer.getType());
        Assertions.assertInstanceOf(LambdaExpression.class, transformer.getApplyExpression());
        assertSqlCanBeParsedAndDeparsed(sql, true);
    }

    @Test
    public void testColumnsApplyFunctionName() throws JSQLParserException {
        // chained APPLY modifiers, taken from the ClickHouse SELECT documentation
        String sql = "SELECT COLUMNS('[jk]') APPLY(toString) APPLY(length) APPLY(max)"
                + " FROM columns_transformers";
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        ColumnsExpression columnsExpression = Assertions.assertInstanceOf(ColumnsExpression.class,
                plainSelect.getSelectItems().get(0).getExpression());
        Assertions.assertEquals(3, columnsExpression.getTransformers().size());
        assertSqlCanBeParsedAndDeparsed(sql, true);
    }

    @Test
    public void testColumnsExcept() throws JSQLParserException {
        String sql = "SELECT COLUMNS('^metric_') EXCEPT (metric_disk) FROM metrics";
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        ColumnsExpression columnsExpression = Assertions.assertInstanceOf(ColumnsExpression.class,
                plainSelect.getSelectItems().get(0).getExpression());
        Assertions.assertEquals(ColumnsTransformer.ColumnsTransformerType.EXCEPT,
                columnsExpression.getTransformers().get(0).getType());
        assertSqlCanBeParsedAndDeparsed(sql, true);
    }

    @Test
    public void testColumnsReplace() throws JSQLParserException {
        String sql =
                "SELECT COLUMNS('^metric_') REPLACE(metric_cpu * 100 AS metric_cpu) FROM metrics";
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        ColumnsExpression columnsExpression = Assertions.assertInstanceOf(ColumnsExpression.class,
                plainSelect.getSelectItems().get(0).getExpression());
        Assertions.assertEquals(ColumnsTransformer.ColumnsTransformerType.REPLACE,
                columnsExpression.getTransformers().get(0).getType());
        assertSqlCanBeParsedAndDeparsed(sql, true);
    }

    @Test
    public void testColumnsCombinedTransformers() throws JSQLParserException {
        // ClickHouse parses its transformers in a loop, so they combine in any order
        String sql =
                "SELECT COLUMNS('m') APPLY(x -> round(x, 2)) EXCEPT (metric_disk) FROM metrics";
        assertSqlCanBeParsedAndDeparsed(sql, true);

        sql = "SELECT COLUMNS('m') EXCEPT (metric_disk) APPLY(x -> round(x, 2)) FROM metrics";
        assertSqlCanBeParsedAndDeparsed(sql, true);
    }

    @Test
    public void testColumnsApplyMultiParamLambda() throws JSQLParserException {
        String sql = "SELECT COLUMNS('m') APPLY((k, v) -> v > 5) FROM metrics";
        assertSqlCanBeParsedAndDeparsed(sql, true);
    }

    @Test
    public void testBareColumnsExpressionStaysFunction() throws JSQLParserException {
        // without a transformer, COLUMNS(...) keeps parsing as a regular function
        String sql = "SELECT COLUMNS('^metric_') FROM metrics";
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        Assertions.assertInstanceOf(Function.class,
                plainSelect.getSelectItems().get(0).getExpression());
        assertSqlCanBeParsedAndDeparsed(sql, true);
    }

    @Test
    public void testColumnsItemFollowedBySetOperation() throws JSQLParserException {
        // a follower keyword without its parenthesis never starts a transformer, so
        // EXCEPT stays a set operation (a bare APPLY or REPLACE stays an alias)
        assertSqlCanBeParsedAndDeparsed("SELECT COLUMNS('m') EXCEPT SELECT b FROM t", true);
        assertSqlCanBeParsedAndDeparsed("SELECT COLUMNS('m') EXCEPT ALL SELECT b FROM t", true);
        assertSqlCanBeParsedAndDeparsed("SELECT a, COLUMNS('m') EXCEPT SELECT b FROM t", true);
        assertSqlCanBeParsedAndDeparsed("SELECT COLUMNS('m') EXCEPT (SELECT b FROM t)", true);
        // parenthesized set operation operands that do not start a column list
        assertSqlCanBeParsedAndDeparsed("SELECT COLUMNS('m') EXCEPT ((SELECT b FROM t))", true);
        assertSqlCanBeParsedAndDeparsed("SELECT COLUMNS('m') EXCEPT (VALUES (1, 2))", true);
    }

    @Test
    public void testBareTransformerKeywordRemainsAlias() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT COLUMNS('m') APPLY FROM t", true);
        assertSqlCanBeParsedAndDeparsed("SELECT COLUMNS('m') REPLACE FROM t", true);
        assertSqlCanBeParsedAndDeparsed("SELECT COLUMNS('m') APPLY", true);
    }

    @Test
    public void testAliasColumnsBodyRemainsAlias() throws JSQLParserException {
        // T(name[type], ...) is the alias-columns reading of the regular alias path,
        // not a transformer body (APPLY takes a function name or a lambda in ClickHouse)
        assertSqlCanBeParsedAndDeparsed("SELECT COLUMNS('m') APPLY(a, b) FROM t", true);
        assertSqlCanBeParsedAndDeparsed("SELECT COLUMNS('m') APPLY(a, b)", true);
        assertSqlCanBeParsedAndDeparsed("SELECT COLUMNS('m') APPLY(a INT) FROM t", true);
        assertSqlCanBeParsedAndDeparsed("SELECT COLUMNS('m') APPLY(`a`, `b`) FROM t", true);
        assertSqlCanBeParsedAndDeparsed(
                "SELECT COLUMNS('m') REPLACE(a INT, b VARCHAR(10)) FROM t", true);
    }

    @Test
    public void testReplaceWithBareIdentifierAsAlias() throws JSQLParserException {
        // the plain ClickHouse rename form: REPLACE(column AS alias)
        assertSqlCanBeParsedAndDeparsed("SELECT COLUMNS('m') REPLACE(b AS c) FROM t", true);
        assertSqlCanBeParsedAndDeparsed("SELECT COLUMNS('m') REPLACE(b AS c, d AS e) FROM t", true);
        // keyword-predicate lambda bodies stay on the transformer reading
        assertSqlCanBeParsedAndDeparsed("SELECT COLUMNS('m') APPLY(x IS NULL) FROM t", true);
        assertSqlCanBeParsedAndDeparsed("SELECT COLUMNS('m') APPLY(x AND y) FROM t", true);
    }

    @Test
    public void testTypedAliasColumnWithCompoundTimeZoneType() throws JSQLParserException {
        // compound timezone types are lexed as a single DT_ZONE token but are still
        // alias-columns types, so the input belongs to the alias path
        assertSqlCanBeParsedAndDeparsed(
                "SELECT COLUMNS('m') APPLY(a TIMESTAMP WITH TIME ZONE) FROM t", true);
        assertSqlCanBeParsedAndDeparsed(
                "SELECT COLUMNS('m') APPLY(a TIMESTAMP(3) WITH TIME ZONE) FROM t", true);
        assertSqlCanBeParsedAndDeparsed(
                "SELECT COLUMNS('m') APPLY(a TIMESTAMP WITHOUT TIME ZONE) FROM t", true);
    }
}
