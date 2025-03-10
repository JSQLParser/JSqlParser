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

class SetPipeOperatorTest {

    @Test
    void parseAndDeparse() throws JSQLParserException {
        String sqlStr = "(\n"
                + "  SELECT 1 AS x, 11 AS y\n"
                + "  UNION ALL\n"
                + "  SELECT 2 AS x, 22 AS y\n"
                + ")\n"
                + "|> SET x = x * x, y = 3;";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
