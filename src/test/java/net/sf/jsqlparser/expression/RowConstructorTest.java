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

class RowConstructorTest {
    @Test
    public void testRowConstructor() throws JSQLParserException {
        TestUtils.assertExpressionCanBeParsedAndDeparsed("ROW(dataid, value, calcMark)", true);
    }

}
