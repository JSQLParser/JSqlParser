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

public class TryCastExpression extends ASTNodeAccessImpl implements Expression {

    private Expression leftExpression;
    private ColDataType type;
    private RowTypeConstructor rowTypeConstructor;
    private boolean useCastKeyword = true;

    public RowTypeConstructor getRowConstructor() {
        return rowTypeConstructor;
    }

    public void setRowTypeConstructor(RowTypeConstructor rowTypeConstructor) {
        this.rowTypeConstructor = rowTypeConstructor;
        this.type = null;
    }

    public TryCastExpression withRowTypeConstructor(RowTypeConstructor rowTypeConstructor) {
        setRowTypeConstructor(rowTypeConstructor);
        return this;
    }

    public ColDataType getType() {
        return type;
    }

    public void setType(ColDataType type) {
        this.type = type;
        this.rowTypeConstructor = null;
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
            return rowTypeConstructor !=null
              ? "TRY_CAST(" + leftExpression + " AS " + rowTypeConstructor.toString() + ")"
              : "TRY_CAST(" + leftExpression + " AS " + type.toString() + ")";
        } else {
            return leftExpression + "::" + type.toString();
        }
    }

    public TryCastExpression withType(ColDataType type) {
        this.setType(type);
        return this;
    }

    public TryCastExpression withUseCastKeyword(boolean useCastKeyword) {
        this.setUseCastKeyword(useCastKeyword);
        return this;
    }

    public TryCastExpression withLeftExpression(Expression leftExpression) {
        this.setLeftExpression(leftExpression);
        return this;
    }

    public <E extends Expression> E getLeftExpression(Class<E> type) {
        return type.cast(getLeftExpression());
    }
}
