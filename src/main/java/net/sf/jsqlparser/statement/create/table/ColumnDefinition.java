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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Globally used definition class for columns.
 */
public class ColumnDefinition implements ImportColumn, TableElement, Serializable {

    private String columnName;
    private ColDataType colDataType;
    private List<String> columnSpecs;
    private List<ColumnOption> columnOptions;

    public ColumnDefinition() {}

    public ColumnDefinition(String columnName, ColDataType colDataType) {
        this.columnName = columnName;
        this.colDataType = colDataType;
    }

    public ColumnDefinition(String columnName, ColDataType colDataType, List<String> columnSpecs) {
        this(columnName, colDataType);
        this.columnSpecs = columnSpecs;
    }

    public List<String> getColumnSpecs() {
        return columnSpecs;
    }

    public void setColumnSpecs(List<String> list) {
        columnSpecs = list;
        columnOptions = null;
    }

    /**
     * Returns column options in source order, including structured references and MySQL
     * {@code SERIAL DEFAULT VALUE}.
     */
    public List<ColumnOption> getColumnOptions() {
        return columnOptions;
    }

    public void setColumnOptions(List<ColumnOption> columnOptions) {
        this.columnOptions = columnOptions;
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
        return columnName + " " + toStringDataTypeAndSpec();
    }

    public String toStringDataTypeAndSpec() {
        return (colDataType == null ? "" : colDataType)
                + (columnOptions != null && !columnOptions.isEmpty()
                        ? " " + PlainSelect.getStringList(columnOptions, false, false)
                        : columnSpecs != null && !columnSpecs.isEmpty()
                                ? " " + PlainSelect.getStringList(columnSpecs, false, false)
                                : "");
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
        List<String> collection = Optional.ofNullable(getColumnSpecs()).orElseGet(ArrayList::new);
        Collections.addAll(collection, columnSpecs);
        return this.withColumnSpecs(collection);
    }

    public ColumnDefinition addColumnSpecs(Collection<String> columnSpecs) {
        List<String> collection = Optional.ofNullable(getColumnSpecs()).orElseGet(ArrayList::new);
        collection.addAll(columnSpecs);
        return this.withColumnSpecs(collection);
    }
}
