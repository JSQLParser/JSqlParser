/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;

/**
 * BigQuery's {@code ASSERT expression [AS description]}, which fails the script when the expression
 * does not evaluate to {@code TRUE}.
 *
 * @see <a href=
 *      "https://cloud.google.com/bigquery/docs/reference/standard-sql/debugging-statements">Debugging
 *      statements</a>
 */
public class AssertStatement implements Statement {
    private Expression expression;
    private StringValue description;

    public AssertStatement() {}

    public AssertStatement(Expression expression) {
        this.expression = expression;
    }

    public AssertStatement(Expression expression, StringValue description) {
        this.expression = expression;
        this.description = description;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public AssertStatement withExpression(Expression expression) {
        setExpression(expression);
        return this;
    }

    public StringValue getDescription() {
        return description;
    }

    public void setDescription(StringValue description) {
        this.description = description;
    }

    public AssertStatement withDescription(StringValue description) {
        setDescription(description);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("ASSERT ").append(expression);
        if (description != null) {
            builder.append(" AS ").append(description);
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }
}
