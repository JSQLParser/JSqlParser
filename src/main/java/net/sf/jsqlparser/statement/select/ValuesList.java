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

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;

import java.util.Iterator;
import java.util.List;
import net.sf.jsqlparser.expression.Alias;

public class ValuesList implements FromItem {

    private Alias alias;
    private MultiExpressionList multiExpressionList;
    private boolean noBrackets = false;
    private List<String> columnNames;

    public ValuesList() {
    }

    public ValuesList(MultiExpressionList multiExpressionList) {
        this.multiExpressionList = multiExpressionList;
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
        return null;
    }

    @Override
    public void setPivot(Pivot pivot) {
    }

    public MultiExpressionList getMultiExpressionList() {
        return multiExpressionList;
    }

    public void setMultiExpressionList(MultiExpressionList multiExpressionList) {
        this.multiExpressionList = multiExpressionList;
    }

    public boolean isNoBrackets() {
        return noBrackets;
    }

    public void setNoBrackets(boolean noBrackets) {
        this.noBrackets = noBrackets;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();

        b.append("(VALUES ");
        for (Iterator<ExpressionList> it = getMultiExpressionList().getExprList().iterator(); it.
                hasNext();) {
            b.append(PlainSelect.getStringList(it.next().getExpressions(), true, !isNoBrackets()));
            if (it.hasNext()) {
                b.append(", ");
            }
        }
        b.append(")");
        if (alias != null) {
            b.append(alias.toString());

            if (columnNames != null) {
                b.append("(");
                for (Iterator<String> it = columnNames.iterator(); it.hasNext();) {
                    b.append(it.next());
                    if (it.hasNext()) {
                        b.append(", ");
                    }
                }
                b.append(")");
            }
        }
        return b.toString();
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }
}
