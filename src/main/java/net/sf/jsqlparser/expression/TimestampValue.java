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
    private String rawValue;
    private static final char QUOTATION = '\'';

    public TimestampValue() {
        // empty constructor
    }

    public TimestampValue(String value) {
        if (value == null) {
            throw new java.lang.IllegalArgumentException("null string");
        } else {
            setRawValue(value);
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

    public String getRawValue() {
        return rawValue;
    }

    public void setRawValue(String rawValue) {
        this.rawValue = rawValue;
        if (rawValue.charAt(0) == QUOTATION) {
            this.value = Timestamp.valueOf(rawValue.substring(1, rawValue.length() - 1));
        } else {
            this.value = Timestamp.valueOf(rawValue.substring(0, rawValue.length()));
        }
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
