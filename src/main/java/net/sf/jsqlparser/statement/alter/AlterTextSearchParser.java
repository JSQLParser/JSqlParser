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

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * PostgreSQL ALTER TEXT SEARCH PARSER. Only rename and schema changes are supported; there is no
 * OWNER TO form.
 */
public class AlterTextSearchParser extends ASTNodeAccessImpl implements Statement {
    public enum Action {
        RENAME, SET_SCHEMA
    }

    private String name;
    private Action action;
    private String newName;
    private String schemaName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AlterTextSearchParser withName(String name) {
        setName(name);
        return this;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public AlterTextSearchParser withAction(Action action) {
        setAction(action);
        return this;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public AlterTextSearchParser withNewName(String newName) {
        setNewName(newName);
        return this;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public AlterTextSearchParser withSchemaName(String schemaName) {
        setSchemaName(schemaName);
        return this;
    }

    public StringBuilder appendTo(StringBuilder sql) {
        sql.append("ALTER TEXT SEARCH PARSER ").append(name);
        switch (action) {
            case RENAME:
                return sql.append(" RENAME TO ").append(newName);
            case SET_SCHEMA:
                return sql.append(" SET SCHEMA ").append(schemaName);
            default:
                throw new IllegalStateException("Unknown text-search action: " + action);
        }
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
