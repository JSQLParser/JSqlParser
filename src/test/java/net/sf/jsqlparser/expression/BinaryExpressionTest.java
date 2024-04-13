package net.sf.jsqlparser.expression;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BinaryExpressionTest {

    @Test
    void testAddition() {
        Expression addition = BinaryExpression.add(new LongValue(1), new LongValue(1));
        Assertions.assertEquals("1 + 1", addition.toString());
    }
}
