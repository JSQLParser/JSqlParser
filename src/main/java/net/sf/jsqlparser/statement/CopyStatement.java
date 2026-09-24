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

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

import java.util.List;
import java.util.function.Consumer;

/**
 * {@code COPY table [(columns)] FROM path [(options)]} and
 * {@code COPY {table | (query)} TO path [(options)]}, the bulk import and export statement in
 * DuckDB and PostgreSQL.
 *
 * @see <a href="https://duckdb.org/docs/stable/sql/statements/copy">COPY</a>
 */
public class CopyStatement implements Statement {
    private Table table;
    private ExpressionList<Column> columns;
    private Select select;
    private boolean from;
    private String path;
    private List<String> options;
    private boolean withKeyword;

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public CopyStatement withTable(Table table) {
        setTable(table);
        return this;
    }

    public ExpressionList<Column> getColumns() {
        return columns;
    }

    public void setColumns(ExpressionList<Column> columns) {
        this.columns = columns;
    }

    public CopyStatement withColumns(ExpressionList<Column> columns) {
        setColumns(columns);
        return this;
    }

    public Select getSelect() {
        return select;
    }

    public void setSelect(Select select) {
        this.select = select;
    }

    public CopyStatement withSelect(Select select) {
        setSelect(select);
        return this;
    }

    /** {@code true} for COPY ... FROM (import), {@code false} for COPY ... TO (export). */
    public boolean isFrom() {
        return from;
    }

    public void setFrom(boolean from) {
        this.from = from;
    }

    public CopyStatement withFrom(boolean from) {
        setFrom(from);
        return this;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public CopyStatement withPath(String path) {
        setPath(path);
        return this;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public CopyStatement withOptions(List<String> options) {
        setOptions(options);
        return this;
    }

    /** Whether the option list is introduced by the optional WITH keyword. */
    public boolean isWithKeyword() {
        return withKeyword;
    }

    public void setWithKeyword(boolean withKeyword) {
        this.withKeyword = withKeyword;
    }

    public CopyStatement withWithKeyword(boolean withKeyword) {
        setWithKeyword(withKeyword);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        return appendTo(builder, builder::append);
    }

    /** Renders the query through the caller's select writer. */
    public StringBuilder appendTo(StringBuilder builder, Consumer<Select> selectPrinter) {
        builder.append("COPY ");
        if (select != null) {
            builder.append('(');
            selectPrinter.accept(select);
            builder.append(')');
        } else {
            builder.append(table);
            if (columns != null && !columns.isEmpty()) {
                builder.append(" (").append(columns).append(")");
            }
        }
        builder.append(from ? " FROM " : " TO ").append(path);
        if (withKeyword) {
            builder.append(" WITH");
        }
        if (options != null && !options.isEmpty()) {
            builder.append(" ").append(PlainSelect.getStringList(options, true, true));
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
