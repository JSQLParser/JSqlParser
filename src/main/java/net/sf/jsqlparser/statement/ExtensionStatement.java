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
 * DuckDB's extension management statements {@code [FORCE] INSTALL extension [FROM repository]} and
 * {@code LOAD extension}.
 *
 * @see <a href="https://duckdb.org/docs/stable/extensions/overview">Extensions</a>
 */
public class ExtensionStatement implements Statement {
    private Operation operation;
    private boolean force;
    private String extensionName;
    private String repository;

    public ExtensionStatement() {}

    public ExtensionStatement(Operation operation, String extensionName) {
        this.operation = operation;
        this.extensionName = extensionName;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public ExtensionStatement withOperation(Operation operation) {
        setOperation(operation);
        return this;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public ExtensionStatement withForce(boolean force) {
        setForce(force);
        return this;
    }

    public String getExtensionName() {
        return extensionName;
    }

    public void setExtensionName(String extensionName) {
        this.extensionName = extensionName;
    }

    public ExtensionStatement withExtensionName(String extensionName) {
        setExtensionName(extensionName);
        return this;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public ExtensionStatement withRepository(String repository) {
        setRepository(repository);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        if (force) {
            builder.append("FORCE ");
        }
        builder.append(operation).append(" ").append(extensionName);
        if (repository != null) {
            builder.append(" FROM ").append(repository);
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

    public enum Operation {
        INSTALL, LOAD
    }
}
