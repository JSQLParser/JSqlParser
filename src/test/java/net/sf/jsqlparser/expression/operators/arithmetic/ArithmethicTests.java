/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.arithmetic;

import org.junit.jupiter.api.Test;

import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.schema.Column;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArithmethicTests {

    @Test
    public void testAddition ()  {
        assertEquals("1 + a",
                new Addition().withLeftExpression(new LongValue(1)).withRightExpression(new Column("a")).toString());
    }

    @Test
    public void testBitwiseAnd() {
        assertEquals("a & b",
                new BitwiseAnd().withLeftExpression(new Column("a")).withRightExpression(new Column("b")).toString());
    }

    @Test
    public void testBitwiseLeftShift() {
        assertEquals("a << b", new BitwiseLeftShift().withLeftExpression(new Column("a"))
                .withRightExpression(new Column("b")).toString());
    }

    @Test
    public void testBitwiseOr() {
        assertEquals("a | b",
                new BitwiseOr().withLeftExpression(new Column("a")).withRightExpression(new Column("b")).toString());
    }

    @Test
    public void testBitwiseRightShift() {
        assertEquals("a >> b",
                new BitwiseRightShift().withLeftExpression(new Column("a")).withRightExpression(new Column("b"))
                .toString());
    }

    @Test
    public void testBitwiseXor() {
        assertEquals("a ^ b",
                new BitwiseXor().withLeftExpression(new Column("a")).withRightExpression(new Column("b")).toString());
    }

    @Test
    public void testConcat() {
        assertEquals("a || b",
                new Concat().withLeftExpression(new Column("a")).withRightExpression(new Column("b")).toString());
    }

    @Test
    public void testDivision() {
        assertEquals("a / b",
                new Division().withLeftExpression(new Column("a")).withRightExpression(new Column("b")).toString());
    }

    @Test
    public void testIntegerDivision() {
        assertEquals("4 DIV 2", new IntegerDivision().withLeftExpression(new LongValue(4))
                .withRightExpression(new LongValue(2)).toString());
    }

    @Test
    public void testModulo() {
        assertEquals("3 % 2",
                new Modulo().withLeftExpression(new LongValue(3)).withRightExpression(new LongValue(2)).toString());
    }

    @Test
    public void testMultiplication() {
        assertEquals("5 * 2",
                new Multiplication().withLeftExpression(new LongValue(5)).withRightExpression(new LongValue(2))
                .toString());
    }

    @Test
    public void testSubtraction() {
        assertEquals("5 - 3", new Subtraction().withLeftExpression(new LongValue(5))
                .withRightExpression(new LongValue(3)).toString());
    }

}
