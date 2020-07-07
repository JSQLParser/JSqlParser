/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create;

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

import java.util.Arrays;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.schema.Database;
import net.sf.jsqlparser.schema.Sequence;
import net.sf.jsqlparser.schema.Sequence.Parameter;
import net.sf.jsqlparser.schema.Sequence.ParameterType;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import org.junit.Test;

public class CreateSequenceTest {

    @Test
    public void testCreateSequence_noParams() throws JSQLParserException {
        String statement = "CREATE SEQUENCE my_seq";
        assertSqlCanBeParsedAndDeparsed(statement);
        assertDeparse(new CreateSequence().withSequence(new Sequence().withName("my_seq")), statement);
    }

    @Test
    public void testCreateSequence_withIncrement() throws JSQLParserException{
        String statement = "CREATE SEQUENCE db.schema.my_seq INCREMENT BY 1";
        assertSqlCanBeParsedAndDeparsed(statement);
        assertDeparse(new CreateSequence().withSequence(
                new Sequence().withDatabase(new Database("db")).withSchemaName("schema").withName("my_seq")
                .addParameters(new Parameter(ParameterType.INCREMENT_BY).withValue(1L))), statement);
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
        String statement = "CREATE SEQUENCE my_sec START WITH 2 INCREMENT BY 5 CACHE 200 CYCLE";
        assertSqlCanBeParsedAndDeparsed(statement);
        assertDeparse(new CreateSequence().withSequence(new Sequence().withName("my_sec")
                .addParameters(Arrays.asList(
                        new Parameter(ParameterType.START_WITH).withValue(2L),
                        new Parameter(ParameterType.INCREMENT_BY).withValue(5L),
                        new Parameter(ParameterType.CACHE).withValue(200L),
                        new Parameter(ParameterType.CYCLE)))),
                statement);
    }

}
