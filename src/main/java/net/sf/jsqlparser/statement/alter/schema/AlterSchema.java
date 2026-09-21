/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter.schema;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/** PostgreSQL schema name or ownership change. Names retain SQL identifier quoting. */
public class AlterSchema implements Statement {
    public enum Action {
        RENAME, OWNER
    }

    private String schemaName;
    private Action action;
    private String newName;
    private String owner;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("ALTER SCHEMA ").append(schemaName);
        if (action == Action.RENAME) {
            builder.append(" RENAME TO ").append(newName);
        } else if (action == Action.OWNER) {
            builder.append(" OWNER TO ").append(owner);
        } else {
            throw new IllegalStateException("Expected a schema alteration action");
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }
}
