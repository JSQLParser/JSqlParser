/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.expression.ExpressionVisitor;

/** Traverses pattern children and, when supplied, SQL expressions in quantifier bounds. */
public class RowPatternVisitorAdapter<T> implements RowPatternVisitor<T> {
    private final ExpressionVisitor<T> expressionVisitor;

    public RowPatternVisitorAdapter() {
        this(null);
    }

    public RowPatternVisitorAdapter(ExpressionVisitor<T> expressionVisitor) {
        this.expressionVisitor = expressionVisitor;
    }

    @Override
    public <S> T visit(RowPattern.Variable pattern, S context) {
        return null;
    }

    @Override
    public <S> T visit(RowPattern.Empty pattern, S context) {
        return null;
    }

    @Override
    public <S> T visit(RowPattern.Anchor pattern, S context) {
        return null;
    }

    @Override
    public <S> T visit(RowPattern.Group pattern, S context) {
        return pattern.getPattern().accept(this, context);
    }

    @Override
    public <S> T visit(RowPattern.Operation pattern, S context) {
        for (RowPattern child : pattern.getPatterns()) {
            child.accept(this, context);
        }
        return null;
    }

    @Override
    public <S> T visit(RowPattern.Quantified pattern, S context) {
        pattern.getPattern().accept(this, context);
        if (expressionVisitor != null) {
            if (pattern.getLowerBound() != null) {
                pattern.getLowerBound().accept(expressionVisitor, context);
            }
            if (pattern.getUpperBound() != null) {
                pattern.getUpperBound().accept(expressionVisitor, context);
            }
        }
        return null;
    }

    @Override
    public <S> T visit(RowPattern.Permute pattern, S context) {
        for (RowPattern child : pattern.getPatterns()) {
            child.accept(this, context);
        }
        return null;
    }

    @Override
    public <S> T visit(RowPattern.Exclusion pattern, S context) {
        return pattern.getPattern().accept(this, context);
    }
}
