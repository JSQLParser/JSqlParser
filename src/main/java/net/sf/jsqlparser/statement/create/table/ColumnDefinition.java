/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import net.sf.jsqlparser.statement.imprt.ImportColumn;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.expression.Expression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Globally used definition class for columns.
 */
public class ColumnDefinition implements ImportColumn, TableElement, Serializable {

    private String columnName;
    private ColDataType colDataType;
    private List<String> columnSpecs;
    private List<ColumnOption> columnOptions;
    private boolean withOptions;

    public boolean isWithOptions() {
        return withOptions;
    }

    public void setWithOptions(boolean withOptions) {
        this.withOptions = withOptions;
    }

    public ColumnDefinition() {}

    public ColumnDefinition(String columnName, ColDataType colDataType) {
        this.columnName = columnName;
        this.colDataType = colDataType;
    }

    public ColumnDefinition(String columnName, ColDataType colDataType, List<String> columnSpecs) {
        this(columnName, colDataType);
        this.columnSpecs = columnSpecs;
    }

    /**
     * Returns raw specifications, or a token snapshot when structured options are present. Use the
     * option API or {@link #addColumnSpecs(Collection)} to append without discarding structured
     * expressions, references and constraints. DEFAULT values use the expression's SQL rendering.
     */
    public List<String> getColumnSpecs() {
        if (columnOptions != null) {
            List<String> tokens = new ArrayList<>();
            for (ColumnOption option : columnOptions) {
                tokens.addAll(option.getTokens());
            }
            return tokens;
        }
        return columnSpecs;
    }

    public void setColumnSpecs(List<String> list) {
        columnSpecs = list;
        columnOptions = null;
    }

    /**
     * Returns column options in source order, including defaults, references, generated columns,
     * nullability, collation, comments, visibility and MySQL attributes. Unrecognized options
     * remain raw. Structured option keywords use canonical capitalization when rendered.
     */
    public List<ColumnOption> getColumnOptions() {
        return columnOptions;
    }

    public void setColumnOptions(List<ColumnOption> columnOptions) {
        this.columnOptions = columnOptions;
        this.columnSpecs = null;
    }

    public boolean isSerialDefaultValue() {
        return columnOptions != null && columnOptions.stream()
                .anyMatch(option -> option.getKind() == ColumnOption.Kind.SERIAL_DEFAULT_VALUE);
    }

    public ForeignKeyReference getForeignKeyReference() {
        if (columnOptions == null) {
            return null;
        }
        return columnOptions.stream()
                .filter(option -> option.getKind() == ColumnOption.Kind.REFERENCE)
                .map(ColumnOption::getForeignKeyReference)
                .findFirst()
                .orElse(null);
    }

    public ColumnDefinition withColumnOptions(List<ColumnOption> columnOptions) {
        setColumnOptions(columnOptions);
        return this;
    }

    public ColumnDefinition addColumnOptions(ColumnOption... columnOptions) {
        List<ColumnOption> collection =
                Optional.ofNullable(getColumnOptions()).orElseGet(ArrayList::new);
        if (this.columnOptions == null && columnSpecs != null && !columnSpecs.isEmpty()) {
            collection.add(ColumnOption.raw(new ArrayList<>(columnSpecs)));
        }
        Collections.addAll(collection, columnOptions);
        return withColumnOptions(collection);
    }

    public ColDataType getColDataType() {
        return colDataType;
    }

    public void setColDataType(ColDataType type) {
        colDataType = type;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String string) {
        columnName = string;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        appendTo(builder, builder::append);
        return builder.toString().trim();
    }

    /** Appends a column definition using the supplied printer for structured expressions. */
    public void appendTo(StringBuilder builder, Consumer<Expression> expressionPrinter) {
        builder.append(columnName);
        if (colDataType != null || withOptions) {
            builder.append(' ');
        }
        appendDataTypeAndSpecTo(builder, expressionPrinter);
    }

    public String toStringDataTypeAndSpec() {
        StringBuilder builder = new StringBuilder();
        appendDataTypeAndSpecTo(builder, builder::append);
        return builder.toString();
    }

    protected void appendDataTypeAndSpecTo(StringBuilder builder,
            Consumer<Expression> expressionPrinter) {
        if (colDataType != null) {
            builder.append(colDataType);
        }
        if (withOptions) {
            builder.append("WITH OPTIONS");
        }
        if (columnOptions != null) {
            for (ColumnOption option : columnOptions) {
                builder.append(' ');
                option.appendTo(builder, expressionPrinter);
            }
        } else if (columnSpecs != null && !columnSpecs.isEmpty()) {
            builder.append(' ').append(PlainSelect.getStringList(columnSpecs, false, false));
        }
    }

    public ColumnDefinition withColumnName(String columnName) {
        this.setColumnName(columnName);
        return this;
    }

    public ColumnDefinition withColDataType(ColDataType colDataType) {
        this.setColDataType(colDataType);
        return this;
    }

    public ColumnDefinition withColumnSpecs(List<String> columnSpecs) {
        this.setColumnSpecs(columnSpecs);
        return this;
    }

    public ColumnDefinition addColumnSpecs(String... columnSpecs) {
        return addColumnSpecs(Arrays.asList(columnSpecs));
    }

    public ColumnDefinition addColumnSpecs(Collection<String> columnSpecs) {
        if (columnOptions != null) {
            if (!columnSpecs.isEmpty()) {
                columnOptions.add(ColumnOption.raw(new ArrayList<>(columnSpecs)));
            }
            return this;
        }
        List<String> collection = Optional.ofNullable(getColumnSpecs()).orElseGet(ArrayList::new);
        collection.addAll(columnSpecs);
        return this.withColumnSpecs(collection);
    }
}
