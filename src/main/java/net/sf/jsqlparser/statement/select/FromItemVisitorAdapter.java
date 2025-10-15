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
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.JsonTable;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.imprt.Import;
import net.sf.jsqlparser.statement.piped.FromQuery;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings({"PMD.UncommentedEmptyMethodBody"})
public class FromItemVisitorAdapter<T> implements FromItemVisitor<T> {
    private SelectVisitor<T> selectVisitor;
    private ExpressionVisitor<T> expressionVisitor;

    public FromItemVisitorAdapter(SelectVisitor<T> selectVisitor,
            ExpressionVisitor<T> expressionVisitor) {
        this.selectVisitor = selectVisitor;
        this.expressionVisitor = expressionVisitor;
    }

    public FromItemVisitorAdapter(ExpressionVisitor<T> expressionVisitor) {
        this.selectVisitor = new SelectVisitorAdapter<>(expressionVisitor);
        this.expressionVisitor = expressionVisitor;
    }

    public FromItemVisitorAdapter() {
        this.selectVisitor = new SelectVisitorAdapter<>();
        this.expressionVisitor = new ExpressionVisitorAdapter<>(this.selectVisitor);
    }


    public SelectVisitor<T> getSelectVisitor() {
        return selectVisitor;
    }

    public FromItemVisitorAdapter<T> setSelectVisitor(SelectVisitor<T> selectVisitor) {
        this.selectVisitor = selectVisitor;
        return this;
    }

    public FromItemVisitorAdapter<T> setSelectVisitor(SelectVisitorAdapter<T> selectVisitor) {
        this.selectVisitor = selectVisitor;
        this.expressionVisitor = selectVisitor.getExpressionVisitor();
        return this;
    }

    public ExpressionVisitor<T> getExpressionVisitor() {
        return expressionVisitor;
    }

    public FromItemVisitorAdapter<T> setExpressionVisitor(ExpressionVisitor<T> expressionVisitor) {
        this.expressionVisitor = expressionVisitor;
        return this;
    }

    @Override
    public <S> T visitJoins(Collection<Join> joins, S context) {
        if (joins != null) {
            for (Join join : joins) {
                join.getFromItem().accept(this, context);
                if (join.getUsingColumns() != null) {
                    for (Column column : join.getUsingColumns()) {
                        column.accept(expressionVisitor, context);
                    }
                }
                if (join.getOnExpressions() != null) {
                    for (Expression expression : join.getOnExpressions()) {
                        expression.accept(expressionVisitor, context);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public <S> T visit(Table table, S context) {
        return null;
    }

    @Override
    public <S> T visit(ParenthesedSelect select, S context) {
        return select.getPlainSelect().accept(selectVisitor, context);
    }

    @Override
    public <S> T visit(LateralSubSelect lateralSubSelect, S context) {
        return lateralSubSelect.getPlainSelect().accept(selectVisitor, context);
    }

    @Override
    public <S> T visit(TableFunction tableFunction, S context) {

        return null;
    }

    @Override
    public <S> T visit(ParenthesedFromItem fromItem, S context) {
        return fromItem.getFromItem().accept(this, context);
    }

    @Override
    public <S> T visit(Values values, S context) {
        for (Expression expression : values.getExpressions()) {
            expression.accept(expressionVisitor, context);
        }
        return null;
    }

    @Override
    public <S> T visit(PlainSelect plainSelect, S context) {
        return plainSelect.accept(selectVisitor, context);
    }

    @Override
    public <S> T visit(SetOperationList setOperationList, S context) {
        ArrayList<T> results = new ArrayList<>();
        for (Select select : setOperationList.getSelects()) {
            results.add(select.accept(selectVisitor, context));
        }
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public <S> T visit(TableStatement tableStatement, S context) {
        return null;
    }

    @Override
    public <S> T visit(Import imprt, S context) {

        return null;
    }

    public <S> T visit(FromQuery fromQuery, S context) {
        return fromQuery.accept(selectVisitor, context);
    }

    @Override
    public <S> T visit(JsonTable jsonTable, S context) {
        // TODO: Implement
        return null;
    }
}
