/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.schema;

import java.io.Serializable;

/**
 * A relation target with optional PostgreSQL inheritance scope, shared by LOCK, TRUNCATE and the
 * targets of UPDATE, DELETE and MERGE.
 */
public class TableReference implements Serializable {
    private Table table;
    private boolean only;
    private boolean includeDescendants;
    private boolean parenthesized;

    public TableReference(Table table) {
        this.table = table;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public boolean isOnly() {
        return only;
    }

    public void setOnly(boolean only) {
        this.only = only;
        if (only) {
            includeDescendants = false;
        } else {
            parenthesized = false;
        }
    }

    public boolean isIncludeDescendants() {
        return includeDescendants;
    }

    public void setIncludeDescendants(boolean includeDescendants) {
        this.includeDescendants = includeDescendants;
        if (includeDescendants) {
            only = false;
            parenthesized = false;
        }
    }

    public boolean isParenthesized() {
        return parenthesized;
    }

    public void setParenthesized(boolean parenthesized) {
        this.parenthesized = parenthesized;
        if (parenthesized) {
            only = true;
            includeDescendants = false;
        }
    }

    public StringBuilder appendTo(StringBuilder builder) {
        if (only) {
            builder.append("ONLY ");
        }
        if (parenthesized) {
            builder.append('(');
        }
        builder.append(table.getFullyQualifiedName());
        if (parenthesized) {
            builder.append(')');
        }
        if (includeDescendants) {
            builder.append(" *");
        }
        return builder;
    }

    /**
     * Appends the target of UPDATE, DELETE or MERGE (PostgreSQL's relation_expr_opt_alias): the
     * inheritance scope surrounds the table name and the alias of the table follows it. A target
     * without a scope is the table as it renders itself, with its alias and hints.
     */
    public StringBuilder appendWithAliasTo(StringBuilder builder) {
        if (!only && !includeDescendants || table == null) {
            return builder.append(table);
        }
        appendTo(builder);
        if (table.getAlias() != null) {
            builder.append(table.getAlias());
        }
        return builder;
    }

    /** Whether ONLY or * describes the inheritance scope of the table. */
    public boolean hasInheritanceScope() {
        return only || includeDescendants;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
