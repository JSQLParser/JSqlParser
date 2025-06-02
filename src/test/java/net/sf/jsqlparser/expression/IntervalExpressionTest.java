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


class IntervalExpressionTest {

    @Test
    void testExtractExpressionIssue2172() throws JSQLParserException {
        String sqlStr = "select INTERVAL Extract( DAY from Now()) - 1 DAY";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "SELECT UNIX_TIMESTAMP(date_sub(date_sub(date_format(now(),'%y-%m-%d'),interval extract(day from now())-1 day),interval 1 month))*1000";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
