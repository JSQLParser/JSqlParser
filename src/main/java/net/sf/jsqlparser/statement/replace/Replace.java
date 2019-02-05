/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.replace;

import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * The replace statement.
 */
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

    /**
     * A list of {@link net.sf.jsqlparser.schema.Column}s either from a "REPLACE mytab (col1, col2)
     * [...]" or a "REPLACE mytab SET col1=exp1, col2=exp2".
     *
     * @return a list of {@link net.sf.jsqlparser.schema.Column}s
     */
    public List<Column> getColumns() {
        return columns;
    }

    /**
     * An {@link ItemsList} (either from a "REPLACE mytab VALUES (exp1,exp2)" or a "REPLACE mytab
     * SELECT * FROM mytab2") it is null in case of a "REPLACE mytab SET col1=exp1, col2=exp2"
     */
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
}
