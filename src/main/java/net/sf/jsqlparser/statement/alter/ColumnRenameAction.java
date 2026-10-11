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

/**
 * Common access to column renames on tables and views. Obtain the active rename through
 * {@link AlterExpression#getColumnRename()}. Names retain their SQL identifier quotes, and changes
 * update the existing action without changing its operation or optional COLUMN keyword.
 */
public interface ColumnRenameAction {
    String getSourceName();

    void setSourceName(String name);

    String getTargetName();

    void setTargetName(String name);
}
