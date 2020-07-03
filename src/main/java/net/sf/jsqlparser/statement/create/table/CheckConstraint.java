/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;

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

    public CheckConstraint table(Table table) {
        this.setTable(table);
        return this;
    }

    public CheckConstraint expression(Expression expression) {
        this.setExpression(expression);
        return this;
    }

    public <E extends Expression> E getExpression(Class<E> type) {
        return type.cast(getExpression());
    }
}
