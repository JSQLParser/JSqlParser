package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

class SelectPipeOperatorTest {

    @Test
    void testRename() throws JSQLParserException {
        String sqlStr = "SELECT 1 AS x, 2 AS y, 3 AS z\n"
                + "|> AS t\n"
                + "|> RENAME y AS renamed_y\n"
                + "|> SELECT *, t.y AS t_y;";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
