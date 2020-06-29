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
    public TableFunction alias(Alias alias) {
        return (TableFunction) FromItem.super.alias(alias);
    }

    @Override
    public TableFunction function(Function function) {
        return (TableFunction) super.function(function);
    }

}
