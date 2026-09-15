/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

/**
 * DuckDB's {@code CONNECT target [AS alias]}, which routes the session's queries to a remote
 * database.
 */
public class ConnectStatement implements Statement {
    private String target;
    private String alias;

    public ConnectStatement() {}

    public ConnectStatement(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public ConnectStatement withTarget(String target) {
        setTarget(target);
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public ConnectStatement withAlias(String alias) {
        setAlias(alias);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("CONNECT ").append(target);
        if (alias != null) {
            builder.append(" AS ").append(alias);
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
