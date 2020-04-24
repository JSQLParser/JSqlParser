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
    public void testCreateSequence_withMaxValue() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE SEQUENCE my_seq MAXVALUE 5");
    }

    @Test
    public void testCreateSequence_withNoMaxValue() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE SEQUENCE my_seq NOMAXVALUE");
    }

    @Test
    public void testCreateSequence_withMinValue() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE SEQUENCE my_seq MINVALUE 5");
    }

    @Test
    public void testCreateSequence_withNoMinValue() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE SEQUENCE my_seq NOMINVALUE");
    }

    @Test
    public void testCreateSequence_withCycle() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE SEQUENCE my_seq CYCLE");
    }

    @Test
    public void testCreateSequence_withNoCycle() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE SEQUENCE my_seq NOCYCLE");
    }

    @Test
    public void testCreateSequence_withCache() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE SEQUENCE my_seq CACHE 10");
    }

    @Test
    public void testCreateSequence_withNoCache() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE SEQUENCE my_seq NOCACHE");
    }

    @Test
    public void testCreateSequence_withOrder() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE SEQUENCE my_seq ORDER");
    }

    @Test
    public void testCreateSequence_withNoOrder() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE SEQUENCE my_seq NOORDER");
    }

    @Test
    public void testCreateSequence_withKeep() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE SEQUENCE my_seq KEEP");
    }

    @Test
    public void testCreateSequence_withNoKeep() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE SEQUENCE my_seq NOKEEP");
    }

    @Test
    public void testCreateSequence_withSession() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE SEQUENCE my_seq SESSION");
    }

    @Test
    public void testCreateSequence_withGlobal() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE SEQUENCE my_seq GLOBAL");
    }

    /**
     * Verifies that we declare the parameter options in the order we found them
     */
    @Test
    public void testCreateSequence_preservesParamOrder() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE SEQUENCE my_sec INCREMENT BY 2 START WITH 10");
        assertSqlCanBeParsedAndDeparsed("CREATE SEQUENCE my_sec START WITH 2 INCREMENT BY 5 NOCACHE");
        assertSqlCanBeParsedAndDeparsed("CREATE SEQUENCE my_sec START WITH 2 INCREMENT BY 5 CACHE 200 CYCLE");
    }

}
