/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

public class FunctionParameterClauseExpression extends ASTNodeAccessImpl implements Expression {
    private Expression expression;
    private String clause;

    public FunctionParameterClauseExpression(Expression expression, String clause) {
        this.expression = expression;
        this.clause = clause;
    }

    public Expression getExpression() {
        return expression;
    }

    public FunctionParameterClauseExpression setExpression(Expression expression) {
        this.expression = expression;
        return this;
    }

    public String getClause() {
        return clause;
    }

    public FunctionParameterClauseExpression setClause(String clause) {
        this.clause = clause;
        return this;
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        if (clause == null || clause.isEmpty()) {
            return expression != null ? expression.toString() : "";
        }
        return expression + " " + clause;
    }
}
