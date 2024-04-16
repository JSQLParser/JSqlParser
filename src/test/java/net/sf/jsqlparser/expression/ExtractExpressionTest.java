package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

class ExtractExpressionTest {

    @Test
    void testRegularFunctionCall() throws JSQLParserException {
        String sqlStr = "select extract(engine_full, '''(.*?)''')";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

    }
}
