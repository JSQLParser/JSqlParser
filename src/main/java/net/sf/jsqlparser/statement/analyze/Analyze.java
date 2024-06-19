/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.analyze;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class Analyze implements Statement {

    private Table table;

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

    @Override
    public String toString() {
        return "ANALYZE " + table.toString();
    }

    public Analyze withTable(Table table) {
        this.setTable(table);
        return this;
    }
}
