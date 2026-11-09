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
 * A PostgreSQL {@code ALTER EVENT TRIGGER} statement. The enable states are shared with the trigger
 * and rule actions of {@link RelationAlterAction}. Each non-null setter selects its action and
 * discards the values of the other actions.
 */
public class AlterEventTrigger extends ASTNodeAccessImpl implements Statement {
    public enum Action {
        STATE, OWNER, RENAME
    }

    private String name;
    private Action action;
    private RelationAlterAction.EnableState enableState;
    private String owner;
    private String newName;

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Action getAction() {
        return action;
    }

    /** {@code ENABLE}, {@code ENABLE REPLICA}, {@code ENABLE ALWAYS} or {@code DISABLE}. */
    public RelationAlterAction.EnableState getEnableState() {
        return enableState;
    }

    public void setEnableState(RelationAlterAction.EnableState enableState) {
        if (enableState != null) {
            select(Action.STATE);
        }
        this.enableState = enableState;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        if (owner != null) {
            select(Action.OWNER);
        }
        this.owner = owner;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        if (newName != null) {
            select(Action.RENAME);
        }
        this.newName = newName;
    }

    private void select(Action action) {
        this.action = action;
        enableState = null;
        owner = null;
        newName = null;
    }

    public StringBuilder appendTo(StringBuilder sql) {
        sql.append("ALTER EVENT TRIGGER ").append(name);
        switch (action) {
            case STATE:
                sql.append(' ').append(enableState.name().replace('_', ' '));
                break;
            case OWNER:
                sql.append(" OWNER TO ").append(owner);
                break;
            case RENAME:
                sql.append(" RENAME TO ").append(newName);
                break;
            default:
                throw new IllegalStateException("Unknown event trigger action: " + action);
        }
        return sql;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    public AlterEventTrigger withName(String name) {
        setName(name);
        return this;
    }

    public AlterEventTrigger withEnableState(RelationAlterAction.EnableState enableState) {
        setEnableState(enableState);
        return this;
    }

    public AlterEventTrigger withOwner(String owner) {
        setOwner(owner);
        return this;
    }

    public AlterEventTrigger withNewName(String newName) {
        setNewName(newName);
        return this;
    }
}
