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

public class BitwiseXor extends BinaryExpression {

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String getStringExpression() {
        return "^";
    }

    @Override
    public BitwiseXor withLeftExpression(Expression arg0) {
        return (BitwiseXor) super.withLeftExpression(arg0);
    }

    @Override
    public BitwiseXor withRightExpression(Expression arg0) {
        return (BitwiseXor) super.withRightExpression(arg0);
    }
}
