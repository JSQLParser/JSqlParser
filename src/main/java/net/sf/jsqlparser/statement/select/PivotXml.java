/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.schema.Column;

import java.util.List;

public class PivotXml extends Pivot {

    private SelectBody inSelect;
    private boolean inAny = false;

    @Override
    public void accept(PivotVisitor pivotVisitor) {
        pivotVisitor.visit(this);
    }

    public SelectBody getInSelect() {
        return inSelect;
    }

    public void setInSelect(SelectBody inSelect) {
        this.inSelect = inSelect;
    }

    public boolean isInAny() {
        return inAny;
    }

    public void setInAny(boolean inAny) {
        this.inAny = inAny;
    }

    @Override
    public String toString() {
        List<Column> forColumns = getForColumns();
        String in = inAny ? "ANY" : inSelect == null ? PlainSelect.getStringList(getInItems()) : inSelect.
                toString();
        return "PIVOT XML ("
                + PlainSelect.getStringList(getFunctionItems())
                + " FOR " + PlainSelect.
                        getStringList(forColumns, true, forColumns != null && forColumns.size() > 1)
                + " IN (" + in + "))";
    }

}
