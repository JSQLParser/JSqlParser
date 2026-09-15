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
 * DuckDB's {@code DISCONNECT [name]}, the counterpart of {@link ConnectStatement}.
 */
public class DisconnectStatement implements Statement {
    private String name;

    public DisconnectStatement() {}

    public DisconnectStatement(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DisconnectStatement withName(String name) {
        setName(name);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("DISCONNECT");
        if (name != null) {
            builder.append(" ").append(name);
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
