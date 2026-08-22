/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;

/**
 * DuckDB's simplified {@code PIVOT} query.
 *
 * <p>
 * Unlike {@link Pivot}, which is a clause attached to a {@link FromItem}, this construct is a query
 * in its own right and can therefore be used as a statement, CTE, or parenthesized subquery.
 *
 * @see <a href="https://duckdb.org/docs/sql/statements/pivot.html">DuckDB PIVOT</a>
 */
public class PivotQuery extends Select {

    private FromItem fromItem;
    private ExpressionList<Expression> onExpressions;
    private List<SelectItem<Function>> usingItems;
    private ExpressionList<Expression> groupByExpressions;

    public FromItem getFromItem() {
        return fromItem;
    }

    public void setFromItem(FromItem fromItem) {
        this.fromItem = fromItem;
    }

    public PivotQuery withFromItem(FromItem fromItem) {
        setFromItem(fromItem);
        return this;
    }

    public ExpressionList<Expression> getOnExpressions() {
        return onExpressions;
    }

    public void setOnExpressions(ExpressionList<Expression> onExpressions) {
        this.onExpressions = onExpressions;
    }

    public PivotQuery withOnExpressions(ExpressionList<Expression> onExpressions) {
        setOnExpressions(onExpressions);
        return this;
    }

    public PivotQuery addOnExpressions(Expression... expressions) {
        return addOnExpressions(Arrays.asList(expressions));
    }

    public PivotQuery addOnExpressions(Collection<? extends Expression> expressions) {
        ExpressionList<Expression> collection =
                Optional.ofNullable(getOnExpressions()).orElseGet(ExpressionList::new);
        collection.addAll(expressions);
        return withOnExpressions(collection);
    }

    public List<SelectItem<Function>> getUsingItems() {
        return usingItems;
    }

    public void setUsingItems(List<SelectItem<Function>> usingItems) {
        this.usingItems = usingItems;
    }

    public PivotQuery withUsingItems(List<SelectItem<Function>> usingItems) {
        setUsingItems(usingItems);
        return this;
    }

    public PivotQuery addUsingItems(SelectItem<Function>... usingItems) {
        List<SelectItem<Function>> collection =
                Optional.ofNullable(getUsingItems()).orElseGet(ArrayList::new);
        Collections.addAll(collection, usingItems);
        return withUsingItems(collection);
    }

    public PivotQuery addUsingItems(Collection<? extends SelectItem<Function>> usingItems) {
        List<SelectItem<Function>> collection =
                Optional.ofNullable(getUsingItems()).orElseGet(ArrayList::new);
        collection.addAll(usingItems);
        return withUsingItems(collection);
    }

    public ExpressionList<Expression> getGroupByExpressions() {
        return groupByExpressions;
    }

    public void setGroupByExpressions(ExpressionList<Expression> groupByExpressions) {
        this.groupByExpressions = groupByExpressions;
    }

    public PivotQuery withGroupByExpressions(ExpressionList<Expression> groupByExpressions) {
        setGroupByExpressions(groupByExpressions);
        return this;
    }

    public PivotQuery addGroupByExpressions(Expression... expressions) {
        return addGroupByExpressions(Arrays.asList(expressions));
    }

    public PivotQuery addGroupByExpressions(Collection<? extends Expression> expressions) {
        ExpressionList<Expression> collection =
                Optional.ofNullable(getGroupByExpressions()).orElseGet(ExpressionList::new);
        collection.addAll(expressions);
        return withGroupByExpressions(collection);
    }

    @Override
    public StringBuilder appendSelectBodyTo(StringBuilder builder) {
        builder.append("PIVOT ").append(fromItem);
        if (onExpressions != null) {
            builder.append(" ON ").append(onExpressions);
        }
        if (usingItems != null) {
            builder.append(" USING ").append(Select.getStringList(usingItems));
        }
        if (groupByExpressions != null) {
            builder.append(" GROUP BY ").append(groupByExpressions);
        }
        return builder;
    }

    @Override
    public <T, S> T accept(SelectVisitor<T> selectVisitor, S context) {
        return selectVisitor.visit(this, context);
    }

    @Override
    public <T, S> T accept(FromItemVisitor<T> fromItemVisitor, S context) {
        return fromItemVisitor.visit(this, context);
    }

    @Override
    public SampleClause getSampleClause() {
        return null;
    }

    @Override
    public FromItem setSampleClause(SampleClause sampleClause) {
        return null;
    }
}
