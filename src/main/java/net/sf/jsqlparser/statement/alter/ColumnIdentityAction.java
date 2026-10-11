/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import java.util.List;

/**
 * An ALTER COLUMN target and its ordered identity alterations. Obtain active column actions through
 * {@link AlterExpression#getColumnIdentityActions()}; edits update the existing statement model.
 */
public interface ColumnIdentityAction {
    String getColumnName();

    void setColumnName(String columnName);

    /** Returns the mutable, ordered alterations, or null when this column has none. */
    List<IdentityAlteration> getIdentityAlterations();

    /** Replaces the alterations; null restores the column's ordinary definition rendering. */
    void setIdentityAlterations(List<IdentityAlteration> identityAlterations);
}
