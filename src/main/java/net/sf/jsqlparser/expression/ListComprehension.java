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

import java.util.List;
import java.util.function.Consumer;

/**
 * DuckDB's list comprehension {@code [expression FOR x[, i] IN list [IF condition]]}, where
 * {@code i} is the 1-based index of {@code x}.
 *
 * @see <a href="https://duckdb.org/docs/stable/sql/functions/list#list-comprehension">List
 *      comprehension</a>
 */
public class ListComprehension extends ASTNodeAccessImpl implements Expression {
    private Expression expression;
    private List<String> variables;
    private Expression list;
    private Expression condition;

    public ListComprehension(Expression expression, List<String> variables, Expression list,
            Expression condition) {
        this.expression = expression;
        this.variables = variables;
        this.list = list;
        this.condition = condition;
    }

    public Expression getExpression() {
        return expression;
    }

    public ListComprehension setExpression(Expression expression) {
        this.expression = expression;
        return this;
    }

    /** The element variable, followed by the index variable if any. */
    public List<String> getVariables() {
        return variables;
    }

    public ListComprehension setVariables(List<String> variables) {
        this.variables = variables;
        return this;
    }

    public Expression getList() {
        return list;
    }

    public ListComprehension setList(Expression list) {
        this.list = list;
        return this;
    }

    /** The {@code IF} filter, or {@code null}. */
    public Expression getCondition() {
        return condition;
    }

    public ListComprehension setCondition(Expression condition) {
        this.condition = condition;
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressionRenderer) {
        builder.append("[");
        expressionRenderer.accept(expression);
        builder.append(" FOR ").append(String.join(", ", variables)).append(" IN ");
        expressionRenderer.accept(list);
        if (condition != null) {
            builder.append(" IF ");
            expressionRenderer.accept(condition);
        }
        return builder.append("]");
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }
}
