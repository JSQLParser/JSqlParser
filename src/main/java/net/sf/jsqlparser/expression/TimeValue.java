/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

import java.sql.Time;

/**
 * A Time in the form {t 'hh:mm:ss'}
 */
public class TimeValue extends ASTNodeAccessImpl implements Expression {

    private Time value;

    public TimeValue(String value) {
        this.value = Time.valueOf(value.substring(1, value.length() - 1));
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    public Time getValue() {
        return value;
    }

    public void setValue(Time d) {
        value = d;
    }

    @Override
    public String toString() {
        return "{t '" + value + "'}";
    }
}
