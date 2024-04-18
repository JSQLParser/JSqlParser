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
}
