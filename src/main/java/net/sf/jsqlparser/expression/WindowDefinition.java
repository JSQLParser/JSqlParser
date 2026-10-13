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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.statement.select.OrderByElement;

public class WindowDefinition implements Serializable {


    final OrderByClause orderBy = new OrderByClause();
    final PartitionByClause partitionBy = new PartitionByClause();
    WindowElement windowElement = null;
    private String windowName;
    private String baseWindowName;

    public OrderByClause getOrderBy() {
        return orderBy;
    }

    public PartitionByClause getPartitionBy() {
        return partitionBy;
    }

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

    public void setPartitionExpressionList(ExpressionList<Expression> partitionExpressionList) {
        setPartitionExpressionList(partitionExpressionList, false);
    }

    public void setPartitionExpressionList(ExpressionList<Expression> partitionExpressionList,
            boolean brackets) {
        partitionBy.setExpressions(partitionExpressionList, brackets);
    }

    /** DuckDB: the window this one extends, as w in OVER (w ORDER BY b), or {@code null}. */
    public String getBaseWindowName() {
        return baseWindowName;
    }

    public WindowDefinition setBaseWindowName(String baseWindowName) {
        this.baseWindowName = baseWindowName;
        return this;
    }

    /** Appends the base window name, separated from any following part. */
    public void appendBaseWindowNameTo(StringBuilder builder) {
        if (baseWindowName != null) {
            builder.append(baseWindowName);
            boolean more = partitionBy.getPartitionExpressionList() != null
                    && !partitionBy.getPartitionExpressionList().isEmpty()
                    || orderBy.getOrderByElements() != null
                            && !orderBy.getOrderByElements().isEmpty()
                    || windowElement != null;
            if (more) {
                builder.append(' ');
            }
        }
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

    /** Returns the partition, order and frame expressions for both inline and named windows. */
    public List<Expression> getAllExpressions() {
        List<Expression> expressions = new ArrayList<>(partitionBy);
        if (getOrderByElements() != null) {
            for (OrderByElement element : getOrderByElements()) {
                expressions.add(element.getExpression());
            }
        }
        if (windowElement != null) {
            if (windowElement.getRange() != null) {
                addOffsetExpression(expressions, windowElement.getRange().getStart());
                addOffsetExpression(expressions, windowElement.getRange().getEnd());
            }
            addOffsetExpression(expressions, windowElement.getOffset());
        }
        return expressions;
    }

    private static void addOffsetExpression(List<Expression> expressions, WindowOffset offset) {
        if (offset != null && offset.getExpression() != null) {
            expressions.add(offset.getExpression());
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        if (windowName != null) {
            b.append(windowName).append(" AS ");
        }
        b.append("(");
        appendBaseWindowNameTo(b);
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
