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

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * Every number with a point or a exponential format is a DoubleValue
 */
public class DoubleValue extends ASTNodeAccessImpl implements Expression {

    private Double value;
    private String stringValue;

    public DoubleValue() {
        // empty constructor
    }

    public DoubleValue(final String value) {
        if (value == null || value.length() == 0) {
            throw new IllegalArgumentException("value can neither be null nor empty.");
        }
        String val = value;
        if (val.charAt(0) == '+') {
            val = val.substring(1);
        }
        this.value = Double.parseDouble(val);
        this.stringValue = val;
    }

    public DoubleValue(final double value) {
        this.value = value;
        this.stringValue = String.valueOf(value);
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    public double getValue() {
        return value;
    }

    public void setValue(Double d) {
        value = d;
    }

    @Override
    public String toString() {
        return stringValue;
    }

    public DoubleValue withValue(Double value) {
        this.setValue(value);
        return this;
    }
}
