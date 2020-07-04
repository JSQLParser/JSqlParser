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

import java.util.Collection;
import java.util.List;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Column;

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

    public PivotXml inSelect(SelectBody inSelect) {
        this.setInSelect(inSelect);
        return this;
    }

    public PivotXml inAny(boolean inAny) {
        this.setInAny(inAny);
        return this;
    }

    public <E extends SelectBody> E getInSelect(Class<E> type) {
        return type.cast(getInSelect());
    }

    @Override()
    public PivotXml alias(Alias alias) {
        return (PivotXml) super.alias(alias);
    }

    @Override()
    public PivotXml functionItems(List<FunctionItem> functionItems) {
        return (PivotXml) super.functionItems(functionItems);
    }

    @Override()
    public PivotXml forColumns(List<Column> forColumns) {
        return (PivotXml) super.forColumns(forColumns);
    }

    @Override()
    public PivotXml singleInItems(List<SelectExpressionItem> singleInItems) {
        return (PivotXml) super.singleInItems(singleInItems);
    }

    @Override()
    public PivotXml multiInItems(List<ExpressionListItem> multiInItems) {
        return (PivotXml) super.multiInItems(multiInItems);
    }

    @Override()
    public PivotXml addFunctionItems(Collection<? extends FunctionItem> functionItems) {
        return (PivotXml) super.addFunctionItems(functionItems);
    }

    @Override()
    public PivotXml addFunctionItems(FunctionItem... functionItems) {
        return (PivotXml) super.addFunctionItems(functionItems);
    }

    @Override()
    public PivotXml addForColumns(Collection<? extends Column> forColumns) {
        return (PivotXml) super.addForColumns(forColumns);
    }

    @Override()
    public PivotXml addForColumns(Column... forColumns) {
        return (PivotXml) super.addForColumns(forColumns);
    }

    @Override()
    public PivotXml addSingleInItems(Collection<? extends SelectExpressionItem> singleInItems) {
        return (PivotXml) super.addSingleInItems(singleInItems);
    }

    @Override()
    public PivotXml addSingleInItems(SelectExpressionItem... singleInItems) {
        return (PivotXml) super.addSingleInItems(singleInItems);
    }

    @Override()
    public PivotXml addMultiInItems(ExpressionListItem... multiInItems) {
        return (PivotXml) super.addMultiInItems(multiInItems);
    }

    @Override()
    public PivotXml addMultiInItems(Collection<? extends ExpressionListItem> multiInItems) {
        return (PivotXml) super.addMultiInItems(multiInItems);
    }

}
