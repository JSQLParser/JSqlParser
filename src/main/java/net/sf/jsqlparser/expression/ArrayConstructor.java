/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.create.table.ColDataType;

public class ArrayConstructor extends ASTNodeAccessImpl implements Expression {
    private ExpressionList<?> expressions;
    private boolean arrayKeyword;
    private ColDataType dataType;

    public ArrayConstructor(ExpressionList<?> expressions, boolean arrayKeyword) {
        this.expressions = expressions;
        this.arrayKeyword = arrayKeyword;
    }

    public ArrayConstructor(Expression... expressions) {
        this(new ExpressionList<Expression>(expressions), false);
    }

    public ExpressionList<?> getExpressions() {
        return expressions;
    }

    public void setExpressions(ExpressionList<?> expressions) {
        this.expressions = expressions;
    }

    public boolean isArrayKeyword() {
        return arrayKeyword;
    }

    public void setArrayKeyword(boolean arrayKeyword) {
        this.arrayKeyword = arrayKeyword;
    }

    public ColDataType getDataType() {
        return dataType;
    }

    public ArrayConstructor setDataType(ColDataType dataType) {
        this.dataType = dataType;
        return this;
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (arrayKeyword) {
            sb.append("ARRAY");

            if (dataType != null) {
                sb.append("<").append(dataType).append(">");
            }
        }
        sb.append("[");
        sb.append(expressions.toString());
        sb.append("]");
        return sb.toString();
    }
}
