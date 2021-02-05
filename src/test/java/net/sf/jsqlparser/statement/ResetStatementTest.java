/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.JSQLParserException;
import org.junit.Test;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;

public class ResetStatementTest {
    @Test
    public void tesResetTZ() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("RESET Time Zone");
    }

    @Test
    public void tesResetAll() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("RESET ALL");
    }

}
