package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

class OracleHintTest {

    @Test
    void testSelect() throws JSQLParserException {
        String sqlString = "SELECT /*+parallel*/ * from dual";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlString, true);
    }

    @Test
    void testDelete() throws JSQLParserException {
        String sqlString = "DELETE /*+parallel*/ from dual";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlString, true);
    }

    @Test
    void testInsert() throws JSQLParserException {
        String sqlString = "INSERT /*+parallel*/ INTO dual VALUES(1)";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlString, true);
    }

    @Test
    void testUpdate() throws JSQLParserException {
        String sqlString = "UPDATE /*+parallel*/ dual SET a=b";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlString, true);
    }

    @Test
    void testMerge() throws JSQLParserException {
        String sqlString =
                "MERGE /*+parallel*/ INTO dual USING z ON (a=b) WHEN MATCHED THEN UPDATE SET a=b";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlString, true);
    }

}
