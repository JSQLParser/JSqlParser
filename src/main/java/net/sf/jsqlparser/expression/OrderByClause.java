/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.statement.select.OrderByElement;

import java.util.List;

public class OrderByClause {
    private List<OrderByElement> orderByElements;
    private WindowElement windowElement;

    public List<OrderByElement> getOrderByElements() {
        return orderByElements;
    }

    public void setOrderByElements(List<OrderByElement> orderByElements) {
        this.orderByElements = orderByElements;
    }

    public WindowElement getWindowElement() {
        return windowElement;
    }

    public void setWindowElement(WindowElement windowElement) {
        this.windowElement = windowElement;
    }

    void toStringOrderByElements(StringBuilder b) {
        if (orderByElements != null && !orderByElements.isEmpty()) {
            b.append("ORDER BY ");
            for (int i = 0; i < orderByElements.size(); i++) {
                if (i > 0) {
                    b.append(", ");
                }
                b.append(orderByElements.get(i).toString());
            }

            if (windowElement != null) {
                b.append(' ');
                b.append(windowElement);
            }
        }
    }
}
