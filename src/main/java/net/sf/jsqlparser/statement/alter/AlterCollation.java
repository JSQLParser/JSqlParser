/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class AlterCollation implements Statement {
    public enum Action {
        REFRESH_VERSION, RENAME, OWNER, SET_SCHEMA
    }

    private String name;
    private Action action;
    private String newName;
    private String owner;
    private String schemaName;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action value) {
        action = value;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String value) {
        newName = value;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String value) {
        owner = value;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String value) {
        schemaName = value;
    }

    public StringBuilder appendTo(StringBuilder sql) {
        sql.append("ALTER COLLATION ").append(name);
        switch (action) {
            case REFRESH_VERSION:
                sql.append(" REFRESH VERSION");
                break;
            case RENAME:
                sql.append(" RENAME TO ").append(newName);
                break;
            case OWNER:
                sql.append(" OWNER TO ").append(owner);
                break;
            case SET_SCHEMA:
                sql.append(" SET SCHEMA ").append(schemaName);
                break;
            default:
                throw new IllegalStateException("Unknown collation action: " + action);
        }
        return sql;
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
