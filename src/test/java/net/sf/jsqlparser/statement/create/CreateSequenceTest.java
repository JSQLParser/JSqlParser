package net.sf.jsqlparser.statement.create;

import net.sf.jsqlparser.JSQLParserException;
import org.junit.Test;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

public class CreateSequenceTest {

    @Test
    public void testCreateSequence_noParams() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE SEQUENCE my_seq");
    }

    @Test
    public void testCreateSequence_withIncrement() throws JSQLParserException{
        assertSqlCanBeParsedAndDeparsed("CREATE SEQUENCE my_seq INCREMENT BY 1");
    }

    @Test
    public void testCreateSequence_withStart() throws JSQLParserException{
        assertSqlCanBeParsedAndDeparsed("CREATE SEQUENCE my_seq START WITH 10");
    }

    @Test
    public void testCreateSequence_withCache() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE SEQUENCE my_seq CACHE 10");
    }

    @Test
    public void testCreateSequence_withNoCache() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE SEQUENCE my_seq NOCACHE");
    }

    /**
     * Verifies that we declare the parameter options in the order we found them
     */
    @Test
    public void testCreateSequence_preservesParamOrder() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE SEQUENCE my_sec INCREMENT BY 2 START WITH 10");
        assertSqlCanBeParsedAndDeparsed("CREATE SEQUENCE my_sec START WITH 2 INCREMENT BY 5 NOCACHE");
    }

}
