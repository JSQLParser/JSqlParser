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
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.select.Select;

import java.util.ArrayList;

public class CastExpression extends ASTNodeAccessImpl implements Expression {
    public String keyword;

    private Expression leftExpression;
    private ColDataType colDataType = null;
    private ArrayList<ColumnDefinition> columnDefinitions = new ArrayList<>();
    private boolean useCastKeyword = true;

    public CastExpression(String keyword) {
        this.keyword = keyword;
    }

    public CastExpression() {
        this("CAST");
    }

    public ColDataType getColDataType() {
        return colDataType;
    }

    public ArrayList<ColumnDefinition> getColumnDefinitions() {
        return columnDefinitions;
    }

    public void setColDataType(ColDataType colDataType) {
        this.colDataType = colDataType;
    }

    public void addColumnDefinition(ColumnDefinition columnDefinition) {
        this.columnDefinitions.add(columnDefinition);
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
            return columnDefinitions.size() > 1
                    ? keyword + "(" + leftExpression + " AS ROW("
                            + Select.getStringList(columnDefinitions) + "))"
                    : keyword + "(" + leftExpression + " AS " + colDataType.toString() + ")";
        } else {
            return leftExpression + "::" + colDataType.toString();
        }
    }

    public CastExpression withType(ColDataType type) {
        this.setColDataType(type);
        return this;
    }

    public CastExpression withUseCastKeyword(boolean useCastKeyword) {
        this.setUseCastKeyword(useCastKeyword);
        return this;
    }

    public CastExpression withLeftExpression(Expression leftExpression) {
        this.setLeftExpression(leftExpression);
        return this;
    }

    public <E extends Expression> E getLeftExpression(Class<E> type) {
        return type.cast(getLeftExpression());
    }
}
