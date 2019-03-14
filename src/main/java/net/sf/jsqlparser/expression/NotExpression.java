/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

public class NotExpression extends ASTNodeAccessImpl implements Expression {

    private Expression expression;

    private boolean exclamationMark = false;

    public NotExpression(Expression expression) {
        this(expression, false);
    }

    public NotExpression(Expression expression, boolean useExclamationMark) {
        setExpression(expression);
        this.exclamationMark = useExclamationMark;
    }

    public Expression getExpression() {
        return expression;
    }

    public final void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String toString() {
        return (exclamationMark ? "! " : "NOT ") + expression.toString();
    }

    public boolean isExclamationMark() {
        return exclamationMark;
    }

    public void setExclamationMark(boolean exclamationMark) {
        this.exclamationMark = exclamationMark;
    }
}
