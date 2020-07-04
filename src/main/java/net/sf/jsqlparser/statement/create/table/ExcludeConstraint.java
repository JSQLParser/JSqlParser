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

import java.util.List;
import net.sf.jsqlparser.expression.Expression;

public class ExcludeConstraint extends Index {

    private Expression expression;

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        StringBuilder exclusionStatement = new StringBuilder("EXCLUDE WHERE ");
        exclusionStatement.append("(");
        exclusionStatement.append(expression);
        exclusionStatement.append(")");
        return exclusionStatement.toString();
    }

    public ExcludeConstraint expression(Expression expression) {
        this.setExpression(expression);
        return this;
    }

    public <E extends Expression> E getExpression(Class<E> type) {
        return type.cast(getExpression());
    }

    @Override()
    public ExcludeConstraint name(List<String> name) {
        return (ExcludeConstraint) super.name(name);
    }

    @Override()
    public ExcludeConstraint type(String type) {
        return (ExcludeConstraint) super.type(type);
    }

    @Override()
    public ExcludeConstraint using(String using) {
        return (ExcludeConstraint) super.using(using);
    }
}
