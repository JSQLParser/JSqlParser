/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.relational;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Tobias Warneke (t.warneke@gmx.net)
 */
public class LikeExpressionTest {

    @Test
    public void testLikeNotIssue660() {
        LikeExpression instance = new LikeExpression();
        assertFalse(instance.isNot());
        assertTrue(instance.withNot(true).isNot());
    }

    @Test
    public void testSetEscapeAndGetStringExpression() throws JSQLParserException {
        LikeExpression instance = (LikeExpression) CCJSqlParserUtil.parseExpression("name LIKE 'J%$_%'");
        // escape character should be $
        Expression instance2 = new StringValue("$");
        instance.setEscape(instance2);
        // match all records with names that start with letter ’J’ and have the ’_’ character in them
        assertEquals("name LIKE 'J%$_%' ESCAPE '$'", instance.toString());
    }
}
