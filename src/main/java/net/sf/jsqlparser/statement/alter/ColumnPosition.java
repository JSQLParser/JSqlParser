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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The MySQL/MariaDB {@code FIRST} or {@code AFTER column} placement that follows the column
 * definition of an ALTER TABLE ADD, CHANGE or MODIFY action. Instances are immutable; replace the
 * position of a definition to move the column.
 */
public final class ColumnPosition implements Serializable {

    public enum Kind {
        FIRST, AFTER
    }

    private static final ColumnPosition FIRST = new ColumnPosition(Kind.FIRST, null);

    private final Kind kind;
    private final String columnName;

    private ColumnPosition(Kind kind, String columnName) {
        this.kind = kind;
        this.columnName = columnName;
    }

    /** Places the column at the start of the table. */
    public static ColumnPosition first() {
        return FIRST;
    }

    /** Places the column after the named existing column. */
    public static ColumnPosition after(String columnName) {
        return new ColumnPosition(Kind.AFTER, Objects.requireNonNull(columnName, "columnName"));
    }

    public Kind getKind() {
        return kind;
    }

    /** Returns the preceding column of an AFTER position, or null for FIRST. */
    public String getColumnName() {
        return columnName;
    }

    /** Returns the SQL tokens of this position as an unmodifiable list. */
    public List<String> getTokens() {
        return kind == Kind.FIRST
                ? Collections.singletonList("FIRST")
                : Collections.unmodifiableList(Arrays.asList("AFTER", columnName));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ColumnPosition)) {
            return false;
        }
        ColumnPosition position = (ColumnPosition) other;
        return kind == position.kind && Objects.equals(columnName, position.columnName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, columnName);
    }

    @Override
    public String toString() {
        return kind == Kind.FIRST ? "FIRST" : "AFTER " + columnName;
    }
}
