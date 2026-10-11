/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

/** A SQL Server table-level DEFAULT expression FOR a column, with an optional constraint name. */
public class DefaultConstraint extends NamedConstraint {
    private Expression expression;
    private Column column;
    private boolean withValues;

    public DefaultConstraint() {
        setType("DEFAULT");

    }

    @Override
    public ConstraintKind getKind() {
        return ConstraintKind.DEFAULT;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

    public boolean isWithValues() {
        return withValues;
    }

    public void setWithValues(boolean withValues) {
        this.withValues = withValues;
    }

    public DefaultConstraint withExpression(Expression expression) {
        setExpression(expression);
        return this;
    }

    public DefaultConstraint withColumn(Column column) {
        setColumn(column);
        return this;
    }

    public DefaultConstraint withWithValues(boolean withValues) {
        setWithValues(withValues);
        return this;
    }

    @Override
    public DefaultConstraint withName(String name) {
        setName(name);
        return this;
    }

    /** Shares rendering with deparsers while allowing both the value and column to be visited. */
    public void appendTo(StringBuilder builder, Consumer<Expression> expressionWriter) {
        if (expression == null || column == null) {
            throw new IllegalStateException("DEFAULT requires an expression and a target column");
        }
        if (getName() != null) {
            builder.append("CONSTRAINT ").append(getName()).append(' ');
        }
        builder.append("DEFAULT ");
        expressionWriter.accept(expression);
        builder.append(" FOR ");
        expressionWriter.accept(column);
        if (withValues) {
            builder.append(" WITH VALUES");
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        appendTo(builder, builder::append);
        return builder.toString();
    }
}
