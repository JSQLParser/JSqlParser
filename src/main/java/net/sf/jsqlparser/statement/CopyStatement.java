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

/**
 * {@code COPY table [(columns)] FROM path [(options)]} and
 * {@code COPY {table | (query)} TO path [(options)]}, DuckDB's bulk import and export statement.
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

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("COPY ");
        if (select != null) {
            builder.append("(").append(select).append(")");
        } else {
            builder.append(table);
            if (columns != null && !columns.isEmpty()) {
                builder.append(" (").append(columns).append(")");
            }
        }
        builder.append(from ? " FROM " : " TO ").append(path);
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
