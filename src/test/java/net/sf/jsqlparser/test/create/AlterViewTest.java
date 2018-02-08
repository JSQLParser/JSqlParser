package net.sf.jsqlparser.test.create;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

import org.junit.Test;

import net.sf.jsqlparser.JSQLParserException;

public class AlterViewTest {

    @Test
    public void testAlterView() throws JSQLParserException {
        String stmt = "ALTER VIEW myview AS SELECT * FROM mytab";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }
}
