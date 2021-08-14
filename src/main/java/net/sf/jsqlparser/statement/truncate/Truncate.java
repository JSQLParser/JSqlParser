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
import net.sf.jsqlparser.statement.DDLStatement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class Truncate extends DDLStatement {

    private Table table;
    boolean cascade;  // to support TRUNCATE TABLE ... CASCADE

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
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

    public Truncate withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public Truncate withCascade(boolean cascade) {
        this.setCascade(cascade);
        return this;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("TRUNCATE TABLE ").append(table);
        if (cascade) {
            builder.append(" CASCADE");
        }
        return builder;
    }
}
