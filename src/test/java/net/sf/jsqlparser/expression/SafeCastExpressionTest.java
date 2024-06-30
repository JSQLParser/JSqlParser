/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

public class SafeCastExpressionTest {

    @Test
    public void testSafeCast() throws JSQLParserException {
        TestUtils.assertExpressionCanBeParsedAndDeparsed(
                "SAFE_CAST(ROW(dataid, value, calcMark) AS ROW(datapointid CHAR, value CHAR, calcMark CHAR))",
                true);
        TestUtils.assertExpressionCanBeParsedAndDeparsed(
                "SAFE_CAST(ROW(dataid, value, calcMark) AS testcol)", true);
    }
}
