/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.extension;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * DuckDB 2.0's {@code CREATE EXTENSION REPOSITORY [IF NOT EXISTS] name [WITH PREFIX prefix]}, which
 * registers a custom repository extensions may be installed from.
 */
public class CreateExtensionRepository implements Statement {
    private String name;
    private boolean ifNotExists;
    private Expression prefix;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CreateExtensionRepository withName(String name) {
        setName(name);
        return this;
    }

    public boolean isIfNotExists() {
        return ifNotExists;
    }

    public void setIfNotExists(boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }

    public CreateExtensionRepository withIfNotExists(boolean ifNotExists) {
        setIfNotExists(ifNotExists);
        return this;
    }

    public Expression getPrefix() {
        return prefix;
    }

    public void setPrefix(Expression prefix) {
        this.prefix = prefix;
    }

    public CreateExtensionRepository withPrefix(Expression prefix) {
        setPrefix(prefix);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("CREATE EXTENSION REPOSITORY ");
        if (ifNotExists) {
            builder.append("IF NOT EXISTS ");
        }
        builder.append(name);
        if (prefix != null) {
            builder.append(" WITH PREFIX ").append(prefix);
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
