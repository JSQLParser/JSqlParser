/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.delete;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static java.util.stream.Collectors.joining;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class Delete implements Statement {

    private Table table;
    private List<Table> tables;
    private List<Join> joins;
    private Expression where;
    private Limit limit;
    private List<OrderByElement> orderByElements;

    public List<OrderByElement> getOrderByElements() {
        return orderByElements;
    }

    public void setOrderByElements(List<OrderByElement> orderByElements) {
        this.orderByElements = orderByElements;
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public Table getTable() {
        return table;
    }

    public Expression getWhere() {
        return where;
    }

    public void setTable(Table name) {
        table = name;
    }

    public void setWhere(Expression expression) {
        where = expression;
    }

    public Limit getLimit() {
        return limit;
    }

    public void setLimit(Limit limit) {
        this.limit = limit;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public List<Join> getJoins() {
        return joins;
    }

    public void setJoins(List<Join> joins) {
        this.joins = joins;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("DELETE");

        if (tables != null && tables.size() > 0) {
            b.append(" ");
            b.append(tables.stream()
                    .map(t -> t.toString())
                    .collect(joining(", ")));
        }

        b.append(" FROM ");
        b.append(table);

        if (joins != null) {
            for (Join join : joins) {
                if (join.isSimple()) {
                    b.append(", ").append(join);
                } else {
                    b.append(" ").append(join);
                }
            }
        }

        if (where != null) {
            b.append(" WHERE ").append(where);
        }

        if (orderByElements != null) {
            b.append(PlainSelect.orderByToString(orderByElements));
        }

        if (limit != null) {
            b.append(limit);
        }
        return b.toString();
    }

    public Delete tables(List<Table> tables) {
        this.setTables(tables);
        return this;
    }

    public Delete joins(List<Join> joins) {
        this.setJoins(joins);
        return this;
    }

    public Delete limit(Limit limit) {
        this.setLimit(limit);
        return this;
    }

    public Delete orderByElements(List<OrderByElement> orderByElements) {
        this.setOrderByElements(orderByElements);
        return this;
    }

    public Delete table(Table table) {
        this.setTable(table);
        return this;
    }

    public Delete where(Expression where) {
        this.setWhere(where);
        return this;
    }

    public Delete addTables(Table... tables) {
        List<Table> collection = Optional.ofNullable(getTables()).orElseGet(ArrayList::new);
        Collections.addAll(collection, tables);
        return this.tables(collection);
    }

    public Delete addTables(Collection<? extends Table> tables) {
        List<Table> collection = Optional.ofNullable(getTables()).orElseGet(ArrayList::new);
        collection.addAll(tables);
        return this.tables(collection);
    }

    public Delete addJoins(Join... joins) {
        List<Join> collection = Optional.ofNullable(getJoins()).orElseGet(ArrayList::new);
        Collections.addAll(collection, joins);
        return this.joins(collection);
    }

    public Delete addJoins(Collection<? extends Join> joins) {
        List<Join> collection = Optional.ofNullable(getJoins()).orElseGet(ArrayList::new);
        collection.addAll(joins);
        return this.joins(collection);
    }

    public Delete addOrderByElements(OrderByElement... orderByElements) {
        List<OrderByElement> collection = Optional.ofNullable(getOrderByElements()).orElseGet(ArrayList::new);
        Collections.addAll(collection, orderByElements);
        return this.orderByElements(collection);
    }

    public Delete addOrderByElements(Collection<? extends OrderByElement> orderByElements) {
        List<OrderByElement> collection = Optional.ofNullable(getOrderByElements()).orElseGet(ArrayList::new);
        collection.addAll(orderByElements);
        return this.orderByElements(collection);
    }

    public <E extends Expression> E getWhere(Class<? extends E> type) {
        return type.cast(getWhere());
    }
}
