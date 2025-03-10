/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.expression.Expression;

public class WherePipeOperator extends PipeOperator {
    private Expression expression;

    public WherePipeOperator(Expression expression) {
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    public WherePipeOperator setExpression(Expression expression) {
        this.expression = expression;
        return this;
    }

    @Override
    public <T, S> T accept(PipeOperatorVisitor<T, S> visitor, S context) {
        return visitor.visit(this, context);
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("|> ")
                .append("WHERE ")
                .append(expression)
                .append("\n");
        return builder;
    }
}
