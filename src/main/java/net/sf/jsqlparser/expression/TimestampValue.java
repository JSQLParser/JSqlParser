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

import java.sql.Timestamp;

/**
 * A Timestamp in the form {ts 'yyyy-mm-dd hh:mm:ss.f . . .'}
 */
public class TimestampValue extends ASTNodeAccessImpl implements Expression {

    private Timestamp value;
    private char quotation = '\'';

    public TimestampValue() {
        // empty constructor
    }

    public TimestampValue(String value) {
        if (value == null) {
            throw new java.lang.IllegalArgumentException("null string");
        } else {
            if (value.charAt(0) == quotation) {
                this.value = Timestamp.valueOf(value.substring(1, value.length() - 1));
            } else {
                this.value = Timestamp.valueOf(value.substring(0, value.length()));
            }
        }
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    public Timestamp getValue() {
        return value;
    }

    public void setValue(Timestamp d) {
        value = d;
    }

    @Override
    public String toString() {
        return "{ts '" + value + "'}";
    }

    public TimestampValue withValue(Timestamp value) {
        this.setValue(value);
        return this;
    }
}
