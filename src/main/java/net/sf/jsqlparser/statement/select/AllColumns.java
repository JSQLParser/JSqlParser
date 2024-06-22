/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Column;

import java.util.ArrayList;
import java.util.List;

public class AllColumns extends ASTNodeAccessImpl implements Expression {
    protected ExpressionList<Column> exceptColumns;
    protected List<SelectItem<Column>> replaceExpressions;
    private String exceptKeyword;

    public AllColumns(ExpressionList<Column> exceptColumns,
            List<SelectItem<Column>> replaceExpressions) {
        this.exceptColumns = exceptColumns;
        this.replaceExpressions = replaceExpressions;
        this.exceptKeyword = exceptColumns != null ? "Except" : null;
    }

    public AllColumns(ExpressionList<Column> exceptColumns,
            List<SelectItem<Column>> replaceExpressions, String exceptKeyword) {
        this.exceptColumns = exceptColumns;
        this.replaceExpressions = replaceExpressions;
        this.exceptKeyword = exceptKeyword;
    }

    public AllColumns() {
        this(null, null);
    }

    public ExpressionList<Column> getExceptColumns() {
        return exceptColumns;
    }

    public ExpressionList<Column> addExceptColumn(Column column) {
        if (exceptColumns == null) {
            exceptColumns = new ExpressionList<>();
        }
        exceptColumns.add(column);
        return exceptColumns;
    }

    public AllColumns setExceptColumns(ExpressionList<Column> exceptColumns) {
        this.exceptColumns = exceptColumns;
        return this;
    }

    public List<SelectItem<Column>> getReplaceExpressions() {
        return replaceExpressions;
    }

    public List<SelectItem<Column>> addReplaceExpression(SelectItem<Column> selectItem) {
        if (replaceExpressions == null) {
            replaceExpressions = new ArrayList<>();
        }
        replaceExpressions.add(selectItem);
        return replaceExpressions;
    }

    public AllColumns setReplaceExpressions(List<SelectItem<Column>> replaceExpressions) {
        this.replaceExpressions = replaceExpressions;
        return this;
    }

    public String getExceptKeyword() {
        return exceptKeyword;
    }

    public AllColumns setExceptKeyword(String exceptKeyword) {
        this.exceptKeyword = exceptKeyword;
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("*");
        if (exceptColumns != null && !exceptColumns.isEmpty()) {
            builder.append(" ").append(exceptKeyword).append("( ");
            exceptColumns.appendTo(builder);
            builder.append(" )");
        }
        if (replaceExpressions != null && !replaceExpressions.isEmpty()) {
            builder.append(" Replace(");
            int i = 0;
            for (SelectItem<?> selectItem : replaceExpressions) {
                builder.append(i++ > 0 ? ", " : " ");
                selectItem.appendTo(builder);
            }
            builder.append(" )");
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S arguments) {
        return expressionVisitor.visit(this, arguments);
    }
}
