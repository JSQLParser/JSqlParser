/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.Model;

import java.util.function.Predicate;

public interface Statement extends Model {
    <T, S> T accept(StatementVisitor<T> statementVisitor, S context);

    default void accept(StatementVisitor<?> statementVisitor) {
        accept(statementVisitor, null);
    }

    /**
     * Static feature analysis of this statement tree. Not cached: the AST is mutable and publicly
     * constructible, and a stale bitmask is worse than a recomputation (micro seconds against a
     * millisecond-scale parse).
     */
    default StatementFeatures getFeatures() {
        return StatementFeatureVisitor.analyse(this);
    }

    /** Same, with a caller-supplied allow-list of provably side-effect-free functions. */
    default StatementFeatures getFeatures(Predicate<String> pureFunctions) {
        return StatementFeatureVisitor.analyse(this, pureFunctions);
    }
}
