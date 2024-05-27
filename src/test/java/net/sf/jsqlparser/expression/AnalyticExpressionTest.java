/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

class AnalyticExpressionTest {

    @Test
    void testRedshiftApproximate() throws JSQLParserException {
        String sqlStr = "select top 10 date.caldate,\n"
                + "count(totalprice), sum(totalprice),\n"
                + "approximate percentile_disc(0.5) \n"
                + "within group (order by totalprice)\n"
                + "from listing\n"
                + "join date on listing.dateid = date.dateid\n"
                + "group by date.caldate\n"
                + "order by 3 desc;";

        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "select approximate count(distinct pricepaid) from sales;";

        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testDatabricks() throws JSQLParserException {
        String sqlStr = "SELECT any_value(col) IGNORE NULLS FROM test;";

        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "SELECT any_value(col) IGNORE NULLS FROM VALUES (NULL), (5), (20) AS tab(col);";

        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
