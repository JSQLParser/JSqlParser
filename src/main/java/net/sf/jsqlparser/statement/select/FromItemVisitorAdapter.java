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

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.imprt.Import;
import net.sf.jsqlparser.statement.piped.FromQuery;

import java.util.ArrayList;

@SuppressWarnings({"PMD.UncommentedEmptyMethodBody"})
public class FromItemVisitorAdapter<T> implements FromItemVisitor<T> {
    private SelectVisitor<T> selectVisitor;

    public FromItemVisitorAdapter(SelectVisitor<T> selectVisitor) {
        this.selectVisitor = selectVisitor;
    }

    public FromItemVisitorAdapter() {
        this.selectVisitor = new SelectVisitorAdapter<>();
    }

    public SelectVisitor<T> getSelectVisitor() {
        return selectVisitor;
    }

    public FromItemVisitorAdapter<T> setSelectVisitor(SelectVisitor<T> selectVisitor) {
        this.selectVisitor = selectVisitor;
        return this;
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
}
