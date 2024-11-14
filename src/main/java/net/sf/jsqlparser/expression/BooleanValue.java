/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

import java.util.Objects;

/**
 * A boolean value true/false
 */
public final class BooleanValue extends ASTNodeAccessImpl implements Expression {

    private boolean value = false;

    public BooleanValue() {
        // empty constructor
    }

    public BooleanValue(String value) {
        this(Boolean.parseBoolean(value));
    }

    public BooleanValue(boolean bool) {
        value = bool;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean bool) {
        value = bool;
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        return Boolean.toString(value);
    }

    public BooleanValue withValue(boolean bool) {
        this.setValue(bool);
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
        BooleanValue that = (BooleanValue) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
