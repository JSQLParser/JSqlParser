package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DropPipeOperatorTest {

    @Test
    void testParseAndDeParseWithoutFromKeyword() throws JSQLParserException {
        String sqlStr = "SELECT 'apples' AS item, 2 AS sales, 'fruit' AS category\n"
                + "|> DROP sales, category;";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testParseAndDeParse() throws JSQLParserException {
        String sqlStr = "FROM (SELECT 1 AS x, 2 AS y) AS t\n"
                + "|> DROP x\n"
                + "|> SELECT t.x AS original_x, y;";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
