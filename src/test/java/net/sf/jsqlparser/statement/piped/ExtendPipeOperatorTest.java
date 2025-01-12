package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

class ExtendPipeOperatorTest {
    @Test
    void testParseAndDeparse() throws JSQLParserException {
        String sqlStr = "FROM (\n"
                + "  SELECT 'apples' AS item, 2 AS sales\n"
                + "  UNION ALL\n"
                + "  SELECT 'carrots' AS item, 8 AS sales\n"
                + ")\n"
                + "|> EXTEND item IN ('carrots', 'oranges') AS is_orange;";
        FromQuery fromQuery = (FromQuery) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        // Assertions.assertInstanceOf(ExtendPipeOperator.class, fromQuery.get(0));
    }

    @Test
    void testParseAndDeparseWithoutFromKeyword() throws JSQLParserException {
        String sqlStr = "(\n"
                + "  SELECT 'apples' AS item, 2 AS sales\n"
                + "  UNION ALL\n"
                + "  SELECT 'carrots' AS item, 8 AS sales\n"
                + ")\n"
                + "|> EXTEND item IN ('carrots', 'oranges') AS is_orange;";
        FromQuery fromQuery = (FromQuery) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        // Assertions.assertInstanceOf(ExtendPipeOperator.class, fromQuery.get(0));
    }
}
