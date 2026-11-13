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

public class MergeDelete implements Serializable, MergeOperation {
    private Expression andPredicate;
    private MergeSide side;

    public Expression getAndPredicate() {
        return andPredicate;
    }

    public void setAndPredicate(Expression andPredicate) {
        this.andPredicate = andPredicate;
    }

    public MergeSide getSide() {
        return side;
    }

    public MergeDelete setSide(MergeSide side) {
        this.side = side;
        return this;
    }

    /** {@code WHEN MATCHED}, or {@code WHEN NOT MATCHED BY SOURCE} with that side. */
    public MergeWhen getWhen() {
        return MergeWhen.ofTargetRow(side);
    }

    public MergeDelete withAndPredicate(Expression andPredicate) {
        this.setAndPredicate(andPredicate);
        return this;
    }

    public MergeDelete withSide(MergeSide side) {
        this.setSide(side);
        return this;
    }

    @Override
    public <S, T> T accept(MergeOperationVisitor<T> mergeOperationVisitor, S context) {
        return mergeOperationVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        getWhen().appendTo(b, andPredicate, b::append).append("DELETE");
        return b.toString();
    }
}
