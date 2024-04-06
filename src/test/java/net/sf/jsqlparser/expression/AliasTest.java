package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

class AliasTest {

    @Test
    void testUDTF() throws JSQLParserException {
        String sqlStr = "select udtf_1(words) as (a1, a2) from tab";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
