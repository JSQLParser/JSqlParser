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

import java.math.BigInteger;
import java.util.Objects;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * Every number without a point or an exponential format is a LongValue.
 */
public class LongValue extends ASTNodeAccessImpl implements Expression {

    private String stringValue;

    public LongValue() {
        // empty constructor
    }

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
        return Long.parseLong(stringValue);
    }

    public BigInteger getBigIntegerValue() {
        return new BigInteger(stringValue);
    }

    public void setValue(long d) {
        stringValue = String.valueOf(d);
    }

    public LongValue withValue(long d) {
        setValue(d);
        return this;
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

    public LongValue withStringValue(String stringValue) {
        this.setStringValue(stringValue);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LongValue longValue = (LongValue) o;
        return stringValue.equals(longValue.stringValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stringValue);
    }
}
