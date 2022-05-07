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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.joining;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.OutputClause;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.WithItem;

public class Delete implements Statement {

    private List<WithItem> withItemsList;
    private Table table;
    private OracleHint oracleHint = null;
    private List<Table> tables;
    private List<Table> usingList;
    private List<Join> joins;
    private Expression where;
    private Limit limit;
    private List<OrderByElement> orderByElements;
    private boolean hasFrom = true;
    private DeleteModifierPriority modifierPriority;
    private boolean modifierIgnore;
    private boolean modifierQuick;
    private List<SelectItem> returningExpressionList = null;
    private OutputClause outputClause;

    public OutputClause getOutputClause() {
        return outputClause;
    }

    public void setOutputClause(OutputClause outputClause) {
        this.outputClause = outputClause;
    }

    public List<SelectItem> getReturningExpressionList() {
        return returningExpressionList;
    }

    public void setReturningExpressionList(List<SelectItem> returningExpressionList) {
        this.returningExpressionList = returningExpressionList;
    }

    public Delete withReturningExpressionList(List<SelectItem> returningExpressionList) {
        this.returningExpressionList = returningExpressionList;
        return this;
    }

    public List<WithItem> getWithItemsList() {
        return withItemsList;
    }

    public void setWithItemsList(List<WithItem> withItemsList) {
        this.withItemsList = withItemsList;
    }

    public Delete withWithItemsList(List<WithItem> withItemsList) {
        this.setWithItemsList(withItemsList);
        return this;
    }
    
     public Delete addWithItemsList(WithItem... withItemsList) {
        List<WithItem> collection = Optional.ofNullable(getWithItemsList()).orElseGet(ArrayList::new);
        Collections.addAll(collection, withItemsList);
        return this.withWithItemsList(collection);
    }

    public Delete addWithItemsList(Collection<? extends WithItem> withItemsList) {
        List<WithItem> collection = Optional.ofNullable(getWithItemsList()).orElseGet(ArrayList::new);
        collection.addAll(withItemsList);
        return this.withWithItemsList(collection);
    }

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
    
    public OracleHint getOracleHint() {
        return oracleHint;
    }

    public void setOracleHint(OracleHint oracleHint) {
        this.oracleHint = oracleHint;
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

    public List<Table> getUsingList() {
        return usingList;
    }

    public void setUsingList(List<Table> usingList) {
        this.usingList = usingList;
    }

    public List<Join> getJoins() {
        return joins;
    }

    public void setJoins(List<Join> joins) {
        this.joins = joins;
    }

    public boolean isHasFrom() {
        return this.hasFrom;
    }

    public void setHasFrom(boolean hasFrom) {
        this.hasFrom = hasFrom;
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
        
        b.append("DELETE");

        if (modifierPriority != null) {
            b.append(" ").append(modifierPriority.name());
        }
        if (modifierQuick) {
            b.append(" QUICK");
        }
        if (modifierIgnore) {
            b.append(" IGNORE");
        }

        if (tables != null && tables.size() > 0) {
            b.append(" ");
            b.append(tables.stream()
                    .map(Table::toString)
                    .collect(joining(", ")));
        }

        if (outputClause!=null) {
            outputClause.appendTo(b);
        }


        if (hasFrom) {
            b.append(" FROM");
        }
        b.append(" ").append(table);

        if (usingList != null && usingList.size()>0) {
            b.append(" USING ");
            b.append(usingList.stream()
                    .map(Table::toString)
                    .collect(joining(", ")));
        }

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

        if (getReturningExpressionList() != null) {
            b.append(" RETURNING ").append(PlainSelect.
                    getStringList(getReturningExpressionList(), true, false));
        }

        return b.toString();
    }

    public Delete withTables(List<Table> tables) {
        this.setTables(tables);
        return this;
    }

    public Delete withUsingList(List<Table> usingList) {
        this.setUsingList(usingList);
        return this;
    }

    public Delete withJoins(List<Join> joins) {
        this.setJoins(joins);
        return this;
    }

    public Delete withLimit(Limit limit) {
        this.setLimit(limit);
        return this;
    }

    public Delete withOrderByElements(List<OrderByElement> orderByElements) {
        this.setOrderByElements(orderByElements);
        return this;
    }

    public Delete withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public Delete withWhere(Expression where) {
        this.setWhere(where);
        return this;
    }

    public Delete withHasFrom(boolean hasFrom) {
        this.setHasFrom(hasFrom);
        return this;
    }

    public Delete withModifierPriority(DeleteModifierPriority modifierPriority){
        this.setModifierPriority(modifierPriority);
        return this;
    }

    public Delete withModifierIgnore(boolean modifierIgnore){
        this.setModifierIgnore(modifierIgnore);
        return this;
    }

    public Delete withModifierQuick(boolean modifierQuick){
        this.setModifierQuick(modifierQuick);
        return this;
    }

    public void setModifierPriority(DeleteModifierPriority modifierPriority) {
        this.modifierPriority = modifierPriority;
    }

    public DeleteModifierPriority getModifierPriority() {
        return modifierPriority;
    }

    public void setModifierIgnore(boolean modifierIgnore) {
        this.modifierIgnore = modifierIgnore;
    }

    public void setModifierQuick(boolean modifierQuick) {
        this.modifierQuick = modifierQuick;
    }

    public boolean isModifierIgnore() {
        return modifierIgnore;
    }

    public boolean isModifierQuick() {
        return modifierQuick;
    }

    public Delete addTables(Table... tables) {
        List<Table> collection = Optional.ofNullable(getTables()).orElseGet(ArrayList::new);
        Collections.addAll(collection, tables);
        return this.withTables(collection);
    }

    public Delete addTables(Collection<? extends Table> tables) {
        List<Table> collection = Optional.ofNullable(getTables()).orElseGet(ArrayList::new);
        collection.addAll(tables);
        return this.withTables(collection);
    }

    public Delete addUsingList(Table... usingList) {
        List<Table> collection = Optional.ofNullable(getUsingList()).orElseGet(ArrayList::new);
        Collections.addAll(collection, usingList);
        return this.withUsingList(collection);
    }

    public Delete addUsingList(Collection<? extends Table> usingList) {
        List<Table> collection = Optional.ofNullable(getUsingList()).orElseGet(ArrayList::new);
        collection.addAll(usingList);
        return this.withUsingList(collection);
    }

    public Delete addJoins(Join... joins) {
        List<Join> collection = Optional.ofNullable(getJoins()).orElseGet(ArrayList::new);
        Collections.addAll(collection, joins);
        return this.withJoins(collection);
    }

    public Delete addJoins(Collection<? extends Join> joins) {
        List<Join> collection = Optional.ofNullable(getJoins()).orElseGet(ArrayList::new);
        collection.addAll(joins);
        return this.withJoins(collection);
    }

    public Delete addOrderByElements(OrderByElement... orderByElements) {
        List<OrderByElement> collection = Optional.ofNullable(getOrderByElements()).orElseGet(ArrayList::new);
        Collections.addAll(collection, orderByElements);
        return this.withOrderByElements(collection);
    }

    public Delete addOrderByElements(Collection<? extends OrderByElement> orderByElements) {
        List<OrderByElement> collection = Optional.ofNullable(getOrderByElements()).orElseGet(ArrayList::new);
        collection.addAll(orderByElements);
        return this.withOrderByElements(collection);
    }

    public <E extends Expression> E getWhere(Class<E> type) {
        return type.cast(getWhere());
    }
}
