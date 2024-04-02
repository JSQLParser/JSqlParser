package net.sf.jsqlparser.statement.select;

import org.junit.jupiter.api.Test;

public class BigQueryTest {

    @Test
    void testTrailingComma() {

        // allows trailing commas after the last select items
        String sqlStr = "WITH\n" +
                "  Products AS (\n" +
                "    SELECT 'shirt' AS product_type, 't-shirt' AS product_name, 3 AS product_count UNION ALL\n" +
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
}
