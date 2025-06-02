package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

class WithItemTest {

    @Test
    void testNotMaterializedIssue2251() throws JSQLParserException {
        String sqlStr = "WITH devices AS NOT MATERIALIZED (\n"
                + "  SELECT\n"
                + "    d.uuid AS device_uuid\n"
                + "  FROM active_devices d\n"
                + ")\n"
                + "SELECT 1;";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

}
