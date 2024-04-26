/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

class ExtractExpressionTest {

    @Test
    void testRegularFunctionCall() throws JSQLParserException {
        String sqlStr = "select extract(engine_full, '''(.*?)''')";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

    }
}
