/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.insert;

/**
 * The conflict resolution algorithm of SQLite's {@code INSERT OR <algorithm>}, which {@link Insert}
 * carries as a prefix in front of the INTO keyword.
 *
 * <p>
 * SQLite accepts ABORT, FAIL, IGNORE, REPLACE and ROLLBACK after {@code INSERT OR} (see SQLite's
 * {@code lang_insert} and {@code lang_conflict} pages). MySQL's {@code REPLACE} statement keeps its
 * historic {@link net.sf.jsqlparser.statement.upsert.Upsert} reading.
 *
 * @see net.sf.jsqlparser.statement.update.UpdateConflictAlgorithm
 */
public enum InsertConflictAlgorithm {

    OR_ABORT("OR ABORT"), OR_FAIL("OR FAIL"), OR_IGNORE("OR IGNORE"), OR_REPLACE(
            "OR REPLACE"), OR_ROLLBACK("OR ROLLBACK");

    private final String keywords;

    InsertConflictAlgorithm(String keywords) {
        this.keywords = keywords;
    }

    /** The words printed between INSERT and INTO. */
    public String getKeywords() {
        return keywords;
    }

    /** The algorithm without the OR, as in {@code INSERT OR IGNORE}: IGNORE. */
    public String getAlgorithm() {
        return keywords.substring("OR ".length());
    }

    /**
     * The algorithm of {@code INSERT OR <algorithm>}.
     *
     * @param algorithm ABORT, FAIL, IGNORE, REPLACE or ROLLBACK, in any case
     * @return the matching constant
     * @throws IllegalArgumentException when the word is not a SQLite conflict algorithm
     */
    public static InsertConflictAlgorithm fromAlgorithm(String algorithm) {
        String upperCase = algorithm.toUpperCase(java.util.Locale.ROOT);
        for (InsertConflictAlgorithm value : values()) {
            if (value.getAlgorithm().equals(upperCase)) {
                return value;
            }
        }
        throw new IllegalArgumentException(
                "Expected ABORT, FAIL, IGNORE, REPLACE or ROLLBACK but found " + algorithm);
    }

    @Override
    public String toString() {
        return keywords;
    }
}
