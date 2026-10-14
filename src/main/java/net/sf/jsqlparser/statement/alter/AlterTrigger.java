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

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * PostgreSQL {@code ALTER TRIGGER name ON table}, which renames the trigger or marks it as
 * dependent on an extension. Names retain SQL identifier quoting.
 */
public class AlterTrigger implements Statement {

    public enum Action {
        /** {@code RENAME TO}, see {@link #getNewName()}. */
        RENAME,
        /**
         * {@code [NO] DEPENDS ON EXTENSION}, see {@link #getExtensionName()} and
         * {@link #isNoDependency()}.
         */
        DEPENDS_ON_EXTENSION
    }

    private String name;
    private Table table;
    private Action action = Action.RENAME;
    private String newName;
    private String extensionName;
    private boolean noDependency;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The table, view or foreign table that owns the trigger.
     *
     * @return the table
     */
    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    /**
     * The rendered alteration. Payloads of the other action are retained for a later switch back.
     *
     * @return the action, {@link Action#RENAME} unless changed
     */
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

    public String getExtensionName() {
        return extensionName;
    }

    public void setExtensionName(String extensionName) {
        this.extensionName = extensionName;
    }

    /**
     * Whether {@code NO DEPENDS ON EXTENSION} removes the dependency.
     *
     * @return {@code true} for {@code NO DEPENDS}
     */
    public boolean isNoDependency() {
        return noDependency;
    }

    public void setNoDependency(boolean noDependency) {
        this.noDependency = noDependency;
    }

    public AlterTrigger withName(String name) {
        setName(name);
        return this;
    }

    public AlterTrigger withTable(Table table) {
        setTable(table);
        return this;
    }

    public AlterTrigger withAction(Action action) {
        setAction(action);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("ALTER TRIGGER ").append(name).append(" ON ").append(table);
        if (action == Action.DEPENDS_ON_EXTENSION) {
            return builder.append(noDependency ? " NO DEPENDS" : " DEPENDS")
                    .append(" ON EXTENSION ").append(extensionName);
        }
        return builder.append(" RENAME TO ").append(newName);
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
