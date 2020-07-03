/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.replace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class Replace implements Statement {

    private Table table;
    private List<Column> columns;
    private ItemsList itemsList;
    private List<Expression> expressions;
    private boolean useValues = true;
    private boolean useIntoTables = false;

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table name) {
        table = name;
    }

    public boolean isUseIntoTables() {
        return useIntoTables;
    }

    public void setUseIntoTables(boolean useIntoTables) {
        this.useIntoTables = useIntoTables;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public ItemsList getItemsList() {
        return itemsList;
    }

    public void setColumns(List<Column> list) {
        columns = list;
    }

    public void setItemsList(ItemsList list) {
        itemsList = list;
    }

    /**
     * A list of {@link net.sf.jsqlparser.expression.Expression}s (from a "REPLACE mytab SET
     * col1=exp1, col2=exp2"). <br>
     * it is null in case of a "REPLACE mytab (col1, col2) [...]"
     */
    public List<Expression> getExpressions() {
        return expressions;
    }

    public void setExpressions(List<Expression> list) {
        expressions = list;
    }

    public boolean isUseValues() {
        return useValues;
    }

    public void setUseValues(boolean useValues) {
        this.useValues = useValues;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        sql.append("REPLACE ");
        if (isUseIntoTables()) {
            sql.append("INTO ");
        }
        sql.append(table);

        if (expressions != null && columns != null) {
            // the SET col1=exp1, col2=exp2 case
            sql.append(" SET ");
            // each element from expressions match up with a column from columns.
            for (int i = 0, s = columns.size(); i < s; i++) {
                sql.append(columns.get(i)).append("=").append(expressions.get(i));
                sql.append((i < s - 1) ? ", " : "");
            }
        } else if (columns != null) {
            // the REPLACE mytab (col1, col2) [...] case
            sql.append(" ").append(PlainSelect.getStringList(columns, true, true));
        }

        if (itemsList != null) {
            // REPLACE mytab SELECT * FROM mytab2
            // or VALUES ('as', ?, 565)

            if (useValues) {
                sql.append(" VALUES");
            }

            sql.append(" ").append(itemsList);
        }

        return sql.toString();
    }

    public Replace useValues(boolean useValues) {
        this.setUseValues(useValues);
        return this;
    }

    public Replace useIntoTables(boolean useIntoTables) {
        this.setUseIntoTables(useIntoTables);
        return this;
    }

    public Replace table(Table table) {
        this.setTable(table);
        return this;
    }

    public Replace columns(List<Column> columns) {
        this.setColumns(columns);
        return this;
    }

    public Replace itemsList(ItemsList itemsList) {
        this.setItemsList(itemsList);
        return this;
    }

    public Replace expressions(List<Expression> expressions) {
        this.setExpressions(expressions);
        return this;
    }

    public Replace addColumns(Column... columns) {
        List<Column> collection = Optional.ofNullable(getColumns()).orElseGet(ArrayList::new);
        Collections.addAll(collection, columns);
        return this.columns(collection);
    }

    public Replace addColumns(Collection<? extends Column> columns) {
        List<Column> collection = Optional.ofNullable(getColumns()).orElseGet(ArrayList::new);
        collection.addAll(columns);
        return this.columns(collection);
    }

    public Replace addExpressions(Expression... expressions) {
        List<Expression> collection = Optional.ofNullable(getExpressions()).orElseGet(ArrayList::new);
        Collections.addAll(collection, expressions);
        return this.expressions(collection);
    }

    public Replace addExpressions(Collection<? extends Expression> expressions) {
        List<Expression> collection = Optional.ofNullable(getExpressions()).orElseGet(ArrayList::new);
        collection.addAll(expressions);
        return this.expressions(collection);
    }
}
