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
    protected List<SelectItem<?>> replaceExpressions;
    private String exceptKeyword;

    public AllColumns(ExpressionList<Column> exceptColumns,
            List<SelectItem<?>> replaceExpressions) {
        this.exceptColumns = exceptColumns;
        this.replaceExpressions = replaceExpressions;
        this.exceptKeyword = exceptColumns != null ? "Except" : null;
    }

    public AllColumns(ExpressionList<Column> exceptColumns,
            List<SelectItem<?>> replaceExpressions, String exceptKeyword) {
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

    public AllColumns setExceptColumns(ExpressionList<Column> exceptColumns) {
        this.exceptColumns = exceptColumns;
        return this;
    }

    public ExpressionList<Column> addExceptColumn(Column column) {
        if (exceptColumns == null) {
            exceptColumns = new ExpressionList<>();
        }
        exceptColumns.add(column);
        return exceptColumns;
    }

    public List<SelectItem<?>> getReplaceExpressions() {
        return replaceExpressions;
    }

    public AllColumns setReplaceExpressions(List<SelectItem<?>> replaceExpressions) {
        this.replaceExpressions = replaceExpressions;
        return this;
    }

    public List<SelectItem<?>> addReplaceExpression(SelectItem<?> selectItem) {
        if (replaceExpressions == null) {
            replaceExpressions = new ArrayList<>();
        }
        replaceExpressions.add(selectItem);
        return replaceExpressions;
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
            builder.append(" REPLACE( ");
            builder.append(Select.getStringList(replaceExpressions));
            builder.append(" )");
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }
}
