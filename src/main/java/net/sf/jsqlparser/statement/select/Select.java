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
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public abstract class Select extends ASTNodeAccessImpl implements Statement, Expression, FromItem {
    protected Table forUpdateTable = null;
    protected List<WithItem<?>> withItemsList;
    Limit limitBy;
    Limit limit;
    Offset offset;
    Fetch fetch;
    WithIsolation isolation;
    boolean oracleSiblings = false;

    ForClause forClause = null;

    List<OrderByElement> orderByElements;
    ForMode forMode = null;
    private boolean skipLocked;
    private Wait wait;
    private boolean noWait = false;
    Alias alias;
    Pivot pivot;
    UnPivot unPivot;

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

        if (!sql.isEmpty()) {
            if (!expression.isEmpty()) {
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
     * @param list list of objects with toString methods
     * @return comma separated list of the elements in the list
     * @see #getStringList(List, boolean, boolean)
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

    public List<WithItem<?>> getWithItemsList() {
        return withItemsList;
    }

    public void setWithItemsList(List<WithItem<?>> withItemsList) {
        this.withItemsList = withItemsList;
    }

    public Select withWithItemsList(List<WithItem<?>> withItemsList) {
        this.setWithItemsList(withItemsList);
        return this;
    }

    public Select addWithItemsList(Collection<? extends WithItem<?>> withItemsList) {
        List<WithItem<?>> collection =
                Optional.ofNullable(getWithItemsList()).orElseGet(ArrayList::new);
        collection.addAll(withItemsList);
        return this.withWithItemsList(collection);
    }

    public Select addWithItemsList(WithItem<?>... withItemsList) {
        return addWithItemsList(Arrays.asList(withItemsList));
    }

    public boolean isOracleSiblings() {
        return oracleSiblings;
    }

    public void setOracleSiblings(boolean oracleSiblings) {
        this.oracleSiblings = oracleSiblings;
    }

    public boolean isNoWait() {
        return this.noWait;
    }

    public void setNoWait(boolean noWait) {
        this.noWait = noWait;
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
        return this.addOrderByElements(Arrays.asList(orderByElements));
    }

    public Select addOrderByExpressions(Collection<Expression> orderByExpressions) {
        for (Expression e : orderByExpressions) {
            addOrderByElements(new OrderByElement().withExpression(e));
        }
        return this;
    }

    public Select addOrderByElements(Expression... orderByExpressions) {
        return addOrderByExpressions(Arrays.asList(orderByExpressions));
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

    public ForMode getForMode() {
        return this.forMode;
    }

    public void setForMode(ForMode forMode) {
        this.forMode = forMode;
    }

    public Table getForUpdateTable() {
        return this.forUpdateTable;
    }

    public void setForUpdateTable(Table forUpdateTable) {
        this.forUpdateTable = forUpdateTable;
    }

    /**
     * Returns the value of the {@link Wait} set for this SELECT
     *
     * @return the value of the {@link Wait} set for this SELECT
     */
    public Wait getWait() {
        return wait;
    }

    /**
     * Sets the {@link Wait} for this SELECT
     *
     * @param wait the {@link Wait} for this SELECT
     */
    public void setWait(final Wait wait) {
        this.wait = wait;
    }

    public boolean isSkipLocked() {
        return skipLocked;
    }

    public void setSkipLocked(boolean skipLocked) {
        this.skipLocked = skipLocked;
    }

    @Override
    public Alias getAlias() {
        return alias;
    }

    @Override
    public void setAlias(Alias alias) {
        this.alias = alias;
    }

    public Select withAlias(Alias alias) {
        this.setAlias(alias);
        return this;
    }

    @Override
    public Pivot getPivot() {
        return pivot;
    }

    @Override
    public void setPivot(Pivot pivot) {
        this.pivot = pivot;
    }

    public UnPivot getUnPivot() {
        return unPivot;
    }

    public void setUnPivot(UnPivot unPivot) {
        this.unPivot = unPivot;
    }

    public StringBuilder appendSelectBodyTo(StringBuilder builder) {
        return builder;
    };

    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    public StringBuilder appendTo(StringBuilder builder) {
        if (withItemsList != null && !withItemsList.isEmpty()) {
            builder.append("WITH ");
            for (Iterator<WithItem<?>> iter = withItemsList.iterator(); iter.hasNext();) {
                WithItem withItem = iter.next();
                builder.append(withItem);
                if (iter.hasNext()) {
                    builder.append(",");
                }
                builder.append(" ");
            }
        }

        appendSelectBodyTo(builder);

        appendTo(builder, alias, null, pivot, unPivot);

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
        if (forMode != null) {
            builder.append(" FOR ");
            builder.append(forMode.getValue());

            if (getForUpdateTable() != null) {
                builder.append(" OF ").append(forUpdateTable);
            }

            if (wait != null) {
                // Wait's toString will do the formatting for us
                builder.append(wait);
            }

            if (isNoWait()) {
                builder.append(" NOWAIT");
            } else if (isSkipLocked()) {
                builder.append(" SKIP LOCKED");
            }
        }

        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    public abstract <T, S> T accept(SelectVisitor<T> selectVisitor, S context);

    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
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

    public Select withForMode(ForMode forMode) {
        this.setForMode(forMode);
        return this;
    }

    public Select withForUpdateTable(Table forUpdateTable) {
        this.setForUpdateTable(forUpdateTable);
        return this;
    }

    public Select withSkipLocked(boolean skipLocked) {
        this.setSkipLocked(skipLocked);
        return this;
    }

    public Select withWait(Wait wait) {
        this.setWait(wait);
        return this;
    }
}
