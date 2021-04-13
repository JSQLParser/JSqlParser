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
    private Expression startIndexExpression;
    private Expression stopIndexExpression;


    public ArrayExpression() {
        // empty constructor
    }

    public ArrayExpression(Expression objExpression, Expression indexExpression, Expression startIndexExpression, Expression stopIndexExpression) {
        this.objExpression = objExpression;
        this.indexExpression = indexExpression;
        this.startIndexExpression = startIndexExpression;
        this.stopIndexExpression = stopIndexExpression;
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

    public Expression getStartIndexExpression() {
        return startIndexExpression;
    }

    public void setStartIndexExpression(Expression startIndexExpression) {
        this.startIndexExpression = startIndexExpression;
    }

    public Expression getStopIndexExpression() {
        return stopIndexExpression;
    }

    public void setStopIndexExpression(Expression stopIndexExpression) {
        this.stopIndexExpression = stopIndexExpression;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String toString() {
        if (indexExpression != null) {
            return objExpression.toString() + "[" + indexExpression.toString() + "]";
        } else {
            return objExpression.toString() + "[" +
                    (startIndexExpression == null ? "" : startIndexExpression.toString()) +
                    ":" +
                    (stopIndexExpression == null ? "" : stopIndexExpression.toString()) +
                    "]";
        }
    }

    public ArrayExpression withObjExpression(Expression objExpression) {
        this.setObjExpression(objExpression);
        return this;
    }

    public ArrayExpression withIndexExpression(Expression indexExpression) {
        this.setIndexExpression(indexExpression);
        return this;
    }

    public ArrayExpression withRangeExpression(Expression startIndexExpression, Expression stopIndexExpression) {
        this.setStartIndexExpression(startIndexExpression);
        this.setStopIndexExpression(stopIndexExpression);
        return this;
    }

    public <E extends Expression> E getObjExpression(Class<E> type) {
        return type.cast(getObjExpression());
    }

    public <E extends Expression> E getIndexExpression(Class<E> type) {
        return type.cast(getIndexExpression());
    }
}
