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

import static java.util.stream.Collectors.toList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class Index {

    private String type;
    private String using;
    private List<ColumnParams> columns;
    private final List<String> name = new ArrayList<>();
    private List<String> idxSpec;

    public List<String> getColumnsNames() {
        return columns.stream()
                .map(col -> col.columnName)
                .collect(toList());
    }

    @Deprecated
    public List<ColumnParams> getColumnWithParams() {
        return getColumns();
    }

    @Deprecated
    public void setColumnNamesWithParams(List<ColumnParams> list) {
        setColumns(list);
    }

    public List<ColumnParams> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnParams> columns) {
        this.columns = columns;
    }

    public Index withColumns(List<ColumnParams> columns) {
        setColumns(columns);
        return this;
    }

    public Index addColumns(ColumnParams... functionDeclarationParts) {
        List<ColumnParams> collection = Optional.ofNullable(getColumns()).orElseGet(ArrayList::new);
        Collections.addAll(collection, functionDeclarationParts);
        return this.withColumns(collection);
    }

    public Index addColumns(Collection<? extends ColumnParams> functionDeclarationParts) {
        List<ColumnParams> collection = Optional.ofNullable(getColumns()).orElseGet(ArrayList::new);
        collection.addAll(functionDeclarationParts);
        return this.withColumns(collection);
    }

    public String getName() {
        return name.isEmpty() ? null : String.join(".", name);
    }

    public List<String> getNameParts() {
        return Collections.unmodifiableList(name);
    }

    public String getType() {
        return type;
    }

    /**
     * In postgresql, the index type (Btree, GIST, etc.) is indicated
     * with a USING clause.
     * Please note that:
     *  Oracle - the type might be BITMAP, indicating a bitmap kind of index
     *  MySQL - the type might be FULLTEXT or SPATIAL
     */
    public void setUsing(String string) {
        using = string;
    }

    public void setColumnsNames(List<String> list) {
        columns = list.stream().map(ColumnParams::new).collect(toList());
    }

    public Index withColumnsNames(List<String> list) {
        setColumnsNames(list);
        return this;
    }

    public void setName(String name) {
        this.name.clear();
        this.name.add(name);
    }

    public void setName(List<String> name) {
        this.name.clear();
        this.name.addAll(name);
    }

    public void setType(String string) {
        type = string;
    }

    public String getUsing() {
        return using;
    }

    public List<String> getIndexSpec() {
        return idxSpec;
    }

    public void setIndexSpec(List<String> idxSpec) {
        this.idxSpec = idxSpec;
    }

    public Index withIndexSpec(List<String> idxSpec) {
        setIndexSpec(idxSpec);
        return this;
    }

    @Override
    public String toString() {
        String idxSpecText = PlainSelect.getStringList(idxSpec, false, false);
        return type + (!name.isEmpty() ? " " + getName() : "") + " " + PlainSelect.
                getStringList(columns, true, true) + (!"".equals(idxSpecText) ? " " + idxSpecText : "");
    }

    public Index withType(String type) {
        this.setType(type);
        return this;
    }

    public Index withUsing(String using) {
        this.setUsing(using);
        return this;
    }

    public Index withName(List<String> name) {
        this.setName(name);
        return this;
    }

    public Index withName(String name) {
        this.setName(name);
        return this;
    }

    public static class ColumnParams {
        public final String columnName;
        public final List<String> params;

        public ColumnParams(String columnName) {
            this.columnName = columnName;
            this.params = null;
        }

        public ColumnParams(String columnName, List<String> params) {
            this.columnName = columnName;
            this.params = params;
        }

        public String getColumnName() {
            return columnName;
        }

        public List<String> getParams() {
            return params;
        }

        @Override
        public String toString() {
            return columnName + (params != null ? " " + String.join(" ", params) : "");
        }
    }
}
