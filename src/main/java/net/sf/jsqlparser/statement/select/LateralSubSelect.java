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

public class LateralSubSelect implements FromItem {

    private SubSelect subSelect;
    private Alias alias;
    private Pivot pivot;
    private UnPivot unpivot;

    public void setSubSelect(SubSelect subSelect) {
        this.subSelect = subSelect;
    }

    public SubSelect getSubSelect() {
        return subSelect;
    }

    @Override
    public void accept(FromItemVisitor fromItemVisitor) {
        fromItemVisitor.visit(this);
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
        return pivot;
    }

    @Override
    public void setPivot(Pivot pivot) {
        this.pivot = pivot;
    }

    @Override
    public UnPivot getUnPivot() {
        return this.unpivot;
    }

    @Override
    public void setUnPivot(UnPivot unpivot) {
        this.unpivot = unpivot;
    }

    @Override
    public String toString() {
        return "LATERAL" + subSelect.toString()
                + ((alias != null) ? alias.toString() : "")
                + ((pivot != null) ? " " + pivot : "")
                + ((unpivot != null) ? " " + unpivot : "");
    }
}
