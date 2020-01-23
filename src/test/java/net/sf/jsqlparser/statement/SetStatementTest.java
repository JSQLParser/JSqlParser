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

import net.sf.jsqlparser.JSQLParserException;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import org.junit.Test;

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
    public void testValueOnIssue927() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SET standard_conforming_strings = on");
    }
}
