/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.util.Objects;
import java.util.function.Consumer;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Table;

/** SQL Server SET IDENTITY_INSERT [database.][schema.]table ON | OFF. */
public final class SetIdentityInsertStatement extends ASTNodeAccessImpl implements Statement {
    private Table table;
    private boolean on;

    public SetIdentityInsertStatement(Table table, boolean on) {
        setTable(table);
        this.on = on;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = Objects.requireNonNull(table, "table");
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    /** Shares statement rendering while allowing deparsers to visit the existing Table AST. */
    public StringBuilder appendTo(StringBuilder builder, Consumer<Table> tableRenderer) {
        builder.append("SET IDENTITY_INSERT ");
        tableRenderer.accept(table);
        return builder.append(on ? " ON" : " OFF");
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }
}
