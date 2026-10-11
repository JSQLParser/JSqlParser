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
    private final NamedConstraint constraint;

    ConstraintDeclaration(ColumnDefinition column, ColumnOption columnOption) {
        this.column = column;
        this.columnOption = columnOption;
        this.constraint = null;
    }

    ConstraintDeclaration(NamedConstraint constraint) {
        this.column = null;
        this.columnOption = null;
        this.constraint = constraint;
    }

    /**
     * Returns the current kind, or OTHER if it is unknown or an option no longer declares a
     * constraint.
     */
    public ConstraintKind getKind() {
        if (columnOption != null) {
            return columnOption.getConstraintKind();
        }
        return constraint.getKind() != null ? constraint.getKind() : ConstraintKind.OTHER;
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
     * Returns the original constraint node, including a column's nested constraint. REFERENCES and
     * nullability column options have no NamedConstraint node; use getColumnOption() for them.
     */
    public NamedConstraint getConstraint() {
        return columnOption == null ? constraint : columnOption.getConstraint();
    }
}
