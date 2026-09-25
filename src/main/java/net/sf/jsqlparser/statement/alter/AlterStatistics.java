/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/** PostgreSQL statistics object metadata; a null target denotes SET STATISTICS DEFAULT. */
public class AlterStatistics implements Statement {
    public enum Action {
        RENAME, OWNER, SET_SCHEMA, SET_STATISTICS
    }

    private String name;
    private Action action;
    private String newName;
    private String owner;
    private String schemaName;
    private Integer statistics;

    public String getName() {
        return name;
    }

    public AlterStatistics setName(String name) {
        this.name = name;
        return this;
    }

    public Action getAction() {
        return action;
    }

    public AlterStatistics setAction(Action action) {
        this.action = action;
        return this;
    }

    public String getNewName() {
        return newName;
    }

    public AlterStatistics setNewName(String newName) {
        this.newName = newName;
        return this;
    }

    public String getOwner() {
        return owner;
    }

    public AlterStatistics setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public AlterStatistics setSchemaName(String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    public Integer getStatistics() {
        return statistics;
    }

    public AlterStatistics setStatistics(Integer statistics) {
        this.statistics = statistics;
        return this;
    }

    public StringBuilder appendTo(StringBuilder sql) {
        sql.append("ALTER STATISTICS ").append(name);
        if (action == Action.RENAME) {
            sql.append(" RENAME TO ").append(newName);
        } else if (action == Action.OWNER) {
            sql.append(" OWNER TO ").append(owner);
        } else if (action == Action.SET_SCHEMA) {
            sql.append(" SET SCHEMA ").append(schemaName);
        } else if (action == Action.SET_STATISTICS) {
            sql.append(" SET STATISTICS ").append(statistics == null ? "DEFAULT" : statistics);
        } else {
            throw new IllegalStateException("Expected statistics alteration action");
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
