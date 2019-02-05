/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2016 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import net.sf.jsqlparser.expression.Expression;

/**
 * Table Exclusion Constraint Eg. 'EXCLUDE WHERE (col1 > 100)'
 *
 * @author wrobstory
 */
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

}
