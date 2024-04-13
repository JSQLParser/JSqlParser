/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.relational;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BetweenTest {
    @Test
    void testBetweenWithAdditionIssue1948() throws JSQLParserException {
        String sqlStr =
                "select col FROM tbl WHERE start_time BETWEEN 1706024185 AND MyFunc() - 734400";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
