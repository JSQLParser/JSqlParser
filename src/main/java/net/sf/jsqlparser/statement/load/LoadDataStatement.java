/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.load;

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.util.List;

/**
 * BigQuery's {@code LOAD DATA {INTO | OVERWRITE} table [(column_definitions)] [OPTIONS
 * (option_list)] FROM FILES (file_options) [WITH PARTITION COLUMNS] [WITH CONNECTION connection]}.
 *
 * @see <a href=
 *      "https://cloud.google.com/bigquery/docs/reference/standard-sql/load-statements">Load
 *      statements</a>
 */
public class LoadDataStatement implements Statement {
    private boolean overwrite;
    private Table table;
    private List<ColumnDefinition> columnDefinitions;
    private ExpressionList<?> options;
    private ExpressionList<?> fileOptions;
    private boolean withPartitionColumns;
    private String connectionName;

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public LoadDataStatement withOverwrite(boolean overwrite) {
        setOverwrite(overwrite);
        return this;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public LoadDataStatement withTable(Table table) {
        setTable(table);
        return this;
    }

    public List<ColumnDefinition> getColumnDefinitions() {
        return columnDefinitions;
    }

    public void setColumnDefinitions(List<ColumnDefinition> columnDefinitions) {
        this.columnDefinitions = columnDefinitions;
    }

    public LoadDataStatement withColumnDefinitions(List<ColumnDefinition> columnDefinitions) {
        setColumnDefinitions(columnDefinitions);
        return this;
    }

    public ExpressionList<?> getOptions() {
        return options;
    }

    public void setOptions(ExpressionList<?> options) {
        this.options = options;
    }

    public LoadDataStatement withOptions(ExpressionList<?> options) {
        setOptions(options);
        return this;
    }

    public ExpressionList<?> getFileOptions() {
        return fileOptions;
    }

    public void setFileOptions(ExpressionList<?> fileOptions) {
        this.fileOptions = fileOptions;
    }

    public LoadDataStatement withFileOptions(ExpressionList<?> fileOptions) {
        setFileOptions(fileOptions);
        return this;
    }

    public boolean isWithPartitionColumns() {
        return withPartitionColumns;
    }

    public void setWithPartitionColumns(boolean withPartitionColumns) {
        this.withPartitionColumns = withPartitionColumns;
    }

    public LoadDataStatement withWithPartitionColumns(boolean withPartitionColumns) {
        setWithPartitionColumns(withPartitionColumns);
        return this;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public LoadDataStatement withConnectionName(String connectionName) {
        setConnectionName(connectionName);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("LOAD DATA ").append(overwrite ? "OVERWRITE " : "INTO ").append(table);
        if (columnDefinitions != null && !columnDefinitions.isEmpty()) {
            builder.append(" ").append(PlainSelect.getStringList(columnDefinitions, true, true));
        }
        if (options != null) {
            builder.append(" OPTIONS (").append(options).append(")");
        }
        builder.append(" FROM FILES (").append(fileOptions).append(")");
        if (withPartitionColumns) {
            builder.append(" WITH PARTITION COLUMNS");
        }
        if (connectionName != null) {
            builder.append(" WITH CONNECTION ").append(connectionName);
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
