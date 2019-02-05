/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;

/**
 * Table Check Constraint Eg. ' CONSTRAINT less_than_ten CHECK (count < 10) ' @au
 *
 *
 * thor mw
 */
public class CheckConstraint extends NamedConstraint {

    private Table table;
    private Expression expression;

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "CONSTRAINT " + getName() + " CHECK (" + expression + ")";
    }
}
