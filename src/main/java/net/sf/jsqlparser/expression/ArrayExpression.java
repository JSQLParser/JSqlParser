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

public class ArrayExpression extends ASTNodeAccessImpl implements Expression {

    private Expression objExpression;
    private Expression indexExpression;

    public ArrayExpression() {
        // empty constructor
    }

    public ArrayExpression(Expression objExpression, Expression indexExpression) {
        this.objExpression = objExpression;
        this.indexExpression = indexExpression;
    }

    public Expression getObjExpression() {
        return objExpression;
    }

    public void setObjExpression(Expression objExpression) {
        this.objExpression = objExpression;
    }

    public Expression getIndexExpression() {
        return indexExpression;
    }

    public void setIndexExpression(Expression indexExpression) {
        this.indexExpression = indexExpression;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String toString() {
        return objExpression.toString() + "[" + indexExpression.toString() + "]";
    }

    public ArrayExpression withObjExpression(Expression objExpression) {
        this.setObjExpression(objExpression);
        return this;
    }

    public ArrayExpression withIndexExpression(Expression indexExpression) {
        this.setIndexExpression(indexExpression);
        return this;
    }

    public <E extends Expression> E getObjExpression(Class<E> type) {
        return type.cast(getObjExpression());
    }

    public <E extends Expression> E getIndexExpression(Class<E> type) {
        return type.cast(getIndexExpression());
    }
}
