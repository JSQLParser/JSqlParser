package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.JSQLParserException;
import static net.sf.jsqlparser.test.TestUtils.*;
import org.junit.Test;

/**
 *
 * @author tw
 */
public class DescribeTest {

    @Test
    public void testDescribe() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("DESCRIBE foo.products");
    }
}
