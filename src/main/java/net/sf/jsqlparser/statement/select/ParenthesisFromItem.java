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

public class ParenthesisFromItem implements FromItem {

    private FromItem fromItem;

    private Alias alias;

    public ParenthesisFromItem() {
    }

    public ParenthesisFromItem(FromItem fromItem) {
        setFromItem(fromItem);
    }

    public FromItem getFromItem() {
        return fromItem;
    }

    public final void setFromItem(FromItem fromItem) {
        this.fromItem = fromItem;
    }

    @Override
    public void accept(FromItemVisitor fromItemVisitor) {
        fromItemVisitor.visit(this);
    }

    @Override
    public String toString() {
        return "(" + fromItem + ")" + (alias != null ? alias.toString() : "");
    }

    @Override
    public Alias getAlias() {
        return alias;
    }

    @Override
    public void setAlias(Alias alias) {
        this.alias = alias;
    }

    @Override
    public Pivot getPivot() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setPivot(Pivot pivot) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public UnPivot getUnPivot() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setUnPivot(UnPivot unpivot) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ParenthesisFromItem fromItem(FromItem fromItem) {
        this.setFromItem(fromItem);
        return this;
    }

    @Override
    public ParenthesisFromItem alias(Alias alias) {
        this.setAlias(alias);
        return this;
    }

    public <E extends FromItem> E getFromItem(Class<E> type) {
        return type.cast(getFromItem());
    }
}
