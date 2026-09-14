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

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * A ClickHouse {@code COLUMNS('regexp')} matcher with one or more transformers, for example
 * {@code COLUMNS('^metric_') APPLY(x -> round(x, 2))}.
 *
 * Without a transformer, {@code COLUMNS('regexp')} keeps parsing as a regular
 * {@link net.sf.jsqlparser.expression.Function}.
 */
public class ColumnsExpression extends ASTNodeAccessImpl implements Expression {

    private Expression columns;
    private List<ColumnsTransformer> transformers;

    public ColumnsExpression(Expression columns) {
        this.columns = columns;
        this.transformers = new ArrayList<>();
    }

    public ColumnsExpression(Expression columns, List<ColumnsTransformer> transformers) {
        this.columns = columns;
        this.transformers = transformers;
    }

    public Expression getColumns() {
        return columns;
    }

    public ColumnsExpression setColumns(Expression columns) {
        this.columns = columns;
        return this;
    }

    public List<ColumnsTransformer> getTransformers() {
        return transformers;
    }

    public ColumnsExpression setTransformers(List<ColumnsTransformer> transformers) {
        this.transformers = transformers;
        return this;
    }

    public List<Expression> getAllExpressions() {
        List<Expression> expressions = new ArrayList<>();
        if (columns != null) {
            expressions.add(columns);
        }
        for (ColumnsTransformer transformer : transformers) {
            transformer.collectExpressions(expressions);
        }
        return expressions;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append(columns);
        for (ColumnsTransformer transformer : transformers) {
            builder.append(" ").append(transformer);
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }
}
