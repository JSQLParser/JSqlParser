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

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Function;

@SuppressWarnings({ "PMD.UncommentedEmptyMethodBody" })
public class TableFunction extends FunctionItem implements FromItem {

    @Override
    public void accept(FromItemVisitor fromItemVisitor) {
        fromItemVisitor.visit(this);
    }

    @Override
    public Pivot getPivot() {
        return null;
    }

    @Override
    public void setPivot(Pivot pivot) {
    }

    @Override
    public UnPivot getUnPivot() {
        return null;
    }

    @Override
    public void setUnPivot(UnPivot unpivot) {
    }

    @Override
    public TableFunction withAlias(Alias alias) {
        return (TableFunction) FromItem.super.withAlias(alias);
    }

    @Override
    public TableFunction withFunction(Function function) {
        return (TableFunction) super.withFunction(function);
    }

    @Override
    public TableFunction withPivot(Pivot pivot) {
        return (TableFunction) FromItem.super.withPivot(pivot);
    }

    @Override
    public TableFunction withUnPivot(UnPivot unpivot) {
        return (TableFunction) FromItem.super.withUnPivot(unpivot);
    }
}
