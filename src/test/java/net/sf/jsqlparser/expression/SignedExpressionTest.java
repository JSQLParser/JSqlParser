/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

/**
 * @author toben
 */
public class SignedExpressionTest {

    @Test
    public void testGetSign() throws JSQLParserException {
        assertThrows(IllegalArgumentException.class, () -> new SignedExpression('*', CCJSqlParserUtil.parseExpression("a")), "must not work");
    }
}
