/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.truncate;

import static java.util.stream.Collectors.joining;

import java.util.List;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class Truncate implements Statement {

    boolean cascade; // to support TRUNCATE TABLE ... CASCADE
    boolean tableToken; // to support TRUNCATE without TABLE
    boolean only; // to support TRUNCATE with ONLY
    private Table table;
    private List<Table> tables;

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public Table getTable() {
        return table;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public boolean getCascade() {
        return cascade;
    }

    public void setCascade(boolean c) {
        cascade = c;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TRUNCATE");
        if (tableToken) {
            sb.append(" TABLE");
        }
        if (only) {
            sb.append(" ONLY");
        }
        sb.append(" ");
        if (tables != null && !tables.isEmpty()) {
            sb.append(tables.stream()
                .map(Table::toString)
                .collect(joining(", ")));
        } else {
            sb.append(table);
        }
        if (cascade) {
            sb.append(" CASCADE");
        }
        return sb.toString();
    }

    public boolean isTableToken() {
        return tableToken;
    }

    public void setTableToken(boolean hasTable) {
        this.tableToken = hasTable;
    }

    public boolean isOnly() {
        return only;
    }

    public void setOnly(boolean only) {
        this.only = only;
    }

    public Truncate withTableToken(boolean hasTableToken) {
        this.setTableToken(hasTableToken);
        return this;
    }

    public Truncate withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public Truncate withTables(List<Table> tables) {
        this.setTables(tables);
        return this;
    }

    public Truncate withCascade(boolean cascade) {
        this.setCascade(cascade);
        return this;
    }

    public Truncate withOnly(boolean only) {
        this.setOnly(only);
        return this;
    }
}

