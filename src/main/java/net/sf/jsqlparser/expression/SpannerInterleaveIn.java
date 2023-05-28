/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.schema.Table;
import java.util.Collections;
import java.util.List;

public class SpannerInterleaveIn {

    public enum OnDelete {

        CASCADE, NO_ACTION
    }

    private Table table;

    private OnDelete onDelete;

    public SpannerInterleaveIn() {
    }

    public SpannerInterleaveIn(Table table, OnDelete action) {
        setTable(table);
        setOnDelete(action);
    }

    public SpannerInterleaveIn(List<String> nameParts) {
        this(new Table(nameParts), null);
    }

    public SpannerInterleaveIn(String tableName) {
        this(Collections.singletonList(tableName));
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public OnDelete getOnDelete() {
        return onDelete;
    }

    public void setOnDelete(OnDelete action) {
        this.onDelete = action;
    }

    @Override
    public String toString() {
        return "INTERLEAVE IN PARENT " + getTable().getName() + (getOnDelete() == null ? "" : " ON DELETE " + (getOnDelete() == OnDelete.CASCADE ? "CASCADE" : "NO ACTION"));
    }

    public SpannerInterleaveIn withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public SpannerInterleaveIn withOnDelete(OnDelete action) {
        this.setOnDelete(action);
        return this;
    }
}
