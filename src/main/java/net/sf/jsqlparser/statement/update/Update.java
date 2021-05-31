/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.update;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.WithItem;

@SuppressWarnings({"PMD.CyclomaticComplexity"})
public class Update implements Statement {

    private List<WithItem> withItemsList;
    private Table table;
    private Expression where;
    private List<Column> columns;
    private List<Expression> expressions;
    private FromItem fromItem;
    private List<Join> joins;
    private List<Join> startJoins;
    private Select select;
    private boolean useColumnsBrackets = true;
    private boolean useSelect = false;
    private OracleHint oracleHint = null;
    private List<OrderByElement> orderByElements;
    private Limit limit;
    private boolean returningAllColumns = false;
    private List<SelectExpressionItem> returningExpressionList = null;

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public List<WithItem> getWithItemsList() {
        return withItemsList;
    }

    public void setWithItemsList(List<WithItem> withItemsList) {
        this.withItemsList = withItemsList;
    }

    public Update withWithItemsList(List<WithItem> withItemsList) {
        this.setWithItemsList(withItemsList);
        return this;
    }

    public Update addWithItemsList(WithItem... withItemsList) {
        List<WithItem> collection = Optional.ofNullable(getWithItemsList()).orElseGet(ArrayList::new);
        Collections.addAll(collection, withItemsList);
        return this.withWithItemsList(collection);
    }

    public Update addWithItemsList(Collection<? extends WithItem> withItemsList) {
        List<WithItem> collection = Optional.ofNullable(getWithItemsList()).orElseGet(ArrayList::new);
        collection.addAll(withItemsList);
        return this.withWithItemsList(collection);
    }

    public Table getTable() {
        return table;
    }

    public Expression getWhere() {
        return where;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public void setWhere(Expression expression) {
        where = expression;
    }

    public OracleHint getOracleHint() {
        return oracleHint;
    }

    public void setOracleHint(OracleHint oracleHint) {
        this.oracleHint = oracleHint;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    public void setColumns(List<Column> list) {
        columns = list;
    }

    public void setExpressions(List<Expression> list) {
        expressions = list;
    }

    public FromItem getFromItem() {
        return fromItem;
    }

    public void setFromItem(FromItem fromItem) {
        this.fromItem = fromItem;
    }

    public List<Join> getJoins() {
        return joins;
    }

    public void setJoins(List<Join> joins) {
        this.joins = joins;
    }

    public List<Join> getStartJoins() {
        return startJoins;
    }

    public void setStartJoins(List<Join> startJoins) {
        this.startJoins = startJoins;
    }

    public Select getSelect() {
        return select;
    }

    public void setSelect(Select select) {
        this.select = select;
    }

    public boolean isUseColumnsBrackets() {
        return useColumnsBrackets;
    }

    public void setUseColumnsBrackets(boolean useColumnsBrackets) {
        this.useColumnsBrackets = useColumnsBrackets;
    }

    public boolean isUseSelect() {
        return useSelect;
    }

    public void setUseSelect(boolean useSelect) {
        this.useSelect = useSelect;
    }

    public void setOrderByElements(List<OrderByElement> orderByElements) {
        this.orderByElements = orderByElements;
    }

    public void setLimit(Limit limit) {
        this.limit = limit;
    }

    public List<OrderByElement> getOrderByElements() {
        return orderByElements;
    }

    public Limit getLimit() {
        return limit;
    }

    public boolean isReturningAllColumns() {
        return returningAllColumns;
    }

    public void setReturningAllColumns(boolean returningAllColumns) {
        this.returningAllColumns = returningAllColumns;
    }

    public List<SelectExpressionItem> getReturningExpressionList() {
        return returningExpressionList;
    }

    public void setReturningExpressionList(List<SelectExpressionItem> returningExpressionList) {
        this.returningExpressionList = returningExpressionList;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public String toString() {
        StringBuilder b = new StringBuilder();

        if (withItemsList != null && !withItemsList.isEmpty()) {
            b.append("WITH ");
            for (Iterator<WithItem> iter = withItemsList.iterator(); iter.hasNext();) {
                WithItem withItem = iter.next();
                b.append(withItem);
                if (iter.hasNext()) {
                    b.append(",");
                }
                b.append(" ");
            }
        }
        b.append("UPDATE ");
        b.append(table);
        if (startJoins != null) {
            for (Join join : startJoins) {
                if (join.isSimple()) {
                    b.append(", ").append(join);
                } else {
                    b.append(" ").append(join);
                }
            }
        }
        b.append(" SET ");

        if (!useSelect) {
            for (int i = 0; i < getColumns().size(); i++) {
                if (i != 0) {
                    b.append(", ");
                }
                b.append(columns.get(i)).append(" = ");
                b.append(expressions.get(i));
            }
        } else {
            if (useColumnsBrackets) {
                b.append("(");
            }
            for (int i = 0; i < getColumns().size(); i++) {
                if (i != 0) {
                    b.append(", ");
                }
                b.append(columns.get(i));
            }
            if (useColumnsBrackets) {
                b.append(")");
            }
            b.append(" = ");
            b.append("(").append(select).append(")");
        }

        if (fromItem != null) {
            b.append(" FROM ").append(fromItem);
            if (joins != null) {
                for (Join join : joins) {
                    if (join.isSimple()) {
                        b.append(", ").append(join);
                    } else {
                        b.append(" ").append(join);
                    }
                }
            }
        }

        if (where != null) {
            b.append(" WHERE ");
            b.append(where);
        }
        if (orderByElements != null) {
            b.append(PlainSelect.orderByToString(orderByElements));
        }
        if (limit != null) {
            b.append(limit);
        }

        if (isReturningAllColumns()) {
            b.append(" RETURNING *");
        } else if (getReturningExpressionList() != null) {
            b.append(" RETURNING ").append(PlainSelect.
                    getStringList(getReturningExpressionList(), true, false));
        }

        return b.toString();
    }

    public Update withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public Update withFromItem(FromItem fromItem) {
        this.setFromItem(fromItem);
        return this;
    }

    public Update withJoins(List<Join> joins) {
        this.setJoins(joins);
        return this;
    }

    public Update withStartJoins(List<Join> startJoins) {
        this.setStartJoins(startJoins);
        return this;
    }

    public Update withSelect(Select select) {
        this.setSelect(select);
        return this;
    }

    public Update withUseColumnsBrackets(boolean useColumnsBrackets) {
        this.setUseColumnsBrackets(useColumnsBrackets);
        return this;
    }

    public Update withUseSelect(boolean useSelect) {
        this.setUseSelect(useSelect);
        return this;
    }

    public Update withOrderByElements(List<OrderByElement> orderByElements) {
        this.setOrderByElements(orderByElements);
        return this;
    }

    public Update withLimit(Limit limit) {
        this.setLimit(limit);
        return this;
    }

    public Update withReturningAllColumns(boolean returningAllColumns) {
        this.setReturningAllColumns(returningAllColumns);
        return this;
    }

    public Update withReturningExpressionList(List<SelectExpressionItem> returningExpressionList) {
        this.setReturningExpressionList(returningExpressionList);
        return this;
    }

    public Update withWhere(Expression where) {
        this.setWhere(where);
        return this;
    }

    public Update withColumns(List<Column> columns) {
        this.setColumns(columns);
        return this;
    }

    public Update withExpressions(List<Expression> expressions) {
        this.setExpressions(expressions);
        return this;
    }

    public Update addColumns(Column... columns) {
        List<Column> collection = Optional.ofNullable(getColumns()).orElseGet(ArrayList::new);
        Collections.addAll(collection, columns);
        return this.withColumns(collection);
    }

    public Update addColumns(Collection<? extends Column> columns) {
        List<Column> collection = Optional.ofNullable(getColumns()).orElseGet(ArrayList::new);
        collection.addAll(columns);
        return this.withColumns(collection);
    }

    public Update addExpressions(Expression... expressions) {
        List<Expression> collection = Optional.ofNullable(getExpressions()).orElseGet(ArrayList::new);
        Collections.addAll(collection, expressions);
        return this.withExpressions(collection);
    }

    public Update addExpressions(Collection<? extends Expression> expressions) {
        List<Expression> collection = Optional.ofNullable(getExpressions()).orElseGet(ArrayList::new);
        collection.addAll(expressions);
        return this.withExpressions(collection);
    }

    public Update addJoins(Join... joins) {
        List<Join> collection = Optional.ofNullable(getJoins()).orElseGet(ArrayList::new);
        Collections.addAll(collection, joins);
        return this.withJoins(collection);
    }

    public Update addJoins(Collection<? extends Join> joins) {
        List<Join> collection = Optional.ofNullable(getJoins()).orElseGet(ArrayList::new);
        collection.addAll(joins);
        return this.withJoins(collection);
    }

    public Update addStartJoins(Join... startJoins) {
        List<Join> collection = Optional.ofNullable(getStartJoins()).orElseGet(ArrayList::new);
        Collections.addAll(collection, startJoins);
        return this.withStartJoins(collection);
    }

    public Update addStartJoins(Collection<? extends Join> startJoins) {
        List<Join> collection = Optional.ofNullable(getStartJoins()).orElseGet(ArrayList::new);
        collection.addAll(startJoins);
        return this.withStartJoins(collection);
    }

    public Update addOrderByElements(OrderByElement... orderByElements) {
        List<OrderByElement> collection = Optional.ofNullable(getOrderByElements()).orElseGet(ArrayList::new);
        Collections.addAll(collection, orderByElements);
        return this.withOrderByElements(collection);
    }

    public Update addOrderByElements(Collection<? extends OrderByElement> orderByElements) {
        List<OrderByElement> collection = Optional.ofNullable(getOrderByElements()).orElseGet(ArrayList::new);
        collection.addAll(orderByElements);
        return this.withOrderByElements(collection);
    }

    public Update addReturningExpressionList(SelectExpressionItem... returningExpressionList) {
        List<SelectExpressionItem> collection = Optional.ofNullable(getReturningExpressionList()).orElseGet(ArrayList::new);
        Collections.addAll(collection, returningExpressionList);
        return this.withReturningExpressionList(collection);
    }

    public Update addReturningExpressionList(Collection<? extends SelectExpressionItem> returningExpressionList) {
        List<SelectExpressionItem> collection = Optional.ofNullable(getReturningExpressionList()).orElseGet(ArrayList::new);
        collection.addAll(returningExpressionList);
        return this.withReturningExpressionList(collection);
    }

    public <E extends Expression> E getWhere(Class<E> type) {
        return type.cast(getWhere());
    }

    public <E extends FromItem> E getFromItem(Class<E> type) {
        return type.cast(getFromItem());
    }
}
