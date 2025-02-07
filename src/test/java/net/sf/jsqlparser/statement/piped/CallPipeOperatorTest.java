package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;


class CallPipeOperatorTest {
    @Test
    void testParseAndDeparse() throws JSQLParserException {
        String sqlStr = "FROM input_table\n" +
                "|> CALL tvf1(arg1)\n" +
                "|> CALL tvf2(arg2, arg3);";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
