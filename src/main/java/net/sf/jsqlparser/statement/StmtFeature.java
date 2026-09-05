/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

/**
 * What a statement does, as far as the grammar can prove it.
 *
 * <p>
 * Named {@code StmtFeature} rather than {@code Feature} because
 * {@code net.sf.jsqlparser.parser.feature.Feature} already occupies that simple name and the two
 * are unrelated: that one describes parser capabilities, this one describes statement effects.
 *
 * <p>
 * These are not mutually exclusive. {@code INSERT .. RETURNING *} carries {@link #MODIFIES_DATA}
 * and {@link #RETURNS_RESULT_SET}; {@code CREATE TABLE .. AS SELECT} carries
 * {@link #MODIFIES_SCHEMA} and {@link #READS_DATA}. Any attempt to collapse them into a single
 * Query/DDL/DML verdict loses information the caller needs.
 */
public enum StmtFeature {
    /** Reads persistent data - a table or view in a query position. */
    READS_DATA,

    /**
     * Returns a result set <em>to the client</em>. A property of the statement's own result
     * position, never of a nested one: {@code INSERT INTO x SELECT ..} does not have it.
     */
    RETURNS_RESULT_SET,

    /** Inserts, updates, deletes, merges or otherwise destroys rows. */
    MODIFIES_DATA,

    /** Creates, alters, drops, truncates, renames, comments, grants. */
    MODIFIES_SCHEMA,

    /** SET, RESET, USE, DECLARE, ALTER SESSION. */
    MODIFIES_SESSION,

    /** COMMIT, ROLLBACK, SAVEPOINT, LOCK, SELECT .. FOR UPDATE. */
    MODIFIES_TRANSACTION,

    /** Nothing further is knowable statically: CALL, EXECUTE, dynamic SQL, procedure bodies. */
    OPAQUE
}
