/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.update;

import java.util.Locale;

/**
 * The conflict resolution algorithm of SQLite's {@code UPDATE OR <algorithm>}.
 */
public enum UpdateConflictAlgorithm {
    ABORT, FAIL, IGNORE, REPLACE, ROLLBACK;

    public static UpdateConflictAlgorithm from(String algorithm) {
        return Enum.valueOf(UpdateConflictAlgorithm.class, algorithm.toUpperCase(Locale.ROOT));
    }
}
