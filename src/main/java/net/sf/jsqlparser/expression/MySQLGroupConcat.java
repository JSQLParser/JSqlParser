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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class MySQLGroupConcat extends ASTNodeAccessImpl implements Expression {

    private ExpressionList expressionList;
    private boolean distinct = false;
    private List<OrderByElement> orderByElements;
    private String separator;

    public ExpressionList getExpressionList() {
        return expressionList;
    }

    public void setExpressionList(ExpressionList expressionList) {
        this.expressionList = expressionList;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public List<OrderByElement> getOrderByElements() {
        return orderByElements;
    }

    public void setOrderByElements(List<OrderByElement> orderByElements) {
        this.orderByElements = orderByElements;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("GROUP_CONCAT(");
        if (isDistinct()) {
            b.append("DISTINCT ");
        }
        b.append(PlainSelect.getStringList(expressionList.getExpressions(), true, false));
        if (orderByElements != null && !orderByElements.isEmpty()) {
            b.append(" ORDER BY ");
            for (int i = 0; i < orderByElements.size(); i++) {
                if (i > 0) {
                    b.append(", ");
                }
                b.append(orderByElements.get(i).toString());
            }
        }
        if (separator != null) {
            b.append(" SEPARATOR ").append(separator);
        }
        b.append(")");
        return b.toString();
    }

    public MySQLGroupConcat withExpressionList(ExpressionList expressionList) {
        this.setExpressionList(expressionList);
        return this;
    }

    public MySQLGroupConcat withDistinct(boolean distinct) {
        this.setDistinct(distinct);
        return this;
    }

    public MySQLGroupConcat withOrderByElements(List<OrderByElement> orderByElements) {
        this.setOrderByElements(orderByElements);
        return this;
    }

    public MySQLGroupConcat withSeparator(String separator) {
        this.setSeparator(separator);
        return this;
    }

    public MySQLGroupConcat addOrderByElements(OrderByElement... orderByElements) {
        List<OrderByElement> collection = Optional.ofNullable(getOrderByElements()).orElseGet(ArrayList::new);
        Collections.addAll(collection, orderByElements);
        return this.withOrderByElements(collection);
    }

    public MySQLGroupConcat addOrderByElements(Collection<? extends OrderByElement> orderByElements) {
        List<OrderByElement> collection = Optional.ofNullable(getOrderByElements()).orElseGet(ArrayList::new);
        collection.addAll(orderByElements);
        return this.withOrderByElements(collection);
    }
}
