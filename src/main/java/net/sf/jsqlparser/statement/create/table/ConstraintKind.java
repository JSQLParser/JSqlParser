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

/** The kind of a declared table or column constraint, independent of its supporting index. */
public enum ConstraintKind {
    PRIMARY_KEY, UNIQUE, FOREIGN_KEY, CHECK, EXCLUDE, DEFAULT, NOT_NULL, OTHER;

    public static ConstraintKind fromType(String type) {
        if (type == null) {
            return OTHER;
        }
        String normalized = type.trim().toUpperCase(java.util.Locale.ROOT);
        switch (normalized.split("\\s+", 2)[0]) {
            case "PRIMARY":
                return PRIMARY_KEY;
            case "UNIQUE":
                return UNIQUE;
            case "FOREIGN":
                return FOREIGN_KEY;
            case "CHECK":
                return CHECK;
            case "EXCLUDE":
                return EXCLUDE;
            case "DEFAULT":
                return DEFAULT;
            case "NOT":
                return "NOT NULL".equals(normalized) ? NOT_NULL : OTHER;
            default:
                return OTHER;
        }
    }
}
