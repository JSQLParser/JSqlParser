/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.arithmetic;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConcatTest {

    @Test
    void concatTest() {
        Expression expression =
                Concat.concat(new StringValue("A"), new StringValue("B"), new StringValue("C"));
        Assertions.assertInstanceOf(Concat.class, expression);
        Assertions.assertEquals("'A' || 'B' || 'C'", expression.toString());

        expression = Concat.concat(new StringValue("A"));
        Assertions.assertInstanceOf(StringValue.class, expression);
        Assertions.assertEquals("'A'", expression.toString());

        expression = Concat.concat();
        Assertions.assertInstanceOf(NullValue.class, expression);
        Assertions.assertEquals("NULL", expression.toString());
    }

    void addTest() {
        Expression expression = Addition.add(new LongValue(1), new LongValue(2), new LongValue(3));
        Assertions.assertInstanceOf(Addition.class, expression);
        Assertions.assertEquals("1 + 2 + 3", expression.toString());

        expression = Addition.add(new LongValue(1));
        Assertions.assertInstanceOf(LongValue.class, expression);
        Assertions.assertEquals("1", expression.toString());

        expression = Addition.add();
        Assertions.assertInstanceOf(NullValue.class, expression);
        Assertions.assertEquals("NULL", expression.toString());
    }

    void multiplyTest() {
        Expression expression =
                Multiplication.multiply(new LongValue(1), new LongValue(2), new LongValue(3));
        Assertions.assertInstanceOf(Addition.class, expression);
        Assertions.assertEquals("1 + 2 + 3", expression.toString());

        expression = Multiplication.multiply(new LongValue(1));
        Assertions.assertInstanceOf(LongValue.class, expression);
        Assertions.assertEquals("1", expression.toString());

        expression = Multiplication.multiply();
        Assertions.assertInstanceOf(NullValue.class, expression);
        Assertions.assertEquals("NULL", expression.toString());
    }
}
