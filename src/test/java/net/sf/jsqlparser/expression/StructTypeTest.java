package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

class StructTypeTest {
    @Test
    void testStructTypeBigQuery() throws JSQLParserException {
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

    @Test
    void testStructTypeDuckDB() throws JSQLParserException {
        // @todo: check why the white-space after the "{" is needed?!
        String sqlStr = "SELECT { t:'abc',len:5}";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "SELECT UNNEST({ t:'abc', len:5 })";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "SELECT * from (SELECT UNNEST([{ t:'abc', len:5 }]))";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "SELECT * from (SELECT UNNEST([{ t:'abc', len:5 }, ('abc', 6) ], recursive => true))";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testStructTypeWithArgumentsDuckDB() throws JSQLParserException {
        // @todo: check why the white-space after the "{" is needed?!
        String sqlStr = "SELECT { t:'abc',len:5}::STRUCT( t VARCHAR, len INTEGER)";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "SELECT t, len, LPAD(t, len, ' ') as padded from (\n" +
                "select Unnest([\n" +
                "  { t:'abc', len: 5}::STRUCT(t VARCHAR, len INTEGER),\n" +
                "  { t:'abc', len: 5},\n" +
                "  ('abc', 2),\n" +
                "  ('例子', 4)\n" +
                "], \"recursive\" => true))";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
