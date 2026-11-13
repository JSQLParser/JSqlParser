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
 * PostgreSQL
 * {@code ALTER CONVERSION name {RENAME TO new_name | OWNER TO role | SET SCHEMA schema}}. Names
 * retain SQL identifier quoting.
 */
public class AlterConversion extends ASTNodeAccessImpl implements Statement {
    public enum Action {
        RENAME, OWNER, SET_SCHEMA
    }

    private String name;
    private Action action;
    private String newName;
    private String owner;
    private String schemaName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AlterConversion withName(String name) {
        setName(name);
        return this;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public AlterConversion withAction(Action action) {
        setAction(action);
        return this;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public AlterConversion withNewName(String newName) {
        setNewName(newName);
        return this;
    }

    /**
     * The new owner: a role name, {@code CURRENT_ROLE}, {@code CURRENT_USER} or
     * {@code SESSION_USER}.
     *
     * @return the owner of {@code OWNER TO}
     */
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public AlterConversion withOwner(String owner) {
        setOwner(owner);
        return this;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public AlterConversion withSchemaName(String schemaName) {
        setSchemaName(schemaName);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("ALTER CONVERSION ").append(name);
        switch (action) {
            case RENAME:
                builder.append(" RENAME TO ").append(newName);
                break;
            case OWNER:
                builder.append(" OWNER TO ").append(owner);
                break;
            case SET_SCHEMA:
                builder.append(" SET SCHEMA ").append(schemaName);
                break;
            default:
                throw new IllegalStateException("Unknown conversion action: " + action);
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }
}
