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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.function.Consumer;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;

public class GroupByElement implements Serializable {
    public enum SortDirection {
        ASC, DESC
    }

    private final List<SortDirection> groupBySortDirections = new ArrayList<>();
    private ExpressionList<Expression> groupByExpressions = new ExpressionList<>();
    private List<ExpressionList<Expression>> groupingSets = new ArrayList<>();
    // postgres rollup is an ExpressionList
    private boolean mysqlWithRollup = false;
    private boolean distinct = false;

    public boolean isUsingBrackets() {
        return groupByExpressions.isUsingBrackets();
    }

    public boolean isDistinct() {
        return distinct;
    }

    public GroupByElement setDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    public <T, S> T accept(GroupByVisitor<T> groupByVisitor, S context) {
        return groupByVisitor.visit(this, context);
    }

    public ExpressionList<Expression> getGroupByExpressionList() {
        return groupByExpressions;
    }

    @Deprecated
    public ExpressionList<Expression> getGroupByExpressions() {
        return groupByExpressions;
    }

    public void setGroupByExpressions(ExpressionList<Expression> groupByExpressions) {
        if (this.groupByExpressions != groupByExpressions) {
            groupBySortDirections.clear();
        }
        this.groupByExpressions = groupByExpressions;
    }

    @Deprecated
    public void addGroupByExpression(Expression groupByExpression) {
        if (groupByExpressions.getExpressions() == null) {
            groupByExpressions.setExpressions(new ArrayList<>());
        }
        groupByExpressions.add(groupByExpression);
    }

    public List<ExpressionList<Expression>> getGroupingSets() {
        return groupingSets;
    }

    public void setGroupingSets(List<ExpressionList<Expression>> groupingSets) {
        this.groupingSets = groupingSets;
    }

    public void addGroupingSet(ExpressionList<Expression> list) {
        this.groupingSets.add(list);
    }

    /** Returns the explicit direction at a grouping-list position, or null if omitted. */
    public SortDirection getGroupBySortDirection(int index) {
        Objects.checkIndex(index, groupByExpressions.size());
        return index < groupBySortDirections.size() ? groupBySortDirections.get(index) : null;
    }

    /** Directions belong to list positions. Replacing the expression list clears them. */
    public void setGroupBySortDirection(int index, SortDirection direction) {
        Objects.checkIndex(index, groupByExpressions.size());
        while (groupBySortDirections.size() <= index) {
            groupBySortDirections.add(null);
        }
        groupBySortDirections.set(index, direction);
    }

    public boolean hasGroupBySortDirections() {
        if (groupByExpressions != null && !groupBySortDirections.isEmpty()) {
            for (int i = 0; i < groupByExpressions.size(); i++) {
                if (getGroupBySortDirection(i) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        appendTo(builder, builder::append, builder::append);
        return builder.toString();
    }

    /** Shares clause layout while letting a deparser visit each expression. */
    public void appendTo(StringBuilder builder, Consumer<ExpressionList<?>> listRenderer,
            Consumer<Expression> expressionRenderer) {
        builder.append("GROUP BY ");
        if (distinct) {
            builder.append("DISTINCT ");
        }
        if (groupByExpressions != null) {
            if (hasGroupBySortDirections()) {
                appendOrderedExpressions(builder, expressionRenderer);
            } else {
                listRenderer.accept(groupByExpressions);
            }
        }
        if (!groupingSets.isEmpty()) {
            if (builder.charAt(builder.length() - 1) != ' ') {
                builder.append(' ');
            }
            builder.append("GROUPING SETS (");
            for (int i = 0; i < groupingSets.size(); i++) {
                builder.append(i > 0 ? ", " : "");
                listRenderer.accept(groupingSets.get(i));
            }
            builder.append(")");
        }
        if (isMysqlWithRollup()) {
            builder.append(" WITH ROLLUP");
        }
    }

    private void appendOrderedExpressions(StringBuilder builder,
            Consumer<Expression> expressionRenderer) {
        boolean brackets = groupByExpressions instanceof ParenthesedExpressionList<?>;
        if (brackets) {
            builder.append('(');
        }
        for (int i = 0; i < groupByExpressions.size(); i++) {
            builder.append(i > 0 ? ", " : "");
            expressionRenderer.accept(groupByExpressions.get(i));
            SortDirection direction = getGroupBySortDirection(i);
            if (direction != null) {
                builder.append(' ').append(direction);
            }
        }
        if (brackets) {
            builder.append(')');
        }
    }

    public GroupByElement withGroupByExpressions(ExpressionList<Expression> groupByExpressions) {
        this.setGroupByExpressions(groupByExpressions);
        return this;
    }

    public GroupByElement withGroupingSets(List<ExpressionList<Expression>> groupingSets) {
        this.setGroupingSets(groupingSets);
        return this;
    }

    public GroupByElement addGroupByExpressions(Expression... groupByExpressions) {
        return this.addGroupByExpressions(Arrays.asList(groupByExpressions));
    }

    public GroupByElement addGroupByExpressions(
            Collection<? extends Expression> groupByExpressions) {
        ExpressionList<Expression> collection =
                Optional.ofNullable(getGroupByExpressionList()).orElseGet(ExpressionList::new);
        collection.addAll(groupByExpressions);
        return this.withGroupByExpressions(collection);
    }

    public GroupByElement addGroupingSets(Object... groupingSets) {
        List collection = Optional.ofNullable(getGroupingSets()).orElseGet(ArrayList::new);
        Collections.addAll(collection, groupingSets);
        return this.withGroupingSets(collection);
    }

    public GroupByElement addGroupingSets(
            Collection<List<ExpressionList<Expression>>> groupingSets) {
        List collection = Optional.ofNullable(getGroupingSets()).orElseGet(ArrayList::new);
        collection.addAll(groupingSets);
        return this.withGroupingSets(collection);
    }

    public boolean isMysqlWithRollup() {
        return mysqlWithRollup;
    }

    public GroupByElement setMysqlWithRollup(boolean mysqlWithRollup) {
        this.mysqlWithRollup = mysqlWithRollup;
        return this;
    }
}
