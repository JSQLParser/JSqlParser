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

public class HexValue extends ASTNodeAccessImpl implements Expression {

    private String value;

    public HexValue() {
        // empty constructor
    }

    public HexValue(final String value) {
        String val = value;
        this.value = val;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    public String getValue() {
        return retrieveValue();
    }

    public void setValue(String value) {
        this.value = value;
    }

    public HexValue withValue(String value) {
        this.setValue(value);
        return this;
    }

    @Override
    public String toString() {
        return retrieveValue();
    }

    private String retrieveValue() {
        return value;
    }
}
