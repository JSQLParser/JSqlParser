package net.sf.jsqlparser.expression.operators;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

public class XorTest {

    @Test
    void testXorIssue1980() throws JSQLParserException {
        String sqlStr = "SELECT a or b from c";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
