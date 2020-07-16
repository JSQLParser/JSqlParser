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

import java.sql.Date;

/**
 * A Date in the form {d 'yyyy-mm-dd'}
 */
public class DateValue extends ASTNodeAccessImpl implements Expression {

    private Date value;

    public DateValue() {
        // empty constructor
    }

    public DateValue(Date value) {
        this.value = value;
    }

    /**
     * A Date in the form {d 'yyyy-mm-dd'}
     *
     * @param value
     */
    public DateValue(String value) {
        this(Date.valueOf(value.substring(1, value.length() - 1)));
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    public Date getValue() {
        return value;
    }

    public void setValue(Date d) {
        value = d;
    }

    @Override
    public String toString() {
        return "{d '" + value.toString() + "'}";
    }

    public DateValue withValue(Date value) {
        this.setValue(value);
        return this;
    }
}
