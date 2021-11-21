/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import net.sf.jsqlparser.JSQLParserException;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import org.junit.jupiter.api.Test;

public class AlterSequenceTest {

    @Test
    public void testAlterSequence_noParams() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER SEQUENCE my_seq");
    }

    @Test
    public void testAlterSequence_withIncrement() throws JSQLParserException{
        assertSqlCanBeParsedAndDeparsed("ALTER SEQUENCE my_seq INCREMENT BY 1");
    }

    @Test
    public void testAlterSequence_withStart() throws JSQLParserException{
        assertSqlCanBeParsedAndDeparsed("ALTER SEQUENCE my_seq START WITH 10");
    }

    @Test
    public void testAlterSequence_withMaxValue() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER SEQUENCE my_seq MAXVALUE 5");
    }

    @Test
    public void testAlterSequence_withNoMaxValue() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER SEQUENCE my_seq NOMAXVALUE");
    }

    @Test
    public void testAlterSequence_withMinValue() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER SEQUENCE my_seq MINVALUE 5");
    }

    @Test
    public void testAlterSequence_withNoMinValue() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER SEQUENCE my_seq NOMINVALUE");
    }

    @Test
    public void testAlterSequence_withCycle() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER SEQUENCE my_seq CYCLE");
    }

    @Test
    public void testAlterSequence_withNoCycle() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER SEQUENCE my_seq NOCYCLE");
    }

    @Test
    public void testAlterSequence_withCache() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER SEQUENCE my_seq CACHE 10");
    }

    @Test
    public void testAlterSequence_withNoCache() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER SEQUENCE my_seq NOCACHE");
    }

    @Test
    public void testAlterSequence_withOrder() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER SEQUENCE my_seq ORDER");
    }

    @Test
    public void testAlterSequence_withNoOrder() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER SEQUENCE my_seq NOORDER");
    }

    @Test
    public void testAlterSequence_withKeep() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER SEQUENCE my_seq KEEP");
    }

    @Test
    public void testAlterSequence_withNoKeep() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER SEQUENCE my_seq NOKEEP");
    }

    @Test
    public void testAlterSequence_withSession() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER SEQUENCE my_seq SESSION");
    }

    @Test
    public void testAlterSequence_withGlobal() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER SEQUENCE my_seq GLOBAL");
    }

    /**
     * Verifies that we declare the parameter options in the order we found them
     */
    @Test
    public void testAlterSequence_preservesParamOrder() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER SEQUENCE my_sec INCREMENT BY 2 START WITH 10");
        assertSqlCanBeParsedAndDeparsed("ALTER SEQUENCE my_sec START WITH 2 INCREMENT BY 5 NOCACHE");
        assertSqlCanBeParsedAndDeparsed("ALTER SEQUENCE my_sec START WITH 2 INCREMENT BY 5 CACHE 200 CYCLE");
    }
    
    @Test
    public void testAlterSequence_restartIssue1405() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER SEQUENCE my_seq RESTART WITH 1");
    }

    @Test
    public void testAlterSequence_restartIssue1405WithoutValue() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("ALTER SEQUENCE my_seq RESTART");
    }
}
