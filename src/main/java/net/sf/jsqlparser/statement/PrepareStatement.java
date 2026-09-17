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
 * {@code PREPARE name AS statement}, which stores a parameterised statement for later
 * {@code EXECUTE}.
 *
 * @see <a href="https://duckdb.org/docs/stable/sql/query_syntax/prepared_statements">Prepared
 *      statements</a>
 */
public class PrepareStatement implements Statement {
    private String name;
    private Statement statement;

    public PrepareStatement() {}

    public PrepareStatement(String name, Statement statement) {
        this.name = name;
        this.statement = statement;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PrepareStatement withName(String name) {
        setName(name);
        return this;
    }

    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    public PrepareStatement withStatement(Statement statement) {
        setStatement(statement);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("PREPARE ").append(name).append(" AS ").append(statement);
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
