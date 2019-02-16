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

    private String stringValue;

    public HexValue(final String value) {
        String val = value;
        this.stringValue = val;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    public String getValue() {
        return stringValue;
    }

    public void setValue(String d) {
        stringValue = d;
    }

    @Override
    public String toString() {
        return stringValue;
    }
}
