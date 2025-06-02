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


class TableSamplePipeOperatorTest {
    @Test
    void testParseAndDeparse() throws JSQLParserException {
        String sqlStr = "FROM LargeTable\n" +
                "|> TABLESAMPLE SYSTEM (1.0 PERCENT);\n";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
