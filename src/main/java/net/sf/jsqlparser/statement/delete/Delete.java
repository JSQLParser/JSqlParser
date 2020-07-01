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

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.util.List;
import static java.util.stream.Collectors.joining;

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
}
