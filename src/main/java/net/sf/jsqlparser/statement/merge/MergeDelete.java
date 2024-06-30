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

    public Expression getAndPredicate() {
        return andPredicate;
    }

    public void setAndPredicate(Expression andPredicate) {
        this.andPredicate = andPredicate;
    }

    public MergeDelete withAndPredicate(Expression andPredicate) {
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
        b.append(" WHEN MATCHED");
        if (andPredicate != null) {
            b.append(" AND ").append(andPredicate.toString());
        }
        b.append(" THEN DELETE");
        return b.toString();
    }
}
