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

import java.math.BigInteger;

/**
 * Every number without a point or an exponential format is a LongValue.
 */
public class LongValue extends ASTNodeAccessImpl implements Expression {

    private String stringValue;

    public LongValue(final String value) {
        String val = value;
        if (val.charAt(0) == '+') {
            val = val.substring(1);
        }
        this.stringValue = val;
    }

    public LongValue(long value) {
        stringValue = String.valueOf(value);
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    public long getValue() {
        return Long.valueOf(stringValue);
    }

    public BigInteger getBigIntegerValue() {
        return new BigInteger(stringValue);
    }

    public void setValue(long d) {
        stringValue = String.valueOf(d);
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String string) {
        stringValue = string;
    }

    @Override
    public String toString() {
        return getStringValue();
    }
}
