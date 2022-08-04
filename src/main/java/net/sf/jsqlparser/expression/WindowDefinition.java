/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import java.util.List;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.statement.select.OrderByElement;

public class WindowDefinition {

    final OrderByClause orderBy = new OrderByClause();
    final PartitionByClause partitionBy = new PartitionByClause();
    WindowElement windowElement = null;
    private String windowName;

    public WindowElement getWindowElement() {
        return windowElement;
    }

    public void setWindowElement(WindowElement windowElement) {
        this.windowElement = windowElement;
    }

    public List<OrderByElement> getOrderByElements() {
        return orderBy.getOrderByElements();
    }

    public void setOrderByElements(List<OrderByElement> orderByElements) {
        orderBy.setOrderByElements(orderByElements);
    }

    public ExpressionList getPartitionExpressionList() {
        return partitionBy.getPartitionExpressionList();
    }

    public void setPartitionExpressionList(ExpressionList partitionExpressionList) {
        setPartitionExpressionList(partitionExpressionList, false);
    }

    public void setPartitionExpressionList(ExpressionList partitionExpressionList, boolean brackets) {
        partitionBy.setPartitionExpressionList(partitionExpressionList, brackets);
    }

    public String getWindowName() {
        return windowName;
    }

    public void setWindowName(String windowName) {
        this.windowName = windowName;
    }
    
    public WindowDefinition withWindowName(String windowName) {
        setWindowName(windowName);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        if (windowName != null) {
            b.append(windowName).append(" AS ");
        }
        b.append("(");
        partitionBy.toStringPartitionBy(b);
        orderBy.toStringOrderByElements(b);

        if (windowElement != null) {
            if (orderBy.getOrderByElements() != null) {
                b.append(' ');
            }
            b.append(windowElement);
        }
        b.append(")");
        return b.toString();
    }
}
