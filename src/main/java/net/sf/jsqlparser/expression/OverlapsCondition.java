/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

public class OverlapsCondition extends ASTNodeAccessImpl implements Expression {
    private final ExpressionList<?> left;
    private final ExpressionList<?> right;

    public OverlapsCondition(ExpressionList<?> left, ExpressionList<?> right) {
        this.left = left;
        this.right = right;
    }

    public ExpressionList<?> getLeft() {
        return left;
    }

    public ExpressionList<?> getRight() {
        return right;
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S arguments) {
        return expressionVisitor.visit(this, arguments);
    }

    @Override
    public String toString() {
        return String.format("%s OVERLAPS %s", left.toString(), right.toString());
    }
}
