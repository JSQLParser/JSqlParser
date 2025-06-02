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

class SelectPipeOperatorTest {

    @Test
    void testRename() throws JSQLParserException {
        String sqlStr = "SELECT 1 AS x, 2 AS y, 3 AS z\n"
                + "|> AS t\n"
                + "|> RENAME y AS renamed_y\n"
                + "|> SELECT *, t.y AS t_y;";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testDistinct() throws JSQLParserException {
        String sqlStr = "FROM orders\n" +
                "|> WHERE order_date >= '2024-01-01'\n" +
                "|> SELECT DISTINCT customer_id \n" +
                "|> INNER JOIN customers USING(customer_id)\n" +
                "|> SELECT *;";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
