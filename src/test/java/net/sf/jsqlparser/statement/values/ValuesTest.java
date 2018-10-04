package net.sf.jsqlparser.statement.values;

import net.sf.jsqlparser.JSQLParserException;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import org.junit.Test;

public class ValuesTest {

    @Test
    public void testDuplicateKey() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("VALUES (1, 2, 'test')");
    }
}
