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
public class UseStatementTest {


    @Test
    public void testSimpleUse() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("USE mydatabase");
    }
}
