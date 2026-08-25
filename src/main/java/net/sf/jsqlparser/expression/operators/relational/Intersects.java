/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0.
 * #L%
 */
package net.sf.jsqlparser.expression.operators.relational;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;

/**
 * The PostgreSQL <code>#</code> binary operator: the geometric intersection of lseg / line / box
 * (documentation Table 9.36) and the integer bitwise exclusive OR (Table 9.4).
 */
public class Intersects extends BinaryExpression {

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    @Override
    public String getStringExpression() {
        return "#";
    }

    @Override
    public Intersects withLeftExpression(Expression expression) {
        return (Intersects) super.withLeftExpression(expression);
    }

    @Override
    public Intersects withRightExpression(Expression expression) {
        return (Intersects) super.withRightExpression(expression);
    }
}
