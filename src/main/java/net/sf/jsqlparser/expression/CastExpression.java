/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.create.table.ColDataType;

/**
 *
 * @author tw
 */
public class CastExpression extends ASTNodeAccessImpl implements Expression {

    private Expression leftExpression;
    private ColDataType type;
    private boolean useCastKeyword = true;

    public ColDataType getType() {
        return type;
    }

    public void setType(ColDataType type) {
        this.type = type;
    }

    public Expression getLeftExpression() {
        return leftExpression;
    }

    public void setLeftExpression(Expression expression) {
        leftExpression = expression;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    public boolean isUseCastKeyword() {
        return useCastKeyword;
    }

    public void setUseCastKeyword(boolean useCastKeyword) {
        this.useCastKeyword = useCastKeyword;
    }

    @Override
    public String toString() {
        if (useCastKeyword) {
            return "CAST(" + leftExpression + " AS " + type.toString() + ")";
        } else {
            return leftExpression + "::" + type.toString();
        }
    }
}
