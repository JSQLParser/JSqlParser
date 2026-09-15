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

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;

/**
 * {@code PRAGMA name}, {@code PRAGMA name(arguments)} and {@code PRAGMA name = value}, used by
 * DuckDB and SQLite to read or set database settings.
 *
 * @see <a href="https://duckdb.org/docs/stable/configuration/pragmas">PRAGMA statements</a>
 */
public class PragmaStatement implements Statement {
    private String name;
    private ExpressionList<?> parameters;
    private Expression value;

    public PragmaStatement() {}

    public PragmaStatement(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PragmaStatement withName(String name) {
        setName(name);
        return this;
    }

    public ExpressionList<?> getParameters() {
        return parameters;
    }

    public void setParameters(ExpressionList<?> parameters) {
        this.parameters = parameters;
    }

    public PragmaStatement withParameters(ExpressionList<?> parameters) {
        setParameters(parameters);
        return this;
    }

    public Expression getValue() {
        return value;
    }

    public void setValue(Expression value) {
        this.value = value;
    }

    public PragmaStatement withValue(Expression value) {
        setValue(value);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("PRAGMA ").append(name);
        if (parameters != null) {
            builder.append("(").append(parameters).append(")");
        }
        if (value != null) {
            builder.append(" = ").append(value);
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
