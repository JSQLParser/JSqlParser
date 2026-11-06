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
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * {@code COPY table [(columns)] FROM path [(options)]} and
 * {@code COPY {table | (query)} TO path [(options)]}, the bulk import and export statement in
 * DuckDB and PostgreSQL. PostgreSQL also accepts its option syntax from before 9.0, options without
 * parentheses such as {@code CSV HEADER} (see {@link #getLegacyOptions()}), and
 * {@code COPY BINARY table}.
 *
 * @see <a href="https://duckdb.org/docs/stable/sql/statements/copy">COPY</a>
 * @see <a href="https://www.postgresql.org/docs/18/sql-copy.html">PostgreSQL COPY</a>
 */
public class CopyStatement extends ASTNodeAccessImpl implements Statement {
    private Table table;
    private ExpressionList<Column> columns;
    private Select select;
    private boolean from;
    private String path;
    private List<String> options;
    private boolean withKeyword;
    private boolean binary;
    private final List<CopyLegacyOption> legacyOptions = new ArrayList<>();

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

    /** The options in parentheses, each as written; null when the statement has none. */
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

    /** Whether the table is preceded by BINARY, PostgreSQL's spelling from before 7.3. */
    public boolean isBinary() {
        return binary;
    }

    public void setBinary(boolean binary) {
        this.binary = binary;
    }

    public CopyStatement withBinary(boolean binary) {
        setBinary(binary);
        return this;
    }

    /**
     * The options of PostgreSQL's syntax from before 9.0, in source order, such as {@code CSV} and
     * {@code HEADER} in {@code COPY t TO STDOUT CSV HEADER}; empty when the statement has none. A
     * statement has either these options or the options in parentheses.
     */
    public List<CopyLegacyOption> getLegacyOptions() {
        return legacyOptions;
    }

    public void setLegacyOptions(Collection<? extends CopyLegacyOption> legacyOptions) {
        List<CopyLegacyOption> replacement = legacyOptions == null
                ? Collections.emptyList()
                : new ArrayList<>(legacyOptions);
        this.legacyOptions.clear();
        this.legacyOptions.addAll(replacement);
    }

    public CopyStatement addLegacyOptions(CopyLegacyOption... legacyOptions) {
        Collections.addAll(this.legacyOptions, legacyOptions);
        return this;
    }

    public CopyStatement withLegacyOptions(Collection<? extends CopyLegacyOption> legacyOptions) {
        setLegacyOptions(legacyOptions);
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
            if (binary) {
                builder.append("BINARY ");
            }
            builder.append(table);
            if (columns != null && !columns.isEmpty()) {
                builder.append(" (").append(columns).append(")");
            }
        }
        builder.append(from ? " FROM " : " TO ").append(path);
        if (withKeyword) {
            builder.append(" WITH");
        }
        for (CopyLegacyOption legacyOption : legacyOptions) {
            legacyOption.appendTo(builder.append(' '));
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
