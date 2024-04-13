package net.sf.jsqlparser.expression;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.util.Assert;

class BinaryExpressionTest {

    @Test
    void testAddition() {
        Expression addition = BinaryExpression.add(new LongValue(1), new LongValue(1));
        Assert.equals("1 + 1", addition.toString());
    }
}
