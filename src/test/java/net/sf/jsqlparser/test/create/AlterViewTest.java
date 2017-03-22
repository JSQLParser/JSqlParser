package net.sf.jsqlparser.test.create;

import net.sf.jsqlparser.JSQLParserException;
import static net.sf.jsqlparser.test.TestUtils.*;
import org.junit.Test;

public class AlterViewTest {

    @Test
    public void testAlterView() throws JSQLParserException {
        String stmt = "ALTER VIEW myview AS SELECT * FROM mytab";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }
}
