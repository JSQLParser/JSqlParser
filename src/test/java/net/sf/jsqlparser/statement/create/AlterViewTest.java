package net.sf.jsqlparser.statement.create;

import net.sf.jsqlparser.JSQLParserException;
import static net.sf.jsqlparser.test.TestUtils.*;
import org.junit.Test;

public class AlterViewTest {

    @Test
    public void testAlterView() throws JSQLParserException {
        String stmt = "ALTER VIEW myview AS SELECT * FROM mytab";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testReplaceView() throws JSQLParserException {
        String stmt = "REPLACE VIEW myview AS SELECT * FROM mytab";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }
}
