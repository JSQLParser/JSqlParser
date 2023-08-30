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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Pivot implements Serializable {

    private List<SelectItem<Function>> functionItems;
    private ExpressionList<Column> forColumns;
    private List<SelectItem<?>> singleInItems;
    private List<SelectItem<ExpressionList>> multiInItems;
    private Alias alias;

    public void accept(PivotVisitor pivotVisitor) {
        pivotVisitor.visit(this);
    }

    public List<SelectItem<?>> getSingleInItems() {
        return singleInItems;
    }

    public void setSingleInItems(List<SelectItem<?>> singleInItems) {
        this.singleInItems = singleInItems;
    }

    public List<SelectItem<ExpressionList>> getMultiInItems() {
        return multiInItems;
    }

    public void setMultiInItems(List<SelectItem<ExpressionList>> multiInItems) {
        this.multiInItems = multiInItems;
    }

    public List<SelectItem<Function>> getFunctionItems() {
        return functionItems;
    }

    public void setFunctionItems(List<SelectItem<Function>> functionItems) {
        this.functionItems = functionItems;
    }

    public ExpressionList<Column> getForColumns() {
        return forColumns;
    }

    public void setForColumns(ExpressionList<Column> forColumns) {
        this.forColumns = forColumns;
    }

    public List<?> getInItems() {
        return singleInItems == null ? multiInItems : singleInItems;
    }

    public Alias getAlias() {
        return alias;
    }

    public void setAlias(Alias alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        return "PIVOT ("
                + PlainSelect.getStringList(functionItems)
                + " FOR "
                + PlainSelect.getStringList(forColumns, true,
                        forColumns != null && forColumns.size() > 1)
                + " IN " + PlainSelect.getStringList(getInItems(), true, true) + ")"
                + (alias != null ? alias.toString() : "");
    }

    public Pivot withFunctionItems(List<SelectItem<Function>> functionItems) {
        this.setFunctionItems(functionItems);
        return this;
    }

    public Pivot withForColumns(ExpressionList<Column> forColumns) {
        this.setForColumns(forColumns);
        return this;
    }

    public Pivot withSingleInItems(List<SelectItem<?>> singleInItems) {
        this.setSingleInItems(singleInItems);
        return this;
    }

    public Pivot withMultiInItems(List<SelectItem<ExpressionList>> multiInItems) {
        this.setMultiInItems(multiInItems);
        return this;
    }

    public Pivot withAlias(Alias alias) {
        this.setAlias(alias);
        return this;
    }

    public Pivot addFunctionItems(SelectItem<Function>... functionItems) {
        List<SelectItem<Function>> collection =
                Optional.ofNullable(getFunctionItems()).orElseGet(ArrayList::new);
        Collections.addAll(collection, functionItems);
        return this.withFunctionItems(collection);
    }

    public Pivot addFunctionItems(Collection<? extends SelectItem<Function>> functionItems) {
        List<SelectItem<Function>> collection =
                Optional.ofNullable(getFunctionItems()).orElseGet(ArrayList::new);
        collection.addAll(functionItems);
        return this.withFunctionItems(collection);
    }

    public Pivot addForColumns(Column... forColumns) {
        return this.addForColumns(Arrays.asList(forColumns));
    }

    public Pivot addForColumns(Collection<? extends Column> forColumns) {
        ExpressionList<Column> collection =
                Optional.ofNullable(getForColumns()).orElseGet(ExpressionList::new);
        collection.addAll(forColumns);
        return this.withForColumns(collection);
    }

    public Pivot addSingleInItems(SelectItem<?>... singleInItems) {
        List<SelectItem<?>> collection =
                Optional.ofNullable(getSingleInItems()).orElseGet(ArrayList::new);
        Collections.addAll(collection, singleInItems);
        return this.withSingleInItems(collection);
    }

    public Pivot addSingleInItems(Collection<? extends SelectItem<?>> singleInItems) {
        List<SelectItem<?>> collection =
                Optional.ofNullable(getSingleInItems()).orElseGet(ArrayList::new);
        collection.addAll(singleInItems);
        return this.withSingleInItems(collection);
    }

    public Pivot addMultiInItems(SelectItem<ExpressionList>... multiInItems) {
        List<SelectItem<ExpressionList>> collection =
                Optional.ofNullable(getMultiInItems()).orElseGet(ArrayList::new);
        Collections.addAll(collection, multiInItems);
        return this.withMultiInItems(collection);
    }

    public Pivot addMultiInItems(Collection<? extends SelectItem<ExpressionList>> multiInItems) {
        List<SelectItem<ExpressionList>> collection =
                Optional.ofNullable(getMultiInItems()).orElseGet(ArrayList::new);
        collection.addAll(multiInItems);
        return this.withMultiInItems(collection);
    }
}
