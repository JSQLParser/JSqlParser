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
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.util.SelectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public abstract class Select extends ASTNodeAccessImpl implements Statement, Expression {
    List<WithItem> withItemsList;
    Limit limitBy;
    Limit limit;
    Offset offset;
    Fetch fetch;
    WithIsolation isolation;
    boolean oracleSiblings = false;

    ForClause forClause = null;

    List<OrderByElement> orderByElements;

    public static String orderByToString(List<OrderByElement> orderByElements) {
        return orderByToString(false, orderByElements);
    }

    public static String orderByToString(boolean oracleSiblings,
            List<OrderByElement> orderByElements) {
        return SelectUtils.getFormattedList(orderByElements, oracleSiblings ? "ORDER SIBLINGS BY" : "ORDER BY");
    }

    public List<WithItem> getWithItemsList() {
        return withItemsList;
    }

    public void setWithItemsList(List<WithItem> withItemsList) {
        this.withItemsList = withItemsList;
    }

    public Select withWithItemsList(List<WithItem> withItemsList) {
        this.setWithItemsList(withItemsList);
        return this;
    }

    public Select addWithItemsList(Collection<? extends WithItem> withItemsList) {
        List<WithItem> collection =
                Optional.ofNullable(getWithItemsList()).orElseGet(ArrayList::new);
        collection.addAll(withItemsList);
        return this.withWithItemsList(collection);
    }

    public Select addWithItemsList(WithItem... withItemsList) {
        return addWithItemsList(Arrays.asList(withItemsList));
    }

    public boolean isOracleSiblings() {
        return oracleSiblings;
    }

    public void setOracleSiblings(boolean oracleSiblings) {
        this.oracleSiblings = oracleSiblings;
    }

    public Select withOracleSiblings(boolean oracleSiblings) {
        this.setOracleSiblings(oracleSiblings);
        return this;
    }

    public ForClause getForClause() {
        return forClause;
    }

    public Select setForClause(ForClause forClause) {
        this.forClause = forClause;
        return this;
    }

    public List<OrderByElement> getOrderByElements() {
        return orderByElements;
    }

    public void setOrderByElements(List<OrderByElement> orderByElements) {
        this.orderByElements = orderByElements;
    }

    public Select withOrderByElements(List<OrderByElement> orderByElements) {
        this.setOrderByElements(orderByElements);
        return this;
    }

    public Select addOrderByElements(Collection<? extends OrderByElement> orderByElements) {
        List<OrderByElement> collection =
                Optional.ofNullable(getOrderByElements()).orElseGet(ArrayList::new);
        collection.addAll(orderByElements);
        return this.withOrderByElements(collection);
    }

    public Select addOrderByElements(OrderByElement... orderByElements) {
        return addOrderByElements(Arrays.asList(orderByElements));
    }

    public Limit getLimit() {
        return limit;
    }

    public void setLimit(Limit limit) {
        this.limit = limit;
    }

    public Select withLimit(Limit limit) {
        this.setLimit(limit);
        return this;
    }

    public Limit getLimitBy() {
        return limitBy;
    }

    public void setLimitBy(Limit limitBy) {
        this.limitBy = limitBy;
    }

    public <E extends Select> E withLimitBy(Class<E> type, Limit limitBy) {
        this.setLimitBy(limitBy);
        return type.cast(this);
    }

    public Offset getOffset() {
        return offset;
    }

    public void setOffset(Offset offset) {
        this.offset = offset;
    }

    public Select withOffset(Offset offset) {
        this.setOffset(offset);
        return this;
    }

    public Fetch getFetch() {
        return fetch;
    }

    public void setFetch(Fetch fetch) {
        this.fetch = fetch;
    }

    public Select withFetch(Fetch fetch) {
        this.setFetch(fetch);
        return this;
    }

    public WithIsolation getIsolation() {
        return isolation;
    }

    public void setIsolation(WithIsolation isolation) {
        this.isolation = isolation;
    }

    public Select withIsolation(WithIsolation isolation) {
        this.setIsolation(isolation);
        return this;
    }

    public abstract StringBuilder appendSelectBodyTo(StringBuilder builder);

    @SuppressWarnings({"PMD.CyclomaticComplexity"})
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

        if (forClause != null) {
            forClause.appendTo(builder);
        }

        builder.append(orderByToString(oracleSiblings, orderByElements));

        if (limitBy != null) {
            builder.append(limitBy);
        }
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

    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Deprecated
    public Select getSelectBody() {
        return this;
    }

    public Values getValues() {
        return (Values) this;
    }

    public PlainSelect getPlainSelect() {
        return (PlainSelect) this;
    }

    public SetOperationList getSetOperationList() {
        return (SetOperationList) this;
    }

    public <E extends Select> E as(Class<E> type) {
        return type.cast(this);
    }
}
