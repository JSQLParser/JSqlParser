package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.CosineSimilarity;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaCC8Test {

    @Test
    void testFunction() throws JSQLParserException {
        String sqlStr = "SELECT COUNT(DISTINCT `tbl1`.`id`) FROM dual";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testParenthesedFrom() throws JSQLParserException {
        String sqlStr = "SELECT * FROM (`tbl1`, `tbl2`, `tbl3`)";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    public void testPivotWithAlias() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM  f PIVOT ( max(f.value1) FOR f.factoryCode IN (ZD, COD, SW, PH) )");

        assertSqlCanBeParsedAndDeparsed(
                "SELECT f.t, f.max1(f.t)");
    }

    // net.sf.jsqlparser.expression.StructTypeTest.testStructTypeWithArgumentsDuckDB
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

    //net.sf.jsqlparser.expression.operators.relational.ComparisonOperatorTest.testCosineSimilarity
    @Test
    void testCosineSimilarity() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT (embedding <=> '[3,1,2]') AS cosine_similarity FROM items;");
        Assertions.assertInstanceOf(
                CosineSimilarity.class,
                CCJSqlParserUtil.parseExpression("embedding <=> '[3,1,2]'"));
    }

    //net.sf.jsqlparser.parser.CCJSqlParserUtilTest.testParseExpressionWithBracketsIssue1159
    @Test
    public void testParseExpressionWithBracketsIssue1159() throws Exception {
        Expression result = CCJSqlParserUtil.parseExpression("[travel_data].[travel_id]", false,
                                                             parser -> parser.withSquareBracketQuotation(true));
        assertEquals("[travel_data].[travel_id]", result.toString());
    }

    //net.sf.jsqlparser.parser.CCJSqlParserUtilTest.testParseExpressionWithBracketsIssue1159_2
    @Test
    public void testParseExpressionWithBracketsIssue1159_2() throws Exception {
        Expression result = CCJSqlParserUtil.parseCondExpression("[travel_data].[travel_id]", false,
                                                                 parser -> parser.withSquareBracketQuotation(true));
        assertEquals("[travel_data].[travel_id]", result.toString());
    }

    //net.sf.jsqlparser.parser.CCJSqlParserUtilTest.testCondExpressionIssue1482_2
    @Test
    public void testCondExpressionIssue1482_2() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil.parseCondExpression(
                "test_table_enum.f1_enum IN ('TEST2'::test.\"test_enum\")", false);
        assertEquals("test_table_enum.f1_enum IN ('TEST2'::test.\"test_enum\")", expr.toString());
    }

    //net.sf.jsqlparser.statement.UnsupportedStatementTest.testFunctions
    @Test
    void testFunctions() throws JSQLParserException {
        String sqlStr =
                "CREATE OR REPLACE FUNCTION func_example(foo integer)\n"
                + "RETURNS integer AS $$\n"
                + "BEGIN\n"
                + "  RETURN foo + 1;\n"
                + "END\n"
                + "$$ LANGUAGE plpgsql;\n"
                + "\n"
                + "CREATE OR REPLACE FUNCTION func_example2(IN foo integer, OUT bar integer)\n"
                + "AS $$\n"
                + "BEGIN\n"
                + "    SELECT foo + 1 INTO bar;\n"
                + "END\n"
                + "$$ LANGUAGE plpgsql;";

        Statements statements = CCJSqlParserUtil.parseStatements(sqlStr);
        assertEquals(2, statements.size());
    }

    @Test
    void testSimpleParse() throws JSQLParserException {
        String sqlStr="select A,sdf,sch.tab.col from TABLE_A";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
