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
import net.sf.jsqlparser.expression.Expression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Globally used definition class for columns.
 */
public class ColumnDefinition implements ImportColumn, TableElement, Serializable {

    private String columnName;
    private ColDataType colDataType;
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
        columnOptions = rawOptions(columnSpecs);
    }

    /**
     * Returns an unmodifiable token snapshot for both raw and structured options, or null when
     * options are unset. Use {@link #getColumnOptions()} to edit the nodes, or
     * {@link #addColumnSpecs(Collection)} to append without discarding structured expressions,
     * references and constraints. DEFAULT values use the expression's SQL rendering.
     */
    public List<String> getColumnSpecs() {
        if (columnOptions != null) {
            List<String> tokens = new ArrayList<>();
            for (ColumnOption option : columnOptions) {
                tokens.addAll(option.getTokens());
            }
            return Collections.unmodifiableList(tokens);
        }
        return null;
    }

    /**
     * Replaces every option with opaque tokens, without parsing them. The supplied list is copied;
     * null clears the options, while an empty list stores an explicitly empty option list.
     */
    public void setColumnSpecs(List<String> list) {
        columnOptions = rawOptions(list);
    }

    private static List<ColumnOption> rawOptions(List<String> list) {
        List<ColumnOption> options = list == null ? null : new ArrayList<>();
        if (list != null && !list.isEmpty()) {
            options.add(ColumnOption.raw(new ArrayList<>(list)));
        }
        return options;
    }

    /**
     * Returns the mutable option list in source order, or null when options are unset. Opaque
     * specifications are included as {@link ColumnOption.Kind#OTHER} nodes. Structured option
     * keywords use canonical capitalization when rendered.
     */
    public List<ColumnOption> getColumnOptions() {
        return columnOptions;
    }

    /** Copies the list membership while retaining the supplied mutable option nodes. */
    public void setColumnOptions(List<ColumnOption> columnOptions) {
        this.columnOptions = columnOptions == null ? null : new ArrayList<>(columnOptions);
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
        Objects.requireNonNull(columnOptions, "columnOptions");
        if (this.columnOptions == null) {
            this.columnOptions = new ArrayList<>();
        }
        Collections.addAll(this.columnOptions, columnOptions);
        return this;
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

    /** Appends copied opaque tokens after the existing nodes, preserving their structure. */
    public ColumnDefinition addColumnSpecs(Collection<String> columnSpecs) {
        Objects.requireNonNull(columnSpecs, "columnSpecs");
        if (columnOptions == null) {
            columnOptions = new ArrayList<>();
        }
        if (!columnSpecs.isEmpty()) {
            columnOptions.add(ColumnOption.raw(new ArrayList<>(columnSpecs)));
        }
        return this;
    }
}
