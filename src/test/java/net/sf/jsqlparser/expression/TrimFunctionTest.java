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
import org.junit.jupiter.api.Test;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.*;

class TrimFunctionTest {

    @Test
    void testTrim() throws JSQLParserException {
        String functionStr = "Trim( BOTH 'x' FROM 'xTomxx' )";
        String sqlStr = "select " + functionStr;
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        TrimFunction trimFunction = new TrimFunction()
                .withTrimSpecification(TrimFunction.TrimSpecification.BOTH)
                .withExpression(new StringValue("x"))
                .withUsingFromKeyword(true)
                .withFromExpression(new StringValue("xTomxx"));
        assertEquals(functionStr, trimFunction.toString());
        assertEquals(
                functionStr.replace(" FROM", ","),
                trimFunction.withUsingFromKeyword(false).toString());

        sqlStr = "select trim(BOTH from unnest(string_to_array(initcap(bbbbb),';')))";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

}
