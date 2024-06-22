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
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;

import java.util.Collection;
import java.util.List;

public class PivotXml extends Pivot {

    private Select inSelect;
    private boolean inAny = false;

    @Override
    public <T, S> T accept(PivotVisitor<T> pivotVisitor, S arguments) {
        return pivotVisitor.visit(this, arguments);
    }

    public Select getInSelect() {
        return inSelect;
    }

    public void setInSelect(Select inSelect) {
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
        String in = inAny ? "ANY"
                : inSelect == null ? PlainSelect.getStringList(getInItems()) : inSelect.toString();
        return "PIVOT XML ("
                + PlainSelect.getStringList(getFunctionItems())
                + " FOR "
                + PlainSelect.getStringList(forColumns, true,
                        forColumns != null && forColumns.size() > 1)
                + " IN (" + in + "))";
    }

    public PivotXml withInSelect(Select inSelect) {
        this.setInSelect(inSelect);
        return this;
    }

    public PivotXml withInAny(boolean inAny) {
        this.setInAny(inAny);
        return this;
    }

    public <E extends Select> E getInSelect(Class<E> type) {
        return type.cast(getInSelect());
    }

    @Override
    public PivotXml withAlias(Alias alias) {
        return (PivotXml) super.withAlias(alias);
    }

    @Override
    public PivotXml withFunctionItems(List<SelectItem<Function>> functionItems) {
        return (PivotXml) super.withFunctionItems(functionItems);
    }

    @Override
    public PivotXml withForColumns(ExpressionList<Column> forColumns) {
        return (PivotXml) super.withForColumns(forColumns);
    }

    @Override
    public PivotXml withSingleInItems(List<SelectItem<?>> singleInItems) {
        return (PivotXml) super.withSingleInItems(singleInItems);
    }

    @Override
    public PivotXml withMultiInItems(List<SelectItem<ExpressionList<?>>> multiInItems) {
        return (PivotXml) super.withMultiInItems(multiInItems);
    }

    @Override
    public PivotXml addFunctionItems(Collection<? extends SelectItem<Function>> functionItems) {
        return (PivotXml) super.addFunctionItems(functionItems);
    }

    @Override
    public PivotXml addFunctionItems(SelectItem<Function>... functionItems) {
        return (PivotXml) super.addFunctionItems(functionItems);
    }

    @Override
    public PivotXml addForColumns(Collection<? extends Column> forColumns) {
        return (PivotXml) super.addForColumns(forColumns);
    }

    @Override
    public PivotXml addForColumns(Column... forColumns) {
        return (PivotXml) super.addForColumns(forColumns);
    }

    @Override
    public PivotXml addSingleInItems(Collection<? extends SelectItem<?>> singleInItems) {
        return (PivotXml) super.addSingleInItems(singleInItems);
    }

    @Override
    public PivotXml addSingleInItems(SelectItem... singleInItems) {
        return (PivotXml) super.addSingleInItems(singleInItems);
    }

    @Override
    public PivotXml addMultiInItems(SelectItem<ExpressionList<?>>... multiInItems) {
        return (PivotXml) super.addMultiInItems(multiInItems);
    }

    @Override
    public PivotXml addMultiInItems(
            Collection<? extends SelectItem<ExpressionList<?>>> multiInItems) {
        return (PivotXml) super.addMultiInItems(multiInItems);
    }

}
