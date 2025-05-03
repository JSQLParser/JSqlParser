/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

class OracleHierarchicalExpressionTest {

    @Test
    void testIssue2231() throws JSQLParserException {
        String sqlString =
                "select name from product where level > 1 start with 1 = 1 or 1 = 2 connect by nextversion = prior activeversion";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlString, true);
    }
}
