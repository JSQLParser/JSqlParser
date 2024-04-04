package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
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
}
