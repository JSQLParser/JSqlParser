package net.sf.jsqlparser.test;

import net.sf.jsqlparser.*;
import org.junit.jupiter.api.*;

public class UnicodeTest {
    @Test
    void testCJKSetIssue1741() throws JSQLParserException {
        String sqlStr = "select c as 中文 from t";

        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "select * from t where 中文 = 'abc'";

        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
