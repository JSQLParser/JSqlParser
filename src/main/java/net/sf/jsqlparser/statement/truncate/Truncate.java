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

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class Truncate implements Statement {

    private Table table;
    boolean cascade; // to support TRUNCATE TABLE ... CASCADE

    boolean tableToken; // to support TRUNCATE without TABLE
    boolean only; // to support TRUNCATE with ONLY

    @Override
    public <T> T accept(StatementVisitor<T> statementVisitor) {
        return statementVisitor.visit(this);
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
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
        sb.append(table);

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

    public Truncate withCascade(boolean cascade) {
        this.setCascade(cascade);
        return this;
    }

    public Truncate withOnly(boolean only) {
        this.setOnly(only);
        return this;
    }
}

