package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TableFunctionTest {

    @Test
    void testLateralFlat() throws JSQLParserException {
        String sqlStr = "WITH t AS (\n" +
                "  SELECT \n" +
                "    'ABC' AS dim, \n" +
                "    ARRAY_CONSTRUCT('item1', 'item2', 'item3') AS user_items\n" +
                ")\n" +
                "SELECT DIM, count(value) as COUNT_\n" +
                "FROM t a,\n" +
                "LATERAL FLATTEN(input => a.user_items) b\n" +
                "group by 1";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

}
