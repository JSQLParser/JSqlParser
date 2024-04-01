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
import net.sf.jsqlparser.util.SelectUtils;

import java.util.ArrayList;

public class CastExpression extends ASTNodeAccessImpl implements Expression {
    public String keyword;

    private Expression leftExpression;
    private ColDataType colDataType = null;
    private ArrayList<ColumnDefinition> columnDefinitions = new ArrayList<>();

    // BigQuery specific FORMAT clause:
    // https://cloud.google.com/bigquery/docs/reference/standard-sql/conversion_functions#cast_as_date
    private String format = null;

    public CastExpression(String keyword, Expression leftExpression, String dataType) {
        this.keyword = keyword;
        this.leftExpression = leftExpression;
        this.colDataType = new ColDataType(dataType);
    }

    // Implicit Cast
    public CastExpression(Expression leftExpression, String dataType) {
        this.keyword = null;
        this.leftExpression = leftExpression;
        this.colDataType = new ColDataType(dataType);
    }

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

    @Deprecated
    public boolean isUseCastKeyword() {
        return keyword != null && !keyword.isEmpty();
    }

    @Deprecated
    public void setUseCastKeyword(boolean useCastKeyword) {
        if (useCastKeyword) {
            if (keyword == null || keyword.isEmpty()) {
                keyword = "CAST";
            }
        } else {
            keyword = null;
        }
    }

    public String getFormat() {
        return format;
    }

    public CastExpression setFormat(String format) {
        this.format = format;
        return this;
    }

    @Override
    public String toString() {
        String formatStr = format != null && !format.isEmpty()
                ? " FORMAT " + format
                : "";
        if (keyword != null && !keyword.isEmpty()) {
            return columnDefinitions.size() > 1
                    ? keyword + "(" + leftExpression + " AS ROW("
                            + SelectUtils.getStringList(columnDefinitions) + ")" + formatStr + ")"
                    : keyword + "(" + leftExpression + " AS " + colDataType.toString() + formatStr
                            + ")";
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
