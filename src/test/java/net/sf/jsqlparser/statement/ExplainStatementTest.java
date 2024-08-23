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
}
