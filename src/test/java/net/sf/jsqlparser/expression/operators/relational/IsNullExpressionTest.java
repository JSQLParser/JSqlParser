/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.relational;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

class IsNullExpressionTest {

    @Test
    void testNotNullExpression() throws JSQLParserException {
        String sqlStr = "select * from mytable where 1 notnull";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
