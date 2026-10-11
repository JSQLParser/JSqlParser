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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.ColumnsExpression;
import net.sf.jsqlparser.expression.ColumnsTransformer;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LambdaExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
    public void testAllColumnsApplyIssue2636() throws JSQLParserException {
        // ClickHouse accepts the same transformers after '*':
        // https://github.com/JSQLParser/JSqlParser/issues/2636
        String sql = "SELECT * APPLY(sum) FROM t";
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        SelectItem<?> selectItem = plainSelect.getSelectItems().get(0);

        Assertions.assertNull(selectItem.getAlias(),
                "APPLY must not fall back to an alias");
        Assertions.assertInstanceOf(AllColumns.class, selectItem.getExpression());
        assertSqlCanBeParsedAndDeparsed(sql, true);
    }

    @Test
    public void testAllColumnsChainedTransformersIssue2636() throws JSQLParserException {
        // ClickHouse parses the transformers in a loop, so they may repeat after '*'
        String sql = "SELECT * APPLY(sum) EXCEPT (a) FROM t";
        assertSqlCanBeParsedAndDeparsed(sql, true);
    }

    @Test
    public void testAllColumnsExceptStrictIssue2636() throws JSQLParserException {
        // STRICT sits between the EXCEPT keyword and its column list
        // (ClickHouse ExpressionElementParsers.cpp, ParserColumnsTransformers)
        String sql = "SELECT * EXCEPT STRICT (a, b) FROM t";
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        SelectItem<?> selectItem = plainSelect.getSelectItems().get(0);
        AllColumns allColumns =
                Assertions.assertInstanceOf(AllColumns.class, selectItem.getExpression());
        ColumnsTransformer transformer = allColumns.getTransformers().get(0);
        Assertions.assertEquals(ColumnsTransformer.ColumnsTransformerType.EXCEPT,
                transformer.getType());
        Assertions.assertTrue(transformer.isStrict());
        Assertions.assertInstanceOf(ParenthesedExpressionList.class,
                transformer.getExceptColumns());
        assertSqlCanBeParsedAndDeparsed(sql, true);

        // the qualified '*' shares the same transformer production
        sql = "SELECT t.* EXCEPT STRICT (a) FROM t";
        select = (Select) CCJSqlParserUtil.parse(sql);
        plainSelect = (PlainSelect) select.getSelectBody();
        AllTableColumns allTableColumns = Assertions.assertInstanceOf(AllTableColumns.class,
                plainSelect.getSelectItems().get(0).getExpression());
        Assertions.assertTrue(allTableColumns.getTransformers().get(0).isStrict());
        assertSqlCanBeParsedAndDeparsed(sql, true);

        // the flag belongs to one transformer of the chain only
        sql = "SELECT * EXCEPT STRICT (a) REPLACE(x AS a) FROM t";
        select = (Select) CCJSqlParserUtil.parse(sql);
        plainSelect = (PlainSelect) select.getSelectBody();
        allColumns = Assertions.assertInstanceOf(AllColumns.class,
                plainSelect.getSelectItems().get(0).getExpression());
        Assertions.assertEquals(2, allColumns.getTransformers().size());
        Assertions.assertTrue(allColumns.getTransformers().get(0).isStrict());
        Assertions.assertFalse(allColumns.getTransformers().get(1).isStrict());
        assertSqlCanBeParsedAndDeparsed(sql, true);

        // the transformer survives as the second leg of a set operation
        sql = "SELECT 1 UNION SELECT * EXCEPT STRICT (a) FROM t";
        SetOperationList union = (SetOperationList) ((Select) CCJSqlParserUtil.parse(sql))
                .getSelectBody();
        PlainSelect rightLeg = (PlainSelect) union.getSelects().get(1);
        AllColumns legColumns = Assertions.assertInstanceOf(AllColumns.class,
                rightLeg.getSelectItems().get(0).getExpression());
        Assertions.assertTrue(legColumns.getTransformers().get(0).isStrict());
        assertSqlCanBeParsedAndDeparsed(sql, true);

        // the COLUMNS(...) matcher accepts the same STRICT form
        sql = "SELECT COLUMNS('m') EXCEPT STRICT (a) FROM t";
        select = (Select) CCJSqlParserUtil.parse(sql);
        plainSelect = (PlainSelect) select.getSelectBody();
        ColumnsExpression columnsExpression = Assertions.assertInstanceOf(ColumnsExpression.class,
                plainSelect.getSelectItems().get(0).getExpression());
        Assertions.assertTrue(columnsExpression.getTransformers().get(0).isStrict());
        assertSqlCanBeParsedAndDeparsed(sql, true);
    }

    @Test
    public void testAllColumnsReplaceStrictIssue2636() throws JSQLParserException {
        String sql = "SELECT * REPLACE STRICT(1 AS a, 2 AS b) FROM t";
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        AllColumns allColumns = Assertions.assertInstanceOf(AllColumns.class,
                plainSelect.getSelectItems().get(0).getExpression());
        ColumnsTransformer transformer = allColumns.getTransformers().get(0);
        Assertions.assertEquals(ColumnsTransformer.ColumnsTransformerType.REPLACE,
                transformer.getType());
        Assertions.assertTrue(transformer.isStrict());
        Assertions.assertEquals(2, transformer.getReplaceItems().size());
        Assertions.assertEquals("a", transformer.getReplaceItems().get(0).getAlias().getName());
        Assertions.assertEquals(sql, select.toString());
        StatementDeParser statementDeParser = new StatementDeParser(new StringBuilder());
        select.accept(statementDeParser, null);
        Assertions.assertEquals(sql, statementDeParser.getBuilder().toString());
        assertSqlCanBeParsedAndDeparsed(sql, true);

        sql = "SELECT COLUMNS('m') REPLACE STRICT(x + 1 AS x) FROM t";
        select = (Select) CCJSqlParserUtil.parse(sql);
        plainSelect = (PlainSelect) select.getSelectBody();
        Assertions.assertInstanceOf(ColumnsExpression.class,
                plainSelect.getSelectItems().get(0).getExpression());
        assertSqlCanBeParsedAndDeparsed(sql, true);

        // the qualified '*' copies the transformers through its own constructor
        sql = "SELECT t.* REPLACE STRICT(x AS x) FROM t";
        select = (Select) CCJSqlParserUtil.parse(sql);
        plainSelect = (PlainSelect) select.getSelectBody();
        AllTableColumns qualifiedStar = Assertions.assertInstanceOf(AllTableColumns.class,
                plainSelect.getSelectItems().get(0).getExpression());
        Assertions.assertTrue(qualifiedStar.getTransformers().get(0).isStrict());
        assertSqlCanBeParsedAndDeparsed(sql, true);

        // each transformer of the chain carries its own flag
        sql = "SELECT * REPLACE STRICT(x AS x) REPLACE(y AS y) FROM t";
        select = (Select) CCJSqlParserUtil.parse(sql);
        plainSelect = (PlainSelect) select.getSelectBody();
        allColumns = Assertions.assertInstanceOf(AllColumns.class,
                plainSelect.getSelectItems().get(0).getExpression());
        Assertions.assertEquals(2, allColumns.getTransformers().size());
        Assertions.assertTrue(allColumns.getTransformers().get(0).isStrict());
        Assertions.assertFalse(allColumns.getTransformers().get(1).isStrict());
        assertSqlCanBeParsedAndDeparsed(sql, true);

        sql = "SELECT * EXCEPT (a) REPLACE STRICT(x AS a) APPLY(sum) FROM t";
        assertSqlCanBeParsedAndDeparsed(sql, true);
    }

    @Test
    public void testAllColumnsExceptPatternIssue2636() throws JSQLParserException {
        // ClickHouse accepts a re2 pattern string instead of the column list and always
        // prints it without brackets (ASTColumnsTransformers.cpp)
        String sql = "SELECT * EXCEPT '^tmp_' FROM t";
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        AllColumns allColumns = Assertions.assertInstanceOf(AllColumns.class,
                plainSelect.getSelectItems().get(0).getExpression());
        ColumnsTransformer transformer = allColumns.getTransformers().get(0);
        Assertions.assertEquals(ColumnsTransformer.ColumnsTransformerType.EXCEPT,
                transformer.getType());
        Assertions.assertNull(transformer.getExceptColumns());
        Assertions.assertEquals("^tmp_", transformer.getExceptPattern().getValue());
        assertSqlCanBeParsedAndDeparsed(sql, true);

        sql = "SELECT t.* EXCEPT '^tmp_' FROM t";
        assertSqlCanBeParsedAndDeparsed(sql, true);

        sql = "SELECT COLUMNS('m') EXCEPT '^tmp_' FROM t";
        select = (Select) CCJSqlParserUtil.parse(sql);
        plainSelect = (PlainSelect) select.getSelectBody();
        ColumnsExpression columnsExpression = Assertions.assertInstanceOf(ColumnsExpression.class,
                plainSelect.getSelectItems().get(0).getExpression());
        Assertions.assertEquals("^tmp_",
                columnsExpression.getTransformers().get(0).getExceptPattern().getValue());
        assertSqlCanBeParsedAndDeparsed(sql, true);

        // ClickHouse parses STRICT before a pattern as well (it drops the flag only
        // during analysis), so the parser keeps it on the AST
        sql = "SELECT * EXCEPT STRICT '^tmp_' FROM t";
        select = (Select) CCJSqlParserUtil.parse(sql);
        plainSelect = (PlainSelect) select.getSelectBody();
        allColumns = Assertions.assertInstanceOf(AllColumns.class,
                plainSelect.getSelectItems().get(0).getExpression());
        Assertions.assertTrue(allColumns.getTransformers().get(0).isStrict());
        Assertions.assertEquals("^tmp_",
                allColumns.getTransformers().get(0).getExceptPattern().getValue());
        assertSqlCanBeParsedAndDeparsed(sql, true);

        // the same combination reaches the COLUMNS(...) path through its own gate
        sql = "SELECT COLUMNS('m') EXCEPT STRICT '^tmp2_' FROM t";
        assertSqlCanBeParsedAndDeparsed(sql, true);

        // the pattern participates in the expression traversal of the COLUMNS side
        // (getAllExpressions -> ExpressionVisitorAdapter), like the other payloads
        List<String> visitedValues = new ArrayList<>();
        ExpressionVisitorAdapter adapter = new ExpressionVisitorAdapter() {
            @Override
            public Void visit(StringValue stringValue, Object context) {
                visitedValues.add(stringValue.getValue());
                return null;
            }
        };
        select = (Select) CCJSqlParserUtil.parse("SELECT COLUMNS('m') EXCEPT '^tmp_' FROM t");
        plainSelect = (PlainSelect) select.getSelectBody();
        plainSelect.getSelectItems().get(0).getExpression().accept(adapter, null);
        Assertions.assertTrue(visitedValues.contains("^tmp_"),
                "the EXCEPT pattern must be reachable for expression visitors");

        // programmatic construction round-trips through the same output paths
        AllColumns built = new AllColumns();
        built.addTransformer(
                new ColumnsTransformer(ColumnsTransformer.ColumnsTransformerType.EXCEPT)
                        .setStrict(true).setExceptPattern(new StringValue("'inner_.*'")));
        Assertions.assertEquals("* EXCEPT STRICT 'inner_.*'", built.toString());
    }

    @Test
    public void testStrictKeywordStaysIdentifierAndAliasIssue2636() throws JSQLParserException {
        // STRICT is a non-reserved keyword, so plain identifier uses must keep parsing
        assertSqlCanBeParsedAndDeparsed("SELECT strict FROM t", true);
        assertSqlCanBeParsedAndDeparsed("SELECT * AS strict FROM t", true);
        assertSqlCanBeParsedAndDeparsed("SELECT t.strict FROM t", true);
        assertSqlCanBeParsedAndDeparsed("SELECT * EXCEPT (strict) FROM t", true);

        // a trailing STRICT is not part of a transformer: it stays the alias of '*'
        String sql = "SELECT * REPLACE(x AS y) STRICT FROM t";
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        SelectItem<?> selectItem = plainSelect.getSelectItems().get(0);
        Assertions.assertEquals("STRICT", selectItem.getAlias().getName());
        Assertions.assertInstanceOf(AllColumns.class, selectItem.getExpression());
        assertSqlCanBeParsedAndDeparsed(sql, true);

        // a column named strict and a trailing STRICT alias coexist on one item
        assertSqlCanBeParsedAndDeparsed("SELECT * EXCEPT (strict) STRICT FROM t", true);

        // the parenthesized set operation after '*' is unchanged
        assertSqlCanBeParsedAndDeparsed("SELECT * EXCEPT (SELECT b FROM u)", true);
    }

    @Test
    public void testStrictTransformerFormsRejectedIssue2636() {
        assertThrowsParseException("SELECT * EXCEPT STRICT FROM t");
        assertThrowsParseException("SELECT * REPLACE STRICT FROM t");
        assertThrowsParseException("SELECT * EXCEPT STRICT a FROM t");
        assertThrowsParseException("SELECT * EXCEPT ('^tmp_') FROM t");
        assertThrowsParseException("SELECT * EXCEPT 5 FROM t");
        // neither APPLY nor EXCLUDE has a STRICT variant
        assertThrowsParseException("SELECT * APPLY STRICT (x) FROM t");
        assertThrowsParseException("SELECT * EXCLUDE STRICT (a) FROM t");
        assertThrowsParseException("SELECT COLUMNS('m') EXCEPT STRICT a FROM t");
        assertThrowsParseException("SELECT a EXCEPT STRICT FROM t");
        // the pattern and the parenthesized column list are mutually exclusive payloads
        assertThrowsParseException("SELECT * EXCEPT STRICT '^x' (a) FROM t");
        assertThrowsParseException("SELECT * REPLACE STRICT () FROM t");
    }

    private static void assertThrowsParseException(String sql) {
        Assertions.assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql),
                "Expected a parse failure for: " + sql);
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

    @ParameterizedTest
    @ValueSource(strings = {"[x, x + 1]", "x IN (1, 2)",
            "CASE x WHEN 1 THEN 0 ELSE 1 END"})
    public void testColumnsApplyExpressionBodies(String body) throws JSQLParserException {
        for (String prefix : new String[] {"", " APPLY(sum)"}) {
            String sql = "SELECT COLUMNS('m')" + prefix + " APPLY(x -> " + body + ") FROM t";
            PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
            ColumnsExpression expression = Assertions.assertInstanceOf(ColumnsExpression.class,
                    select.getSelectItem(0).getExpression());
            ColumnsTransformer transformer = expression.getTransformers()
                    .get(expression.getTransformers().size() - 1);
            LambdaExpression lambda = Assertions.assertInstanceOf(LambdaExpression.class,
                    transformer.getApplyExpression());
            Assertions.assertEquals(body, lambda.getExpression().toString());
        }
    }

    @Test
    public void testColumnsReplaceCaseExpression() throws JSQLParserException {
        String sql = "SELECT COLUMNS('m') REPLACE(CASE m WHEN 1 THEN 0 ELSE 1 END AS m) FROM t";
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        ColumnsExpression expression = Assertions.assertInstanceOf(ColumnsExpression.class,
                select.getSelectItem(0).getExpression());
        SelectItem<?> replacement = expression.getTransformers().get(0).getReplaceItems().get(0);
        Assertions.assertEquals("CASE m WHEN 1 THEN 0 ELSE 1 END",
                replacement.getExpression().toString());
        Assertions.assertEquals("m", replacement.getAlias().getName());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT * FROM (SELECT COLUMNS('m') APPLY(sum) FROM t) q",
            "WITH q AS (SELECT COLUMNS('m') APPLY(x -> x + 1) FROM t) SELECT * FROM q",
            "SELECT (SELECT COLUMNS('m') APPLY(sum) FROM t) AS result",
            "SELECT COLUMNS('m') APPLY(sum) EXCEPT (SELECT b FROM t)",
            "SELECT 1 UNION SELECT COLUMNS('m') APPLY(sum) FROM t",
            "INSERT INTO dst SELECT COLUMNS('m') APPLY(sum) FROM t",
            "SELECT JSON_VALUE(doc, '$' DEFAULT (SELECT COLUMNS('m') APPLY(sum) APPLY(max) FROM t) ON EMPTY) FROM src"})
    public void testColumnsInSelectContexts(String sql) throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(sql, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"COLUMNS('m').x", "{fn COLUMNS('m')}", "COLUMNS('m')(x)"})
    public void testDecoratedColumnsFunctionKeepsAlias(String function) throws JSQLParserException {
        String sql = "SELECT " + function + " APPLY(sum) FROM t";
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
        SelectItem<?> item = select.getSelectItem(0);
        Function expression = Assertions.assertInstanceOf(Function.class, item.getExpression());
        Assertions.assertSame(item, expression.getParent());
        Assertions.assertEquals("APPLY", item.getAlias().getName());
        Assertions.assertEquals("sum", item.getAlias().getAliasColumns().get(0).name);
    }

    @ParameterizedTest
    @ValueSource(strings = {"APPLY(a, b)", "APPLY(a INT)", "REPLACE(a INT)",
            "REPLACE(a)", "REPLACE(a, b)"})
    public void testColumnsAliasAstAtEachBoundary(String suffix) throws JSQLParserException {
        for (String prefix : new String[] {"", " APPLY(sum)"}) {
            String sql = "SELECT COLUMNS('m')" + prefix + " " + suffix + " FROM t";
            PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(sql, true);
            SelectItem<?> item = select.getSelectItem(0);
            if (prefix.isEmpty()) {
                Assertions.assertInstanceOf(Function.class, item.getExpression());
            } else {
                ColumnsExpression expression = Assertions.assertInstanceOf(ColumnsExpression.class,
                        item.getExpression());
                Assertions.assertEquals(1, expression.getTransformers().size());
                Assertions.assertEquals("sum", expression.getTransformers().get(0)
                        .getApplyExpression().toString());
            }
            Alias alias = item.getAlias();
            Assertions.assertNotNull(alias);
            Assertions.assertEquals(suffix.substring(0, suffix.indexOf('(')), alias.getName());
            Assertions.assertFalse(alias.isUseAs());
            Assertions.assertEquals("a", alias.getAliasColumns().get(0).name);
            if (suffix.contains("INT")) {
                Assertions.assertEquals("INT",
                        alias.getAliasColumns().get(0).colDataType.toString());
            } else {
                Assertions.assertNull(alias.getAliasColumns().get(0).colDataType);
                if (suffix.contains(",")) {
                    Assertions.assertEquals("b", alias.getAliasColumns().get(1).name);
                }
            }
        }
    }

    @Test
    public void testWithLambdaExpressionAliasIssue2632() throws JSQLParserException {
        // https://github.com/JSQLParser/JSqlParser/issues/2632
        String sql = "WITH (x -> x * 2) AS double SELECT double(5)";
        Select select = (Select) assertSqlCanBeParsedAndDeparsed(sql, true);
        WithItem<?> withItem = select.getWithItemsList().get(0);
        Assertions.assertTrue(withItem.getExpression() instanceof ParenthesedExpressionList);
        LambdaExpression lambda = (LambdaExpression) ((ParenthesedExpressionList<?>) withItem
                .getExpression()).get(0);
        Assertions.assertEquals(List.of("x"), lambda.getIdentifiers());
        Assertions.assertEquals("double", withItem.getAliasName());
        Assertions.assertEquals("double(5)",
                ((PlainSelect) select.getSelectBody()).getSelectItems().get(0).getExpression()
                        .toString());
    }

    @Test
    public void testWithLambdaExpressionAliasDocForms() throws JSQLParserException {
        // documented lambda form: (param) -> expr — the AST keeps the parameter name only,
        // so the single-parameter parentheses follow the same deparse convention as
        // function-argument lambdas and are not preserved
        Select select = (Select) CCJSqlParserUtil
                .parse("WITH (value) -> value + 1 AS increment SELECT increment(5)");
        LambdaExpression lambda = (LambdaExpression) select.getWithItemsList().get(0)
                .getExpression();
        Assertions.assertEquals(List.of("value"), lambda.getIdentifiers());
        Assertions.assertEquals("increment", select.getWithItemsList().get(0).getAliasName());
        assertSqlCanBeParsedAndDeparsed("WITH value -> value + 1 AS increment SELECT increment(5)",
                true);

        // multi-parameter lambda with a parenthesized parameter list
        assertSqlCanBeParsedAndDeparsed(
                "WITH (id, extension) -> concat(lower(id), extension) AS gen_name "
                        + "SELECT gen_name('A', 'B')",
                true);

        // the whole lambda may be wrapped in parentheses (issue #2632 style), multi-param too
        assertSqlCanBeParsedAndDeparsed("WITH ((x, y) -> x + y) AS f SELECT f(1, 2)", true);

        // unparenthesized parameter is accepted by ClickHouse as well
        assertSqlCanBeParsedAndDeparsed("WITH x -> x * 2 AS double SELECT double(5)", true);
    }

    @Test
    public void testWithExpressionAlias() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("WITH 1 AS one SELECT one + 1", true);
        assertSqlCanBeParsedAndDeparsed("WITH 2 + 2 AS a SELECT a", true);
        assertSqlCanBeParsedAndDeparsed(
                "WITH '2019-08-01 15:23:00' AS ts_upper_bound SELECT ts_upper_bound", true);
        assertSqlCanBeParsedAndDeparsed("WITH sum(number) AS s SELECT s FROM numbers(3)", true);

        // a bare column reference aliased to another name
        assertSqlCanBeParsedAndDeparsed("WITH x AS y SELECT y FROM (SELECT 42 AS x)", true);

        // an expression alias may reference an earlier WITH item, and the expression
        // may itself be a compound one
        assertSqlCanBeParsedAndDeparsed(
                "WITH (SELECT sum(number) FROM numbers(10)) AS total, total * 2 AS doubled "
                        + "SELECT doubled",
                true);
        assertSqlCanBeParsedAndDeparsed("WITH sum(x) + 1 AS y SELECT y", true);
        assertSqlCanBeParsedAndDeparsed(
                "WITH CASE WHEN 1 < 2 THEN 'a' ELSE 'b' END AS c SELECT c", true);
        assertSqlCanBeParsedAndDeparsed("WITH t AS u, v AS w SELECT u, w", true);
    }

    @Test
    public void testWithParenthesedSubqueryAlias() throws JSQLParserException {
        String sql = "WITH (SELECT a FROM src) AS v SELECT a FROM v, dst";
        assertSqlCanBeParsedAndDeparsed(sql, true);
        Assertions.assertEquals(Set.of("src", "dst"), TablesNamesFinder.findTables(sql));
    }

    @Test
    public void testWithExpressionAliasMixedItems() throws JSQLParserException {
        String sql = "WITH 1 AS one, (x -> x * 2) AS double, t AS (SELECT 1 AS a) "
                + "SELECT one, double(5), a FROM t";
        assertSqlCanBeParsedAndDeparsed(sql, true);
    }

    @Test
    public void testWithStandardCteUnaffected() throws JSQLParserException {
        // guards: the classic CTE shapes must keep parsing exactly as before
        assertSqlCanBeParsedAndDeparsed("WITH t AS (SELECT 1 AS a) SELECT a FROM t", true);
        assertSqlCanBeParsedAndDeparsed("WITH t (a, b) AS (SELECT 1 AS a, 2 AS b) SELECT a FROM t",
                true);
        assertSqlCanBeParsedAndDeparsed(
                "WITH RECURSIVE q AS (SELECT 1 AS n UNION ALL SELECT n + 1 FROM q WHERE n < 3) "
                        + "SELECT n FROM q",
                true);
        assertSqlCanBeParsedAndDeparsed(
                "WITH a AS MATERIALIZED (SELECT 1 AS b) SELECT b FROM a", true);
        assertSqlCanBeParsedAndDeparsed(
                "WITH a AS NOT MATERIALIZED (SELECT 1 AS b) SELECT b FROM a", true);
    }

    @Test
    public void testDataTypeNumericArgsFunctionCall() throws JSQLParserException {
        // ClickHouse names lambda aliases after type-like keywords (issue #2632 uses double),
        // and calls them like functions: double(5). DATA_TYPE(N) without a trailing literal
        // is a function call, not a typed literal.
        Select select = (Select) assertSqlCanBeParsedAndDeparsed("SELECT double(5)", true);
        Assertions.assertTrue(((PlainSelect) select.getSelectBody()).getSelectItems().get(0)
                .getExpression() instanceof Function);
        assertSqlCanBeParsedAndDeparsed("SELECT int(5), varchar(5), double(5) + 1", true);

        // the same family: a DATA_TYPE keyword used as a plain column name, e.g. the
        // documented ClickHouse corpus sum(number) FROM numbers(10)
        assertSqlCanBeParsedAndDeparsed("SELECT number FROM t", true);
        assertSqlCanBeParsedAndDeparsed("SELECT sum(number) FROM numbers(3)", true);

        // guards: typed literals with a trailing literal keep their cast reading
        assertSqlCanBeParsedAndDeparsed("SELECT INT '5'", true);
        assertSqlCanBeParsedAndDeparsed("SELECT DECIMAL(10,2) '1.5'", true);
    }

    @Test
    public void testWithExpressionAliasTraversableByVisitorAdapter() throws JSQLParserException {
        // a generic SelectVisitorAdapter traversal must reach the expression body of a
        // WITH <expression> AS <identifier> item, including nested table references,
        // exactly as it reaches the body of a classic CTE
        List<String> plainSelects = new ArrayList<>();
        SelectVisitorAdapter<Void> visitor = new SelectVisitorAdapter<>() {
            @Override
            public <S> Void visit(PlainSelect plainSelect, S context) {
                plainSelects.add(String.valueOf(plainSelect.getFromItem()));
                return super.visit(plainSelect, context);
            }
        };
        Select select = (Select) CCJSqlParserUtil
                .parse("WITH (SELECT 1 FROM src) AS v SELECT a FROM v, dst");
        select.getSelectBody().accept(visitor, null);
        Assertions.assertTrue(plainSelects.contains("src"),
                "expression body table missing from traversal: " + plainSelects);

        // a lambda alias is handed to the expression visitor
        List<String> expressions = new ArrayList<>();
        SelectVisitorAdapter<Void> exprVisitor =
                new SelectVisitorAdapter<>(new ExpressionVisitorAdapter<>() {
                    @Override
                    public <S> Void visit(LambdaExpression lambdaExpression, S context) {
                        expressions.add(String.valueOf(lambdaExpression));
                        return super.visit(lambdaExpression, context);
                    }
                });
        Select lambdaSelect = (Select) CCJSqlParserUtil
                .parse("WITH (x -> x * 2) AS double SELECT double(5)");
        Assertions.assertNotNull(lambdaSelect.getWithItemsList().get(0).getExpression());
        lambdaSelect.getSelectBody().accept(exprVisitor, null);
        Assertions.assertEquals(List.of("x -> x * 2"), expressions);
    }

    @Test
    public void testWithExpressionAliasRecursiveClausesSerialize() throws JSQLParserException {
        // the tolerant-grammar SEARCH/CYCLE suffix must survive serialization on the
        // expression branch instead of being dropped
        String sql = "WITH 1 AS one SEARCH DEPTH FIRST BY one SET o SELECT one";
        Select select = (Select) assertSqlCanBeParsedAndDeparsed(sql, true);
        Assertions.assertNotNull(select.getWithItemsList().get(0).getSearchClause());

        String sql2 = "WITH 1 AS one CYCLE one SET c USING p SELECT one";
        Select select2 = (Select) assertSqlCanBeParsedAndDeparsed(sql2, true);
        Assertions.assertNotNull(select2.getWithItemsList().get(0).getCycleClause());
    }
}
