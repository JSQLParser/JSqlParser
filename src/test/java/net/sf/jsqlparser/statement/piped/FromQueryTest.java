package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FromQueryTest {
    @Test
    void testParseAndDeparse() throws JSQLParserException {
        String sqlStr = "FROM Produce\n"
                + "|> WHERE\n"
                + "    item != 'bananas'\n"
                + "    AND category IN ('fruit', 'nut')\n"
                + "|> AGGREGATE COUNT(*) AS num_items, SUM(sales) AS total_sales\n"
                + "   GROUP BY item\n"
                + "|> ORDER BY item DESC;";
        FromQuery fromQuery = (FromQuery) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertInstanceOf(WherePipeOperator.class, fromQuery.get(0));
        Assertions.assertInstanceOf(AggregatePipeOperator.class, fromQuery.get(1));
        Assertions.assertInstanceOf(OrderByPipeOperator.class, fromQuery.get(2));
    }
}
