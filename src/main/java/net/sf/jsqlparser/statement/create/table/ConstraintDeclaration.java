/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

/**
 * A view of a constraint declared in a CREATE TABLE definition. Its source nodes remain editable;
 * no constraint data is copied. A column declaration retains its owning column rather than
 * manufacturing a table constraint or a key-column list.
 *
 * @see CreateTable#getConstraints()
 */
public final class ConstraintDeclaration {
    private final ColumnDefinition column;
    private final ColumnOption columnOption;
    private final Index index;

    ConstraintDeclaration(ColumnDefinition column, ColumnOption columnOption) {
        this.column = column;
        this.columnOption = columnOption;
        this.index = null;
    }

    ConstraintDeclaration(Index index) {
        this.column = null;
        this.columnOption = null;
        this.index = index;
    }

    /** Returns the current kind, or OTHER if an edit no longer represents a constraint. */
    public Index.Kind getKind() {
        if (columnOption != null) {
            return columnOption.getConstraintKind();
        }
        return index.getKind() != null && index.getKind().canDescribeConstraint() ? index.getKind()
                : Index.Kind.OTHER;
    }

    /** Returns the original owning column, or null for a table-level declaration. */
    public ColumnDefinition getColumn() {
        return column;
    }

    /** Returns the original column option, or null for a table-level declaration. */
    public ColumnOption getColumnOption() {
        return columnOption;
    }

    /**
     * Returns the original constraint node, including a column's nested constraint. REFERENCES,
     * DEFAULT and nullability column options have no Index node; use getColumnOption() for them.
     */
    public Index getIndex() {
        return columnOption == null ? index : columnOption.getConstraint();
    }
}
