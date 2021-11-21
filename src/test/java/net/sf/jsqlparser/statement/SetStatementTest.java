/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.util.Collections;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author toben
 */
public class SetStatementTest {

    @Test
    public void testSimpleSet() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SET statement_timeout = 0");
    }

    @Test
    public void testIssue373() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SET deferred_name_resolution true");
    }

    @Test
    public void testIssue373_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SET tester 5");
    }

    @Test
    public void testMultiValue() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SET v = 1, c = 3");
    }

    @Test
    public void testListValue() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SET v = 1, 3");
    }

    @Test
    public void tesTimeZone() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SET LOCAL Time Zone 'UTC'");
    }

    @Test
    public void tesLocalWithEq() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SET LOCAL cursor_tuple_fraction = 0.05");
    }

    @Test
    public void testValueOnIssue927() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SET standard_conforming_strings = on");
    }

    @Test
    public void testObject() {
        SetStatement setStatement = new SetStatement();
        setStatement.add("standard_conforming_strings", Collections.singletonList(new StringValue("ON")), false);
        setStatement.withUseEqual(0, true).remove(0);

        assertEquals(0, setStatement.getCount());
    }
}
