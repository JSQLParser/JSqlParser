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
import net.sf.jsqlparser.statement.create.table.ColDataType;

public class SafeCastExpression extends ASTNodeAccessImpl implements Expression {

    private Expression leftExpression;
    private ColDataType type;
    private RowConstructor rowConstructor;
    private boolean useCastKeyword = true;

    public RowConstructor getRowConstructor() {
        return rowConstructor;
    }

    public void setRowConstructor(RowConstructor rowConstructor) {
        this.rowConstructor = rowConstructor;
        this.type = null;
    }

    public SafeCastExpression withRowConstructor(RowConstructor rowConstructor) {
        setRowConstructor(rowConstructor);
        return this;
    }

    public ColDataType getType() {
        return type;
    }

    public void setType(ColDataType type) {
        this.type = type;
        this.rowConstructor = null;
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
            return rowConstructor!=null
              ? "SAFE_CAST(" + leftExpression + " AS " + rowConstructor.toString() + ")"
              : "SAFE_CAST(" + leftExpression + " AS " + type.toString() + ")";
        } else {
            return leftExpression + "::" + type.toString();
        }
    }

    public SafeCastExpression withType(ColDataType type) {
        this.setType(type);
        return this;
    }

    public SafeCastExpression withUseCastKeyword(boolean useCastKeyword) {
        this.setUseCastKeyword(useCastKeyword);
        return this;
    }

    public SafeCastExpression withLeftExpression(Expression leftExpression) {
        this.setLeftExpression(leftExpression);
        return this;
    }

    public <E extends Expression> E getLeftExpression(Class<E> type) {
        return type.cast(getLeftExpression());
    }
}
