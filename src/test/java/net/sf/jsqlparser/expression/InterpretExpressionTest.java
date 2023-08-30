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

/**
 * @author Matteo Sist
 */
public class InterpretExpressionTest {

    @Test
    public void testInterpret() throws JSQLParserException {
        TestUtils.assertExpressionCanBeParsedAndDeparsed("INTERPRET(1 AS INTEGER)", true);
        TestUtils.assertExpressionCanBeParsedAndDeparsed(
                "INTERPRET(SUBSTRING(ENTRY_DATA, 1, 4) AS INTEGER)", true);
    }
}
