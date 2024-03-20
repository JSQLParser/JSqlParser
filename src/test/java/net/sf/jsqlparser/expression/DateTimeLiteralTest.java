package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

public class DateTimeLiteralTest {

    @Test
    void testDateTimeWithAlias() throws JSQLParserException {
        String sqlStr = "SELECT DATETIME '2005-01-03 12:34:56' as datetime";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testDateTimeWithDoubleQuotes() throws JSQLParserException {
        String sqlStr = "SELECT DATETIME \"2005-01-03 12:34:56\" as datetime";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

}
