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

public class DateTimeLiteralExpression extends ASTNodeAccessImpl implements Expression {

    private String value;
    private DateTime type;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public DateTime getType() {
        return type;
    }

    public void setType(DateTime type) {
        this.type = type;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String toString() {
        return type.name() + " " + value;
    }

    public DateTimeLiteralExpression value(String value) {
        this.setValue(value);
        return this;
    }

    public DateTimeLiteralExpression type(DateTime type) {
        this.setType(type);
        return this;
    }

    public static enum DateTime {
        DATE, TIME, TIMESTAMP;
    }
}
