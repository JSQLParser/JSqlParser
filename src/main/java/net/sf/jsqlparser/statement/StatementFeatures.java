/*-
 * Copyright (C) 2004 - 2026 JSQLParser
 * Licensed under the Apache License, Version 2.0.
 */
package net.sf.jsqlparser.statement;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Result of a static feature analysis of a {@link Statement}.
 *
 * <p>
 * Every flag is three-valued: <em>certain</em> (the grammar proves it), <em>possible</em> (could
 * not be excluded), or absent (the grammar proves it cannot happen). Callers pick their own risk
 * direction:
 *
 * <ul>
 * <li>a dispatcher (executeQuery vs. executeUpdate) uses {@link #is(StmtFeature)}</li>
 * <li>a read-only guard uses {@link #may(StmtFeature)}</li>
 * </ul>
 *
 * <p>
 * <b>This is a syntactic claim, never a semantic guarantee.</b> Function volatility lives in the
 * catalogue (e.g. {@code pg_proc.provolatile}), not in the parse tree.
 */
public final class StatementFeatures {

    private final Set<StmtFeature> certain;
    private final Set<StmtFeature> uncertain;
    private final Set<String> unresolved;

    public StatementFeatures(EnumSet<StmtFeature> certain, EnumSet<StmtFeature> uncertain,
            Set<String> unresolved) {
        // a feature proven certain is never also reported as merely possible
        EnumSet<StmtFeature> u = EnumSet.copyOf(uncertain);
        u.removeAll(certain);
        this.certain = Collections.unmodifiableSet(EnumSet.copyOf(certain));
        this.uncertain = Collections.unmodifiableSet(u);
        this.unresolved = Collections.unmodifiableSet(new LinkedHashSet<>(unresolved));
    }

    /** Proven by the grammar. */
    public boolean is(StmtFeature feature) {
        return certain.contains(feature);
    }

    /** Proven, or not excludable. This is what a guard must call. */
    public boolean may(StmtFeature feature) {
        return certain.contains(feature) || uncertain.contains(feature);
    }

    public Set<StmtFeature> getCertain() {
        return certain;
    }

    /** Strictly the "could not be excluded" set; disjoint from {@link #getCertain()}. */
    public Set<StmtFeature> getUncertain() {
        return uncertain;
    }

    /**
     * Why the analysis is uncertain: unresolved function names, dynamic SQL markers, called
     * procedure names. Resolve these against your own catalogue or allow-list.
     */
    public Set<String> getUnresolvedReferences() {
        return unresolved;
    }

    public boolean isOpaque() {
        return may(StmtFeature.OPAQUE);
    }

    // ---- convenience shorthands -------------------------------------------------------------
    // Deliberately named for what they mean, not for the DDL/DML/Query folklore, which cannot
    // express "INSERT ... RETURNING *" (both row-returning and mutating) at all.

    public boolean returnsResultSet() {
        return is(StmtFeature.RETURNS_RESULT_SET);
    }

    public boolean modifiesData() {
        return is(StmtFeature.MODIFIES_DATA);
    }

    public boolean mayModifyData() {
        return may(StmtFeature.MODIFIES_DATA);
    }

    public boolean modifiesSchema() {
        return is(StmtFeature.MODIFIES_SCHEMA);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("certain=").append(certain);
        if (!uncertain.isEmpty()) {
            b.append(" possible=").append(uncertain);
        }
        if (!unresolved.isEmpty()) {
            b.append(" unresolved=").append(unresolved);
        }
        return b.toString();
    }
}
