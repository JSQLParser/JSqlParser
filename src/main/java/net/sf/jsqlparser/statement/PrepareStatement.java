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

import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * {@code PREPARE name [(types)] AS statement}, which stores a parameterised statement for later
 * {@code EXECUTE}.
 *
 * @see <a href="https://duckdb.org/docs/stable/sql/query_syntax/prepared_statements">Prepared
 *      statements</a>
 */
public class PrepareStatement implements Statement {
    private String name;
    private Statement statement;
    private List<ColDataType> parameterTypes;

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

    /** Returns declared parameter types, or {@code null} when types are inferred. */
    public List<ColDataType> getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(List<ColDataType> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public PrepareStatement withParameterTypes(List<ColDataType> parameterTypes) {
        setParameterTypes(parameterTypes);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        return appendTo(builder, builder::append);
    }

    /** Renders the nested statement through the caller's statement writer. */
    public StringBuilder appendTo(StringBuilder builder, Consumer<Statement> statementPrinter) {
        builder.append("PREPARE ").append(name);
        if (parameterTypes != null && !parameterTypes.isEmpty()) {
            builder.append(PlainSelect.getStringList(parameterTypes, true, true));
        }
        builder.append(" AS ");
        statementPrinter.accept(statement);
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
