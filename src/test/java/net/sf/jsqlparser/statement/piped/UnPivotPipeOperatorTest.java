package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

class UnPivotPipeOperatorTest {

    @Test
    void testParseAndDeparse() throws JSQLParserException {
        String sqlStr = "(\n" +
                "  SELECT 'kale' as product, 55 AS Q1, 45 AS Q2\n" +
                "  UNION ALL\n" +
                "  SELECT 'apple', 8, 10\n" +
                ")\n" +
                "|> UNPIVOT(sales FOR quarter IN (Q1, Q2));";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

}
