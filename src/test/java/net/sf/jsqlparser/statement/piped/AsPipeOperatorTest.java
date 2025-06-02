/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

class AsPipeOperatorTest {

    @Test
    void testParseAndDeparse() throws JSQLParserException {
        String sqlStr="(\n" +
                "  SELECT '000123' AS id, 'apples' AS item, 2 AS sales\n" +
                "  UNION ALL\n" +
                "  SELECT '000456' AS id, 'bananas' AS item, 5 AS sales\n" +
                ") AS sales_table\n" +
                "|> AGGREGATE SUM(sales) AS total_sales GROUP BY id, item\n" +
                "|> AS t1\n" +
                "|> JOIN (SELECT 456 AS id, 'yellow' AS color) AS t2\n" +
                "   ON CAST(t1.id AS INT64) = t2.id\n" +
                "|> SELECT t2.id, total_sales, color;";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
