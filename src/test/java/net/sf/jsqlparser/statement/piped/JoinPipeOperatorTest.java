package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JoinPipeOperatorTest {
    @Test
    void testParseAndDeparse() throws JSQLParserException {
        String sqlStr = "FROM (\n"
                + "  SELECT 'apples' AS item, 2 AS sales\n"
                + "  UNION ALL\n"
                + "  SELECT 'bananas' AS item, 5 AS sales\n"
                + ")\n"
                + "|> AS produce_sales\n"
                + "|> LEFT JOIN\n"
                + "     (\n"
                + "       SELECT \"apples\" AS item, 123 AS id\n"
                + "     ) AS produce_data\n"
                + "   ON produce_sales.item = produce_data.item\n"
                + "|> SELECT produce_sales.item, sales, id;";
        FromQuery fromQuery = (FromQuery) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        Assertions.assertInstanceOf(AsPipeOperator.class, fromQuery.get(0));
        Assertions.assertInstanceOf(JoinPipeOperator.class, fromQuery.get(1));
        Assertions.assertInstanceOf(SelectPipeOperator.class, fromQuery.get(2));
    }

}
