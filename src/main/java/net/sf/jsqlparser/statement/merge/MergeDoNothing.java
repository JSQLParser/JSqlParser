/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.merge;

import net.sf.jsqlparser.expression.Expression;

import java.io.Serializable;
import java.util.Objects;

/** PostgreSQL's {@code WHEN [NOT] MATCHED [BY SOURCE | BY TARGET] [AND ...] THEN DO NOTHING}. */
public class MergeDoNothing implements Serializable, MergeOperation {
    private MergeWhen when = MergeWhen.MATCHED;
    private Expression andPredicate;

    public MergeDoNothing() {}

    public MergeDoNothing(MergeWhen when) {
        setWhen(when);
    }

    public MergeWhen getWhen() {
        return when;
    }

    public void setWhen(MergeWhen when) {
        this.when = Objects.requireNonNull(when);
    }

    public MergeDoNothing withWhen(MergeWhen when) {
        this.setWhen(when);
        return this;
    }

    public Expression getAndPredicate() {
        return andPredicate;
    }

    public void setAndPredicate(Expression andPredicate) {
        this.andPredicate = andPredicate;
    }

    public MergeDoNothing withAndPredicate(Expression andPredicate) {
        this.setAndPredicate(andPredicate);
        return this;
    }

    @Override
    public <S, T> T accept(MergeOperationVisitor<T> mergeOperationVisitor, S context) {
        return mergeOperationVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        when.appendTo(b, andPredicate, b::append).append("DO NOTHING");
        return b.toString();
    }
}
