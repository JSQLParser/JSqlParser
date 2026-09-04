/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.analyze;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class Analyze implements Statement {

    public enum Modifier {
        NO_WRITE_TO_BINLOG, LOCAL
    }

    public enum HistogramAction {
        UPDATE, DROP
    }

    private final List<Table> tables = new ArrayList<>();
    private boolean tableKeyword;
    private Modifier modifier;
    private HistogramAction histogramAction;
    private final List<Column> histogramColumns = new ArrayList<>();
    private Integer histogramBucketCount;

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public Table getTable() {
        return tables.isEmpty() ? null : tables.get(0);
    }

    public void setTable(Table table) {
        tables.clear();
        if (table != null) {
            tables.add(table);
        }
    }

    public List<Table> getTables() {
        return Collections.unmodifiableList(tables);
    }

    public void setTables(Collection<? extends Table> tables) {
        this.tables.clear();
        if (tables != null) {
            this.tables.addAll(tables);
        }
    }

    public boolean isTableKeyword() {
        return tableKeyword;
    }

    public void setTableKeyword(boolean tableKeyword) {
        this.tableKeyword = tableKeyword;
    }

    public Modifier getModifier() {
        return modifier;
    }

    public void setModifier(Modifier modifier) {
        this.modifier = modifier;
    }

    public HistogramAction getHistogramAction() {
        return histogramAction;
    }

    public void setHistogramAction(HistogramAction histogramAction) {
        this.histogramAction = histogramAction;
    }

    public List<Column> getHistogramColumns() {
        return Collections.unmodifiableList(histogramColumns);
    }

    public void setHistogramColumns(Collection<? extends Column> columns) {
        histogramColumns.clear();
        if (columns != null) {
            histogramColumns.addAll(columns);
        }
    }

    public Integer getHistogramBucketCount() {
        return histogramBucketCount;
    }

    public void setHistogramBucketCount(Integer histogramBucketCount) {
        this.histogramBucketCount = histogramBucketCount;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("ANALYZE");
        if (modifier != null) {
            builder.append(" ").append(modifier);
        }
        if (tableKeyword) {
            builder.append(" TABLE");
        }
        builder.append(" ").append(tables.stream().map(Table::toString)
                .collect(Collectors.joining(", ")));
        if (histogramAction != null) {
            builder.append(" ").append(histogramAction).append(" HISTOGRAM ON ")
                    .append(histogramColumns.stream().map(Column::toString)
                            .collect(Collectors.joining(", ")));
            if (histogramBucketCount != null) {
                builder.append(" WITH ").append(histogramBucketCount).append(" BUCKETS");
            }
        }
        return builder.toString();
    }

    public Analyze withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public Analyze withTables(Collection<? extends Table> tables) {
        setTables(tables);
        return this;
    }

    public Analyze addTables(Table... tables) {
        Collections.addAll(this.tables, tables);
        return this;
    }

    public Analyze withTableKeyword(boolean tableKeyword) {
        setTableKeyword(tableKeyword);
        return this;
    }

    public Analyze withModifier(Modifier modifier) {
        setModifier(modifier);
        return this;
    }

    public Analyze withHistogramAction(HistogramAction histogramAction) {
        setHistogramAction(histogramAction);
        return this;
    }

    public Analyze withHistogramColumns(Collection<? extends Column> columns) {
        setHistogramColumns(columns);
        return this;
    }

    public Analyze addHistogramColumns(Column... columns) {
        Collections.addAll(histogramColumns, columns);
        return this;
    }

    public Analyze withHistogramBucketCount(Integer histogramBucketCount) {
        setHistogramBucketCount(histogramBucketCount);
        return this;
    }
}
