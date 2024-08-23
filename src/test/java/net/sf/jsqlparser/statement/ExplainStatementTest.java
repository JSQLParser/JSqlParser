package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

class ExplainStatementTest {

    @Test
    void testDuckDBSummarizeTable() throws JSQLParserException {
        String sqlStr = "SUMMARIZE cfe.test;";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testDuckDBSummarizeSelect() throws JSQLParserException {
        String sqlStr = "SUMMARIZE SELECT * FROM cfe.test;";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testOracleExplainPlan() throws JSQLParserException {
        String sqlStr = "EXPLAIN PLAN SELECT * FROM cfe.test;";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testH2ExplainPlanFor() throws JSQLParserException {
        String sqlStr = "EXPLAIN PLAN FOR SELECT * FROM cfe.test;";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testH2ExplainAnalyze() throws JSQLParserException {
        String sqlStr = "EXPLAIN ANALYZE SELECT * FROM cfe.test;";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
