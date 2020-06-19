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
import static net.sf.jsqlparser.test.TestUtils.*;
import org.junit.Test;

public class ExplainTest {

    @Test
    public void testDescribe() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("EXPLAIN SELECT * FROM mytable");
    }

    @Test
    public void testAnalyze() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("EXPLAIN ANALYZE SELECT * FROM mytable");
    }

    @Test
    public void testBuffers() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("EXPLAIN BUFFERS SELECT * FROM mytable");
    }

    @Test
    public void testCosts() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("EXPLAIN COSTS SELECT * FROM mytable");
    }

    @Test
    public void testVerbose() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("EXPLAIN VERBOSE SELECT * FROM mytable");
    }

    @Test
    public void testMultiOptions_orderPreserved() throws  JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("EXPLAIN VERBOSE ANALYZE BUFFERS COSTS SELECT * FROM mytable");
    }
}
