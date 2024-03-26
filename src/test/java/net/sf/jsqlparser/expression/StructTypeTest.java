package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

class StructTypeTest {
    @Test
    void testStructType() throws JSQLParserException {
        String sqlStr = "SELECT t, len, FORMAT('%T', LPAD(t, len)) AS LPAD FROM UNNEST([\n" +
                "  STRUCT('abc' AS t, 5 AS len),\n" +
                "  ('abc', 2),\n" +
                "  ('例子', 4)\n" +
                "])";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "SELECT STRUCT(1, t.str_col)";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "SELECT STRUCT(1 AS a, 'abc' AS b)";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "SELECT STRUCT<x int64, y string>(1, t.str_col)";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
