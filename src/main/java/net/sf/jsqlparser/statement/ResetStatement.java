/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * PostgreSQL {@code RESET configuration_parameter}, {@code RESET ALL} and {@code RESET TIME ZONE},
 * and MySQL {@code RESET PERSIST [[IF EXISTS] system_var_name]}, which {@link #isPersist()} marks.
 *
 * @see <a href="https://www.postgresql.org/docs/current/sql-reset.html">PostgreSQL RESET</a>
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/reset-persist.html">MySQL RESET PERSIST</a>
 */
public final class ResetStatement extends ASTNodeAccessImpl implements Statement {

    private String name = "";
    private boolean persist;
    private boolean ifExists;

    public ResetStatement() {
        // empty constructor
    }

    public ResetStatement(String name) {
        add(name);
    }

    public void add(String name) {
        this.name = name;
    }

    /**
     * Returns the parameter or, for RESET PERSIST, the system variable; null when RESET PERSIST
     * names none and removes every persisted variable.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** Whether this is MySQL RESET PERSIST, which removes persisted global system variables. */
    public boolean isPersist() {
        return persist;
    }

    public void setPersist(boolean persist) {
        this.persist = persist;
    }

    public ResetStatement withPersist(boolean persist) {
        setPersist(persist);
        return this;
    }

    /** Whether RESET PERSIST has IF EXISTS before the variable name. */
    public boolean isIfExists() {
        return ifExists;
    }

    public void setIfExists(boolean ifExists) {
        this.ifExists = ifExists;
    }

    public ResetStatement withIfExists(boolean ifExists) {
        setIfExists(ifExists);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("RESET ");
        if (!persist) {
            return builder.append(name);
        }
        builder.append("PERSIST");
        if (ifExists) {
            builder.append(" IF EXISTS");
        }
        if (name != null && !name.isEmpty()) {
            builder.append(' ').append(name);
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
