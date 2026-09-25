/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter.sequence;

import net.sf.jsqlparser.schema.Sequence;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * An {@code ALTER SEQUENCE} statement
 */
public class AlterSequence implements Statement {

    public enum Action {
        PARAMETERS, RENAME, OWNER, SET_SCHEMA, SET_LOGGED, SET_UNLOGGED
    }

    public Sequence sequence;
    private boolean ifExists;
    private Action action = Action.PARAMETERS;
    private String newName;
    private String owner;
    private String schemaName;

    public boolean isIfExists() {
        return ifExists;
    }

    public void setIfExists(boolean ifExists) {
        this.ifExists = ifExists;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = java.util.Objects.requireNonNull(action, "action");
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

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("ALTER SEQUENCE ");
        if (ifExists) {
            builder.append("IF EXISTS ");
        }
        builder.append(action == Action.PARAMETERS ? sequence : sequence.getFullyQualifiedName());
        switch (action) {
            case RENAME:
                return builder.append(" RENAME TO ").append(newName);
            case OWNER:
                return builder.append(" OWNER TO ").append(owner);
            case SET_SCHEMA:
                return builder.append(" SET SCHEMA ").append(schemaName);
            case SET_LOGGED:
                return builder.append(" SET LOGGED");
            case SET_UNLOGGED:
                return builder.append(" SET UNLOGGED");
            default:
                return builder;
        }
    }


    public Sequence getSequence() {
        return sequence;
    }

    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    public AlterSequence withSequence(Sequence sequence) {
        this.setSequence(sequence);
        return this;
    }
}
