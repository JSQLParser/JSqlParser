/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

class JsonExpressionTest {

    @Test
    void testIssue1792() throws JSQLParserException, InterruptedException {
        String sqlStr =
                "SELECT ''::JSON -> 'obj'::TEXT";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr =
                "SELECT ('{\"obj\":{\"field\": \"value\"}}'::JSON -> 'obj'::TEXT ->> 'field'::TEXT)";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr =
                "SELECT\n"
                        + " CASE\n"
                        + "    WHEN true\n"
                        + "    THEN (SELECT ((('{\"obj\":{\"field\": \"value\"}}'::JSON -> 'obj'::TEXT ->> 'field'::TEXT))))\n"
                        + " END";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
