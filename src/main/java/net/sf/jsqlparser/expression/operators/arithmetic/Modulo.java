/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.arithmetic;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;

/**
 * Modulo expression (a % b).
 */
public class Modulo extends BinaryExpression {

    public Modulo() {
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String getStringExpression() {
        return "%";
    }

    @Override()
    public Modulo leftExpression(Expression arg0) {
        return (Modulo) super.leftExpression(arg0);
    }

    @Override()
    public Modulo rightExpression(Expression arg0) {
        return (Modulo) super.rightExpression(arg0);
    }
}
