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

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public abstract class SelectBody extends ASTNodeAccessImpl implements Expression {
    List<WithItem> withItemsList;
    Limit limit;
    Offset offset;
    Fetch fetch;
    WithIsolation isolation;
    boolean oracleSiblings = false;
    List<OrderByElement> orderByElements;

    public static String orderByToString(List<OrderByElement> orderByElements) {
        return orderByToString(false, orderByElements);
    }

    public static String orderByToString(boolean oracleSiblings,
            List<OrderByElement> orderByElements) {
        return getFormattedList(orderByElements, oracleSiblings ? "ORDER SIBLINGS BY" : "ORDER BY");
    }

    public static String getFormattedList(List<?> list, String expression) {
        return getFormattedList(list, expression, true, false);
    }

    public static String getFormattedList(List<?> list, String expression, boolean useComma,
            boolean useBrackets) {
        String sql = getStringList(list, useComma, useBrackets);

        if (sql.length() > 0) {
            if (expression.length() > 0) {
                sql = " " + expression + " " + sql;
            } else {
                sql = " " + sql;
            }
        }

        return sql;
    }

    /**
     * List the toString out put of the objects in the List comma separated. If the List is null or
     * empty an empty string is returned.
     * <p>
     * The same as getStringList(list, true, false)
     *
     * @see #getStringList(List, boolean, boolean)
     * @param list list of objects with toString methods
     * @return comma separated list of the elements in the list
     */
    public static String getStringList(List<?> list) {
        return getStringList(list, true, false);
    }

    /**
     * List the toString out put of the objects in the List that can be comma separated. If the List
     * is null or empty an empty string is returned.
     *
     * @param list list of objects with toString methods
     * @param useComma true if the list has to be comma separated
     * @param useBrackets true if the list has to be enclosed in brackets
     * @return comma separated list of the elements in the list
     */
    public static String getStringList(List<?> list, boolean useComma, boolean useBrackets) {
        return appendStringListTo(new StringBuilder(), list, useComma, useBrackets).toString();
    }

    /**
     * Append the toString out put of the objects in the List (that can be comma separated). If the
     * List is null or empty an empty string is returned.
     *
     * @param list list of objects with toString methods
     * @param useComma true if the list has to be comma separated
     * @param useBrackets true if the list has to be enclosed in brackets
     * @return comma separated list of the elements in the list
     */
    public static StringBuilder appendStringListTo(StringBuilder builder, List<?> list,
            boolean useComma, boolean useBrackets) {
        if (list != null) {
            String comma = useComma ? ", " : " ";

            if (useBrackets) {
                builder.append("(");
            }

            int size = list.size();
            for (int i = 0; i < size; i++) {
                builder.append(list.get(i)).append(i < size - 1 ? comma : "");
            }

            if (useBrackets) {
                builder.append(")");
            }
        }
        return builder;
    }

    public List<WithItem> getWithItemsList() {
        return withItemsList;
    }

    public void setWithItemsList(List<WithItem> withItemsList) {
        this.withItemsList = withItemsList;
    }

    public SelectBody withWithItemsList(List<WithItem> withItemsList) {
        this.setWithItemsList(withItemsList);
        return this;
    }

    public SelectBody addWithItemsList(Collection<? extends WithItem> withItemsList) {
        List<WithItem> collection =
                Optional.ofNullable(getWithItemsList()).orElseGet(ArrayList::new);
        collection.addAll(withItemsList);
        return this.withWithItemsList(collection);
    }

    public SelectBody addWithItemsList(WithItem... withItemsList) {
        return addWithItemsList(Arrays.asList(withItemsList));
    }

    public boolean isOracleSiblings() {
        return oracleSiblings;
    }

    public void setOracleSiblings(boolean oracleSiblings) {
        this.oracleSiblings = oracleSiblings;
    }

    public SelectBody withOracleSiblings(boolean oracleSiblings) {
        this.setOracleSiblings(oracleSiblings);
        return this;
    }

    public List<OrderByElement> getOrderByElements() {
        return orderByElements;
    }

    public void setOrderByElements(List<OrderByElement> orderByElements) {
        this.orderByElements = orderByElements;
    }

    public SelectBody withOrderByElements(List<OrderByElement> orderByElements) {
        this.setOrderByElements(orderByElements);
        return this;
    }

    public SelectBody addOrderByElements(Collection<? extends OrderByElement> orderByElements) {
        List<OrderByElement> collection =
                Optional.ofNullable(getOrderByElements()).orElseGet(ArrayList::new);
        collection.addAll(orderByElements);
        return this.withOrderByElements(collection);
    }

    public SelectBody addOrderByElements(OrderByElement... orderByElements) {
        return addOrderByElements(Arrays.asList(orderByElements));
    }

    public Limit getLimit() {
        return limit;
    }

    public void setLimit(Limit limit) {
        this.limit = limit;
    }

    public SelectBody withLimit(Limit limit) {
        this.setLimit(limit);
        return this;
    }

    public Offset getOffset() {
        return offset;
    }

    public void setOffset(Offset offset) {
        this.offset = offset;
    }

    public SelectBody withOffset(Offset offset) {
        this.setOffset(offset);
        return this;
    }

    public Fetch getFetch() {
        return fetch;
    }

    public void setFetch(Fetch fetch) {
        this.fetch = fetch;
    }

    public SelectBody withFetch(Fetch fetch) {
        this.setFetch(fetch);
        return this;
    }

    public WithIsolation getIsolation() {
        return isolation;
    }

    public void setIsolation(WithIsolation isolation) {
        this.isolation = isolation;
    }

    public SelectBody withIsolation(WithIsolation isolation) {
        this.setIsolation(isolation);
        return this;
    }

    public abstract StringBuilder appendSelectBodyTo(StringBuilder builder);

    public StringBuilder appendTo(StringBuilder builder) {
        if (withItemsList != null && !withItemsList.isEmpty()) {
            builder.append("WITH ");
            for (Iterator<WithItem> iter = withItemsList.iterator(); iter.hasNext();) {
                WithItem withItem = iter.next();
                builder.append(withItem);
                if (iter.hasNext()) {
                    builder.append(",");
                }
                builder.append(" ");
            }
        }

        appendSelectBodyTo(builder);

        builder.append(orderByToString(oracleSiblings, orderByElements));

        if (limit != null) {
            builder.append(limit);
        }
        if (offset != null) {
            builder.append(offset);
        }
        if (fetch != null) {
            builder.append(fetch);
        }
        if (isolation != null) {
            builder.append(isolation);
        }

        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    public abstract void accept(SelectVisitor selectVisitor);

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

}
