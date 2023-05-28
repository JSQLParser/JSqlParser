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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Column;

public class Pivot implements Serializable {

    private List<FunctionItem> functionItems;

    private List<Column> forColumns;

    private List<SelectExpressionItem> singleInItems;

    private List<ExpressionListItem> multiInItems;

    private Alias alias;

    public void accept(PivotVisitor pivotVisitor) {
        pivotVisitor.visit(this);
    }

    public List<SelectExpressionItem> getSingleInItems() {
        return singleInItems;
    }

    public void setSingleInItems(List<SelectExpressionItem> singleInItems) {
        this.singleInItems = singleInItems;
    }

    public List<ExpressionListItem> getMultiInItems() {
        return multiInItems;
    }

    public void setMultiInItems(List<ExpressionListItem> multiInItems) {
        this.multiInItems = multiInItems;
    }

    public List<FunctionItem> getFunctionItems() {
        return functionItems;
    }

    public void setFunctionItems(List<FunctionItem> functionItems) {
        this.functionItems = functionItems;
    }

    public List<Column> getForColumns() {
        return forColumns;
    }

    public void setForColumns(List<Column> forColumns) {
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
        return "PIVOT (" + PlainSelect.getStringList(functionItems) + " FOR " + PlainSelect.getStringList(forColumns, true, forColumns != null && forColumns.size() > 1) + " IN " + PlainSelect.getStringList(getInItems(), true, true) + ")" + (alias != null ? alias.toString() : "");
    }

    public Pivot withFunctionItems(List<FunctionItem> functionItems) {
        this.setFunctionItems(functionItems);
        return this;
    }

    public Pivot withForColumns(List<Column> forColumns) {
        this.setForColumns(forColumns);
        return this;
    }

    public Pivot withSingleInItems(List<SelectExpressionItem> singleInItems) {
        this.setSingleInItems(singleInItems);
        return this;
    }

    public Pivot withMultiInItems(List<ExpressionListItem> multiInItems) {
        this.setMultiInItems(multiInItems);
        return this;
    }

    public Pivot withAlias(Alias alias) {
        this.setAlias(alias);
        return this;
    }

    public Pivot addFunctionItems(FunctionItem... functionItems) {
        List<FunctionItem> collection = Optional.ofNullable(getFunctionItems()).orElseGet(ArrayList::new);
        Collections.addAll(collection, functionItems);
        return this.withFunctionItems(collection);
    }

    public Pivot addFunctionItems(Collection<? extends FunctionItem> functionItems) {
        List<FunctionItem> collection = Optional.ofNullable(getFunctionItems()).orElseGet(ArrayList::new);
        collection.addAll(functionItems);
        return this.withFunctionItems(collection);
    }

    public Pivot addForColumns(Column... forColumns) {
        List<Column> collection = Optional.ofNullable(getForColumns()).orElseGet(ArrayList::new);
        Collections.addAll(collection, forColumns);
        return this.withForColumns(collection);
    }

    public Pivot addForColumns(Collection<? extends Column> forColumns) {
        List<Column> collection = Optional.ofNullable(getForColumns()).orElseGet(ArrayList::new);
        collection.addAll(forColumns);
        return this.withForColumns(collection);
    }

    public Pivot addSingleInItems(SelectExpressionItem... singleInItems) {
        List<SelectExpressionItem> collection = Optional.ofNullable(getSingleInItems()).orElseGet(ArrayList::new);
        Collections.addAll(collection, singleInItems);
        return this.withSingleInItems(collection);
    }

    public Pivot addSingleInItems(Collection<? extends SelectExpressionItem> singleInItems) {
        List<SelectExpressionItem> collection = Optional.ofNullable(getSingleInItems()).orElseGet(ArrayList::new);
        collection.addAll(singleInItems);
        return this.withSingleInItems(collection);
    }

    public Pivot addMultiInItems(ExpressionListItem... multiInItems) {
        List<ExpressionListItem> collection = Optional.ofNullable(getMultiInItems()).orElseGet(ArrayList::new);
        Collections.addAll(collection, multiInItems);
        return this.withMultiInItems(collection);
    }

    public Pivot addMultiInItems(Collection<? extends ExpressionListItem> multiInItems) {
        List<ExpressionListItem> collection = Optional.ofNullable(getMultiInItems()).orElseGet(ArrayList::new);
        collection.addAll(multiInItems);
        return this.withMultiInItems(collection);
    }
}
