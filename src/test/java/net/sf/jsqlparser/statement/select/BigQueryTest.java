/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.ArrayExpression;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.TablePartitioning;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class BigQueryTest {

    @Test
    @Disabled
    void testTrailingComma() {
        // allows trailing commas after the last select items
        String sqlStr = "WITH\n" +
                "  Products AS (\n" +
                "    SELECT 'shirt' AS product_type, 't-shirt' AS product_name, 3 AS product_count UNION ALL\n"
                +
                "    SELECT 'shirt', 't-shirt', 8 UNION ALL\n" +
                "    SELECT 'shirt', 'polo', 25 UNION ALL\n" +
                "    SELECT 'pants', 'jeans', 6\n" +
                "  )\n" +
                "SELECT\n" +
                "  product_type,\n" +
                "  product_name,\n" +
                "  SUM(product_count) AS product_sum,\n" +
                "  GROUPING(product_type) AS product_type_agg,\n" +
                "  GROUPING(product_name) AS product_name_agg,\n" +
                "FROM Products\n" +
                "GROUP BY GROUPING SETS(product_type, product_name, ())\n" +
                "ORDER BY product_name, product_type";
    }

    @Test
    void testAggregateFunctionIgnoreNulls() throws JSQLParserException {
        String sqlStr = "SELECT ARRAY_AGG(x IGNORE NULLS) AS array_agg\n" +
                "FROM UNNEST([NULL, 1, -2, 3, -2, 1, NULL]) AS x";

        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testAggregateFunctionLimit() throws JSQLParserException {
        String sqlStr = "SELECT ARRAY_AGG(x LIMIT 5) AS array_agg\n" +
                "FROM UNNEST([2, 1, -2, 3, -2, 1, 2]) AS x;\n";

        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testAny() throws JSQLParserException {
        String sqlStr = "SELECT\n" +
                "  fruit,\n" +
                "  ANY_VALUE(fruit) OVER (ORDER BY LENGTH(fruit) ROWS BETWEEN 1 PRECEDING AND CURRENT ROW) AS any_value\n"
                +
                "FROM UNNEST(['apple', 'banana', 'pear']) as fruit;\n";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testAggregateFunctionHaving() throws JSQLParserException {
        String sqlStr = "WITH\n" +
                "  Store AS (\n" +
                "    SELECT 20 AS sold, \"apples\" AS fruit\n" +
                "    UNION ALL\n" +
                "    SELECT 30 AS sold, \"pears\" AS fruit\n" +
                "    UNION ALL\n" +
                "    SELECT 30 AS sold, \"bananas\" AS fruit\n" +
                "    UNION ALL\n" +
                "    SELECT 10 AS sold, \"oranges\" AS fruit\n" +
                "  )\n" +
                "SELECT ANY_VALUE(fruit HAVING MAX sold) AS a_highest_selling_fruit FROM Store;\n";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testAsStruct() throws JSQLParserException {
        String sqlStr = "SELECT ARRAY(SELECT AS STRUCT 1 a, 2 b)";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testAsValue() throws JSQLParserException {
        String sqlStr = "SELECT AS VALUE STRUCT(1 AS a, 2 AS b) xyz";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testTimeSeriesFunction() throws JSQLParserException {
        String sqlStr = "with raw_data as (\n"
                + "    select timestamp('2024-12-01') zetime\n"
                + "    union all \n"
                + "    select timestamp('2024-12-04')\n"
                + "  )\n"
                + "select zetime from GAP_FILL(\n"
                + "  TABLE raw_data,\n"
                + "  ts_column => 'zetime',\n"
                + "  bucket_width => INTERVAL 4 HOUR\n"
                + ")";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        TableFunction function = select.getFromItem(TableFunction.class);
        Assertions.assertEquals("TABLE", function.getFunction().getExtraKeyword());
    }

    @Test
    void testArrayOffsetAccessor() throws JSQLParserException {
        String sqlStr = "SELECT arr[OFFSET(0)] FROM t";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        ArrayExpression arrayExpression =
                (ArrayExpression) select.getSelectItem(0).getExpression();

        Assertions.assertEquals("OFFSET(0)", arrayExpression.getIndexExpression().toString());
    }

    @Test
    void testArraySafeOffsetAndOrdinalAccessors() throws JSQLParserException {
        String sqlStr = "SELECT arr[ORDINAL(1)], arr[SAFE_OFFSET(2)], arr[SAFE_ORDINAL(3)] FROM t";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testOffsetClauseStillParses() throws JSQLParserException {
        String sqlStr = "SELECT a FROM t LIMIT 10 OFFSET 5";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testUnnestWithOffsetAfterAlias() throws JSQLParserException {
        String sqlStr = "SELECT x, pos FROM UNNEST([1, 2, 3]) AS x WITH OFFSET AS pos";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        TableFunction unnest = (TableFunction) select.getFromItem();

        Assertions.assertTrue(unnest.isWithOffset());
        Assertions.assertEquals("x", unnest.getAlias().getName());
        Assertions.assertEquals("pos", unnest.getOffsetAlias().getName());
    }

    @Test
    void testUnnestWithOffsetWithoutOffsetAlias() throws JSQLParserException {
        String sqlStr = "SELECT * FROM t, UNNEST(t.arr) AS c WITH OFFSET";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testUnnestWithOrdinalityAliasStillBindsToTableFunction() throws JSQLParserException {
        String sqlStr = "SELECT * FROM UNNEST(ARRAY[1, 2, 3]) WITH ORDINALITY AS t (a, b)";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        TableFunction unnest = (TableFunction) select.getFromItem();

        Assertions.assertFalse(unnest.isWithOffset());
        Assertions.assertEquals("t", unnest.getAlias().getName());
    }

    @Test
    void testAggregateFunctionIgnoreNullsBeforeOrderBy() throws JSQLParserException {
        String sqlStr = "SELECT ARRAY_AGG(x IGNORE NULLS ORDER BY x) FROM t";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testAggregateFunctionIgnoreNullsOrderByLimit() throws JSQLParserException {
        String sqlStr = "SELECT ARRAY_AGG(DISTINCT x IGNORE NULLS ORDER BY x DESC LIMIT 5) FROM t";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testAggregateFunctionOrderByWithoutNullHandling() throws JSQLParserException {
        String sqlStr = "SELECT ARRAY_AGG(x ORDER BY x) FROM t";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testCreateTablePartitionByExpression() throws JSQLParserException {
        String sqlStr = "CREATE TABLE ds.t (id INT64, ts TIMESTAMP) PARTITION BY DATE(ts)";
        CreateTable createTable =
                (CreateTable) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertEquals(TablePartitioning.Type.EXPRESSION,
                createTable.getPartitioning().getType());
        Assertions.assertEquals("DATE(ts)",
                createTable.getPartitioning().getExpression().toString());
    }

    @Test
    void testCreateTablePartitionByColumnWithOptions() throws JSQLParserException {
        String sqlStr =
                "CREATE TABLE ds.t (id INT64, d DATE) PARTITION BY d OPTIONS (description = 'x')";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testCreateTableAsSelectPartitionByAndClusterBy() throws JSQLParserException {
        String sqlStr = "CREATE TABLE ds.t PARTITION BY d CLUSTER BY id "
                + "AS SELECT 1 AS id, CURRENT_DATE() AS d";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testCreateTablePartitionByHashStillParses() throws JSQLParserException {
        String sqlStr = "CREATE TABLE t (id INT) PARTITION BY HASH (id)";
        CreateTable createTable =
                (CreateTable) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertEquals(TablePartitioning.Type.HASH,
                createTable.getPartitioning().getType());
    }
}
