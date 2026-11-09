/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.maintenance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.schema.TableReference;

/**
 * A table processed by PostgreSQL {@code VACUUM} or {@code ANALYZE}:
 * {@code [ONLY] table [*] [(column, ...)]}. The relation reuses {@link TableReference}, so
 * {@code ONLY} and {@code *} are described as in {@code LOCK} and {@code TRUNCATE}.
 */
public class MaintenanceTarget extends ASTNodeAccessImpl {

    private TableReference reference;
    private final List<Column> columns = new ArrayList<>();

    public MaintenanceTarget(Table table) {
        this(new TableReference(table));
    }

    public MaintenanceTarget(TableReference reference) {
        setReference(reference);
    }

    public TableReference getReference() {
        return reference;
    }

    public void setReference(TableReference reference) {
        this.reference = Objects.requireNonNull(reference, "reference");
    }

    public Table getTable() {
        return reference.getTable();
    }

    /** Replaces the table and keeps {@code ONLY} or {@code *}. */
    public void setTable(Table table) {
        reference.setTable(table);
    }

    /** Whether the target is a plain table name, without {@code ONLY}, {@code *} or columns. */
    public boolean isPlainTable() {
        return !reference.isOnly() && !reference.isIncludeDescendants() && columns.isEmpty();
    }

    /** The columns to analyze; empty for all columns. */
    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(Collection<? extends Column> columns) {
        List<Column> replacement =
                columns == null ? Collections.emptyList() : new ArrayList<>(columns);
        this.columns.clear();
        this.columns.addAll(replacement);
    }

    public MaintenanceTarget addColumns(Column... columns) {
        Collections.addAll(this.columns, columns);
        return this;
    }

    public MaintenanceTarget withColumns(Collection<? extends Column> columns) {
        setColumns(columns);
        return this;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        reference.appendTo(builder);
        if (!columns.isEmpty()) {
            builder.append(" (");
            for (int i = 0; i < columns.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(columns.get(i).getFullyQualifiedName());
            }
            builder.append(')');
        }
        return builder;
    }

    /** Appends the targets after a space, separated by commas; nothing without targets. */
    public static StringBuilder appendTargets(StringBuilder builder,
            List<MaintenanceTarget> targets) {
        for (int i = 0; i < targets.size(); i++) {
            builder.append(i == 0 ? " " : ", ");
            targets.get(i).appendTo(builder);
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
