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

/** PostgreSQL ALTER [PROCEDURAL] LANGUAGE with RENAME TO or OWNER TO. */
public class AlterLanguage extends ASTNodeAccessImpl implements Statement {
    public enum Action {
        RENAME, OWNER
    }

    private boolean procedural;
    private String name;
    private Action action;
    private String newName;
    private String owner;

    public boolean isProcedural() {
        return procedural;
    }

    public void setProcedural(boolean procedural) {
        this.procedural = procedural;
    }

    public AlterLanguage withProcedural(boolean procedural) {
        setProcedural(procedural);
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AlterLanguage withName(String name) {
        setName(name);
        return this;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public AlterLanguage withAction(Action action) {
        setAction(action);
        return this;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public AlterLanguage withNewName(String newName) {
        setNewName(newName);
        return this;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public AlterLanguage withOwner(String owner) {
        setOwner(owner);
        return this;
    }

    public StringBuilder appendTo(StringBuilder sql) {
        sql.append(procedural ? "ALTER PROCEDURAL LANGUAGE " : "ALTER LANGUAGE ").append(name);
        return action == Action.RENAME ? sql.append(" RENAME TO ").append(newName)
                : sql.append(" OWNER TO ").append(owner);
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
