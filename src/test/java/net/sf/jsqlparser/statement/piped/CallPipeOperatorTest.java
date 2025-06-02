/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;


class CallPipeOperatorTest {
    @Test
    void testParseAndDeparse() throws JSQLParserException {
        String sqlStr = "FROM input_table\n" +
                "|> CALL tvf1(arg1)\n" +
                "|> CALL tvf2(arg2, arg3);";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
